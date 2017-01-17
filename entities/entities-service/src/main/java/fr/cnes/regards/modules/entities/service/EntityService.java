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
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractDataEntity;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.AbstractLinkEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.service.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.entities.service.validator.ComputationModeValidator;
import fr.cnes.regards.modules.entities.service.validator.NotAlterableAttributeValidator;
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

        if (pErrors.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (ObjectError error : pErrors.getAllErrors()) {
                String errorMessage = error.getDefaultMessage();
                LOGGER.error(errorMessage);
                errors.add(errorMessage);
            }
            throw new EntityInvalidException(errors);
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

        // Null value check
        if (att == null) {
            String messageKey = "error.missing.required.attribute.message";
            String defaultMessage = String.format("Missing required attribute \"%s\".", key);
            if (pManageAlterable && attModel.isAlterable() && !attModel.isOptional()) {
                pErrors.reject(messageKey, defaultMessage);
                return;
            }
            if (!pManageAlterable && !attModel.isOptional()) {
                pErrors.reject(messageKey, defaultMessage);
                return;
            }
            LOGGER.debug(String.format("Attribute \"%s\" not required in current context.", key));
            return;
        }

        // Do validation
        for (Validator validator : getValidators(pModelAttribute, key, pManageAlterable)) {
            if (validator.supports(att.getClass())) {
                validator.validate(att, pErrors);
            } else {
                String defaultMessage = String.format("Unsupported validator \"%s\" for attribute \"%s\"",
                                                      validator.getClass().getName(), key);
                pErrors.reject("error.unsupported.validator.message", defaultMessage);
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
                // Compute value
                if (ObjectAttribute.class.equals(att.getClass())) {
                    ObjectAttribute o = (ObjectAttribute) att;
                    buildAttributeMap(pAttMap, att.getName(), o.getValue());
                } else {
                    // Compute key
                    String key = pNamespace.concat(NAMESPACE_SEPARATOR).concat(att.getName());
                    LOGGER.debug(String.format("Key \"%s\" -> \"%s\".", key, att.toString()));
                    pAttMap.put(key, att);
                }
            }
        }
    }

    private Collection associateCollection(Collection pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToAssociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        for (AbstractEntity target : entityToAssociate) {
            if (!(target instanceof Document)) {
                // Documents cannot be tagged into Collections
                pSource.getTags().add(target.getIpId().toString());
            }
            // bidirectional association if it's a collection or dataset
            if (target instanceof AbstractLinkEntity) {
                target.getTags().add(pSource.getIpId().toString());
                entitiesRepository.save(target);
            }
        }

        return entitiesRepository.save(pSource);
    }

    private AbstractDataEntity associateDataEntity(AbstractDataEntity pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToAssociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        for (AbstractEntity target : entityToAssociate) {
            if (target instanceof AbstractLinkEntity) {
                // only Collections(and DataSets) can only be associated with DataObjects
                pSource.getTags().add(target.getIpId().toString());
            }
        }
        return entitiesRepository.save(pSource);
    }

    private DataSet associateDataSet(DataSet pSource, Set<UniformResourceName> pTargetsUrn) {
        return pSource;
    }

    @Override
    public <T extends AbstractEntity> T dissociate(T pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToDissociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        final Set<String> toDissociateAssociations = pSource.getTags();
        for (AbstractEntity toBeDissociated : entityToDissociate) {
            toDissociateAssociations.remove(toBeDissociated.getIpId().toString());
            toBeDissociated.getTags().remove(pSource.getIpId().toString());
            entitiesRepository.save(toBeDissociated);
        }
        pSource.setTags(toDissociateAssociations);
        return entitiesRepository.save(pSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractEntity> T associate(T pSource, Set<UniformResourceName> pTargetsUrn) {
        if (pSource instanceof Collection) {
            return (T) associateCollection((Collection) pSource, pTargetsUrn);
        }
        if (pSource instanceof AbstractDataEntity) {
            return (T) associateDataEntity((AbstractDataEntity) pSource, pTargetsUrn);
        }
        if (pSource instanceof DataSet) {
            return (T) associateDataSet((DataSet) pSource, pTargetsUrn);
        }
        throw new UnsupportedOperationException("routing for " + pSource.getClass() + " is not implemented");
    }

}