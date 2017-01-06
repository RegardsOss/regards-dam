/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataEntity;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.Tag;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.service.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.entities.service.validator.ComputationModeValidator;
import fr.cnes.regards.modules.entities.service.validator.NotAlterableAttributeValidator;
import fr.cnes.regards.modules.entities.service.validator.RequiredAttributeValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.RestrictionValidatorFactory;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IModelAttributeService;

/**
 * Entity service implementation
 *
 * @author Marc Sordi
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Service
public class EntityService implements IEntityService {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityService.class);

    /**
     * Namespace separator
     */
    private static final String NAMESPACE_SEPARATOR = ".";

    /**
     * Attribute model service
     */
    private final IModelAttributeService modelAttributeService;

    private final IAbstractEntityRepository<AbstractEntity> entitiesRepository;

    public EntityService(IModelAttributeService pModelAttributeService,
            IAbstractEntityRepository<AbstractEntity> pEntitiesRepository) {
        modelAttributeService = pModelAttributeService;
        entitiesRepository = pEntitiesRepository;
    }

    @Override
    public void validate(AbstractEntity pAbstractEntity, Errors pErrors, boolean pManageAlterable)
            throws ModuleException {
        Assert.notNull(pAbstractEntity, "Entity must not be null.");

        Model model = pAbstractEntity.getModel();
        Assert.notNull(model, "Model must be set on entity in order to be validated.");
        Assert.notNull(model.getId(), "Model identifier must be specified.");

        // Retrieve model attributes
        List<ModelAttribute> modAtts = modelAttributeService.getModelAttributes(model.getId());

        // Check model not empty
        if (((modAtts == null) || modAtts.isEmpty()) && (pAbstractEntity.getAttributes() != null)) {
            pErrors.rejectValue("attributes", "error.no.attribute.defined.but.set",
                                "No attribute defined in corresponding model but trying to create.");
        }

        // Prepare attributes for validation check
        Map<String, AbstractAttribute<?>> attMap = new HashMap<>();

        // Build attribute map
        buildAttributeMap(attMap, Fragment.getDefaultName(), pAbstractEntity.getAttributes());

        // Loop over model attributes ... to validate each attribute
        for (ModelAttribute modelAtt : modAtts) {
            checkModelAttribute(attMap, modelAtt, pErrors, pManageAlterable);
        }
    }

    /**
     * Validate an attribute with its corresponding model attribute
     *
     * @param pAttMap
     *            attribue map
     * @param pModelAttribute
     *            model attribute
     * @param pErrors
     *            validation errors
     * @param pManageAlterable
     *            manage update or not
     */
    protected void checkModelAttribute(Map<String, AbstractAttribute<?>> pAttMap, ModelAttribute pModelAttribute,
            Errors pErrors, boolean pManageAlterable) {

        AttributeModel attModel = pModelAttribute.getAttribute();
        String key = attModel.getFragment().getName().concat(NAMESPACE_SEPARATOR).concat(attModel.getName());
        LOGGER.debug(String.format("Computed key : \"%s\"", key));

        // Retrieve attribute
        AbstractAttribute<?> att = pAttMap.get(key);

        // Do validation
        for (Validator validator : getValidators(pModelAttribute, key, pManageAlterable)) {
            if (validator.supports(att.getClass())) {
                validator.validate(att, pErrors);
            } else {
                pErrors.rejectValue(key, "error.unsupported.validator.message", "Unsupported validator.");
            }
        }
    }

    /**
     * Compute available validators
     *
     * @param pModelAttribute
     *            {@link ModelAttribute}
     * @param pAttributeKey
     *            attribute key
     * @param pManageAlterable
     *            manage update or not
     * @return {@link Validator} list
     */
    protected List<Validator> getValidators(ModelAttribute pModelAttribute, String pAttributeKey,
            boolean pManageAlterable) {

        AttributeModel attModel = pModelAttribute.getAttribute();

        List<Validator> validators = new ArrayList<>();
        // Check computation mode
        validators.add(new ComputationModeValidator(pModelAttribute.getMode(), pAttributeKey));
        // Check required attribute
        if (!pManageAlterable && !attModel.isOptional()) {
            validators.add(new RequiredAttributeValidator(pAttributeKey));
        }
        // Check alterable attribute
        // Update mode only :
        // FIXME retrieve not alterable attribute from database before update
        if (pManageAlterable && !attModel.isAlterable()) {
            validators.add(new NotAlterableAttributeValidator(pAttributeKey));
        }
        // Check attribute type
        validators.add(new AttributeTypeValidator(attModel.getType(), pAttributeKey));
        // Check restriction
        if (attModel.hasRestriction()) {
            validators.add(RestrictionValidatorFactory.getValidator(attModel.getRestriction(), pAttributeKey));
        }
        return validators;
    }

    /**
     * Build real attribute map extracting namespace from {@link ObjectAttribute} (i.e. fragment name)
     *
     * @param pAttMap
     *            {@link Map} to build
     * @param pNamespace
     *            namespace context
     * @param pAttributes
     *            {@link AbstractAttribute} list to analyze
     */
    protected void buildAttributeMap(Map<String, AbstractAttribute<?>> pAttMap, String pNamespace,
            final List<AbstractAttribute<?>> pAttributes) {
        if (pAttributes != null) {
            for (AbstractAttribute<?> att : pAttributes) {

                // Compute key
                String key = pNamespace.concat(NAMESPACE_SEPARATOR).concat(att.getName());

                // Compute value
                if (ObjectAttribute.class.equals(att.getClass())) {
                    ObjectAttribute o = (ObjectAttribute) att;
                    buildAttributeMap(pAttMap, key, o.getValue());
                } else {
                    LOGGER.debug(String.format("Key \"%s\" -> \"%s\".", key, att.toString()));
                    pAttMap.put(key, att);
                }
            }
        }
    }

    @Override
    public Collection associate(Collection pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToAssociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        for (AbstractEntity target : entityToAssociate) {
            if (!(target instanceof Document)) {
                // Collections cannot be tagged into Document
                pSource.getTags().add(new Tag(target.getIpId().toString()));
            }
            // bidirectional association if it's a collection or dataset
            if (target instanceof Collection) {
                target.getTags().add(new Tag(pSource.getIpId().toString()));
                entitiesRepository.save(target);
            }
        }

        return entitiesRepository.save(pSource);
    }

    @Override
    public DataEntity associate(DataEntity pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToAssociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        for (AbstractEntity target : entityToAssociate) {
            if (target instanceof Collection) {
                // only Collections(and DataSets) can only be associated with DataObjects
                pSource.getTags().add(new Tag(target.getIpId().toString()));
            }
        }
        return entitiesRepository.save(pSource);
    }

    @Override
    public DataSet associate(DataSet pSource, Set<UniformResourceName> pTargetsUrn) {
        return pSource;
    }

    @Override
    public <T extends AbstractEntity> T dissociate(T pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToDissociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        final Set<Tag> toDissociateAssociations = pSource.getTags();
        for (AbstractEntity toBeDissociated : entityToDissociate) {
            toDissociateAssociations.remove(new Tag(toBeDissociated.getIpId().toString()));
            toBeDissociated.getTags().remove(new Tag(pSource.getIpId().toString()));
            entitiesRepository.save(toBeDissociated);
        }
        pSource.setTags(toDissociateAssociations);
        return entitiesRepository.save(pSource);
    }

}
