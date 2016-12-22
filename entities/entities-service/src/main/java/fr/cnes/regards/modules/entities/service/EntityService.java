/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.service.validator.ValidatorFactory;
import fr.cnes.regards.modules.models.domain.ComputationMode;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IModelAttributeService;

/**
 * Entity service implementation
 *
 * @author Marc Sordi
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
    @Autowired
    private IModelAttributeService modelAttributeService;

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

        buildAttributeMap(attMap, Fragment.getDefaultName(), pAbstractEntity.getAttributes());
        // build map

        // Loop over model attributes ... to check each attribute
        for (ModelAttribute modelAtt : modAtts) {
            AttributeModel attModel = modelAtt.getAttribute();
            String key = attModel.getFragment().getName().concat(NAMESPACE_SEPARATOR).concat(attModel.getName());
            LOGGER.debug(String.format("Computed key : \"%s\"", key));

            // Retrieve attribute
            AbstractAttribute<?> att = attMap.get(key);

            // Only "GIVEN" attribute must be set
            if (!ComputationMode.GIVEN.equals(modelAtt.getMode()) && (att != null)) {
                pErrors.rejectValue(key, "error.computed.attribute.given", "Computed attribute value must not be set.");
            }

            // Required attribute must be set
            if (!pManageAlterable && !attModel.isOptional() && (att == null)) {
                pErrors.rejectValue(key, "error.attribute.required", "Attribute required.");
            }

            // Update mode only :
            // FIXME retrieve not alterable attribute from database before update
            if (pManageAlterable && !attModel.isAlterable() && (att != null)) {
                pErrors.rejectValue(key, "error.attribute.not.alterable", "Attribute not alterable must not be set.");
            }

            if (att != null) {
                // Check attribute type
                if (!att.represents(attModel.getType())) {
                    pErrors.rejectValue(key, "error.inconsistent.attribute.type",
                                        "Attribute not consistent with model attribute type.");
                }

                // Check restriction
                if (attModel.hasRestriction()) {
                    ValidatorFactory.getValidator(attModel.getRestriction());
                }

                // Static validation
                // TODO RestrictionValidatorFactory.getValidator(pRestriction)
            }
        }
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
    private void buildAttributeMap(Map<String, AbstractAttribute<?>> pAttMap, String pNamespace,
            final List<AbstractAttribute<?>> pAttributes) {
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
