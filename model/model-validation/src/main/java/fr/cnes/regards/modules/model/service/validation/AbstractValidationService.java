/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.model.service.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import fr.cnes.regards.framework.geojson.AbstractFeature;
import fr.cnes.regards.modules.model.domain.ComputationMode;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.ObjectProperty;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;
import fr.cnes.regards.modules.model.service.validation.validator.ComputationModeValidator;
import fr.cnes.regards.modules.model.service.validation.validator.PropertyTypeValidator;
import fr.cnes.regards.modules.model.service.validation.validator.restriction.RestrictionValidatorFactory;

/**
 *
 * Override this class to validate feature properties
 * @author oroussel
 * @author Marc SORDI
 */
public abstract class AbstractValidationService<F extends AbstractFeature<Set<IProperty<?>>, ?>>
        implements IValidationService<F> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractValidationService.class);

    private final IModelFinder modelFinder;

    public AbstractValidationService(IModelFinder modelFinder) {
        this.modelFinder = modelFinder;
    }

    @Override
    public Errors validate(String model, F feature, ValidationMode mode, String objectName) {

        Errors errors = new MapBindingResult(new HashMap<>(), objectName);

        // Retrieve attributes from model
        List<ModelAttrAssoc> modAtts = modelFinder.findByModel(model);

        // Build fast property access map
        Map<String, IProperty<?>> pptyMap = getPropertyMap(feature.getProperties());
        // Get a copy of entity attributes values to optimize the search of unexpected properties
        // FIXME check it's a real copy!
        Set<String> toCheckProperties = new HashSet<>(pptyMap.keySet());

        // Loop over model attributes ... to validate each properties
        for (ModelAttrAssoc modelAttrAssoc : modAtts) {
            checkModelAttribute(modelAttrAssoc, errors, mode, feature, pptyMap, toCheckProperties);
        }

        // If properties isn't empty it means some properties are unexpected by the model
        if (!toCheckProperties.isEmpty()) {
            toCheckProperties.forEach(propPath -> errors
                    .reject("error.unexpected.property.message",
                            String.format("%s isn't expected by the model %s", propPath, model)));
        }

        return errors;
    }

    /**
     * Build a fast access map for current properties
     */
    private Map<String, IProperty<?>> getPropertyMap(Set<IProperty<?>> properties) {
        Map<String, IProperty<?>> pmap = new HashMap<>();
        if (properties != null) {
            for (IProperty<?> ppt : properties) {
                addPropertyToMap(pmap, ppt, null);
            }
        }
        return pmap;
    }

    private void addPropertyToMap(Map<String, IProperty<?>> pmap, IProperty<?> ppt, String namespace) {
        if (ppt.represents(PropertyType.OBJECT)) {
            for (IProperty<?> inner : ((ObjectProperty) ppt).getValue()) {
                addPropertyToMap(pmap, inner, ppt.getName());
            }
        } else {
            StringBuilder builder = new StringBuilder();
            if (namespace != null && !namespace.isEmpty()) {
                builder.append(namespace);
                builder.append(".");
            }
            pmap.put(builder.append(ppt.getName()).toString(), ppt);
        }
    }

    /**
     * Validate a property according to its corresponding model attribute
     * @param modelAttrAssoc model attribute
     * @param errors validation errors
     * @param pptyMap properties to check
     * @param toCheckProperties properties not already checked
     *
     */
    protected void checkModelAttribute(ModelAttrAssoc modelAttrAssoc, Errors errors, ValidationMode mode, F feature,
            Map<String, IProperty<?>> pptyMap, Set<String> toCheckProperties) {

        AttributeModel attModel = modelAttrAssoc.getAttribute();
        String attPath = attModel.getJsonPropertyPath();

        // Only validate attribute that have a ComputationMode of GIVEN. Otherwise the attribute value will most likely
        // be missing and is added during the crawling process
        if (modelAttrAssoc.getMode() == ComputationMode.GIVEN) {
            LOGGER.debug("Computed key : \"{}\"", attPath);

            // Retrieve property
            IProperty<?> att = pptyMap.get(attPath);

            // Null property check
            if (att == null) {
                checkNullProperty(attModel, errors, mode);
                return;
            }

            // Null property value check
            if (att.getValue() == null) {
                checkNullPropertyValue(attModel, errors, mode);
                return;
            }

            // Check if value is expected or not according to the validation context
            checkAuthorizedPropertyValue(attModel, errors, mode);
            if (errors.hasErrors()) {
                // Ok, attribute has been checked
                toCheckProperties.remove(attPath);
                return;
            }

            // Do validation
            for (Validator validator : getValidators(modelAttrAssoc, attPath, mode, feature)) {
                if (validator.supports(att.getClass())) {
                    validator.validate(att, errors);
                } else {
                    String defaultMessage = String.format("Unsupported validator \"%s\" for property \"%s\"",
                                                          validator.getClass().getName(), attPath);
                    errors.reject("error.unsupported.validator.message", defaultMessage);
                }
            }
        }
        // Ok, attribute has been checked or is a computed one
        toCheckProperties.remove(attPath);
    }

    protected void checkNullProperty(AttributeModel attModel, Errors errors, ValidationMode mode) {
        if (!ValidationMode.PATCH.equals(mode)) { // In PATCH mode, all properties can be null
            if (!attModel.isOptional()) {
                String messageKey = "error.missing.required.property.message";
                String defaultMessage = String.format("Missing required property \"%s\".",
                                                      attModel.getJsonPropertyPath());
                errors.reject(messageKey, defaultMessage);
                return;
            }
        }
        LOGGER.debug("Property \"{}\" is optional in {} context.", attModel.getJsonPropertyPath(), mode);
    }

    protected void checkNullPropertyValue(AttributeModel attModel, Errors errors, ValidationMode mode) {
        if (ValidationMode.PATCH.equals(mode)) {
            // In PATCH mode, null value is used to unset a property
            if (!attModel.isAlterable()) {
                String messageKey = "error.unset.non.alterable.property.message";
                String defaultMessage = String.format("Non alterable property \"%s\" cannot be unset.",
                                                      attModel.getJsonPropertyPath());
                errors.reject(messageKey, defaultMessage);
            } else {
                LOGGER.debug("Property \"{}\" will be unset in {} context.", attModel.getJsonPropertyPath(), mode);
            }
        }
    }

    protected void checkAuthorizedPropertyValue(AttributeModel attModel, Errors errors, ValidationMode mode) {
        if (ValidationMode.PATCH.equals(mode)) {
            if (!attModel.isAlterable()) {
                String messageKey = "error.patch.non.alterable.property.message";
                String defaultMessage = String.format("Non alterable property \"%s\" must not be set in patch request.",
                                                      attModel.getJsonPropertyPath());
                errors.reject(messageKey, defaultMessage);
            } else {
                LOGGER.debug("Property \"{}\" will be updated in {} context.", attModel.getJsonPropertyPath(), mode);
            }
        }
    }

    /**
     * Get validators
     */
    protected List<Validator> getValidators(ModelAttrAssoc modelAttrAssoc, String attributeKey, ValidationMode mode,
            F feature) {
        AttributeModel attModel = modelAttrAssoc.getAttribute();

        List<Validator> validators = new ArrayList<>();
        // Check computation mode
        validators.add(new ComputationModeValidator(modelAttrAssoc.getMode(), attributeKey));
        // Check attribute type
        validators.add(new PropertyTypeValidator(attModel.getType(), attributeKey));
        // Check restriction
        if (attModel.hasRestriction()) {
            validators.add(RestrictionValidatorFactory.getValidator(attModel.getRestriction(), attributeKey));
        }
        return validators;
    }
}
