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
import fr.cnes.regards.modules.model.dto.properties.AbstractProperty;
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
public abstract class AbstractValidationService<F extends AbstractFeature<Set<AbstractProperty<?>>, ?>>
        implements IValidationService<F> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractValidationService.class);

    private final IModelFinder modelFinder;

    public AbstractValidationService(IModelFinder modelFinder) {
        this.modelFinder = modelFinder;
    }

    @Override
    public Errors validate(String model, F feature, ValidationMode mode) {

        Errors errors = new MapBindingResult(new HashMap<>(), AbstractProperty.class.getName());

        // Retrieve attributes from model
        List<ModelAttrAssoc> modAtts = modelFinder.findByModel(model);

        // Build fast property access map
        Map<String, AbstractProperty<?>> pptyMap = getPropertyMap(feature.getProperties());
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
    private Map<String, AbstractProperty<?>> getPropertyMap(Set<AbstractProperty<?>> properties) {
        Map<String, AbstractProperty<?>> pmap = new HashMap<>();
        for (AbstractProperty<?> ppt : properties) {
            addPropertyToMap(pmap, ppt, null);
        }
        return pmap;
    }

    private void addPropertyToMap(Map<String, AbstractProperty<?>> pmap, AbstractProperty<?> ppt, String namespace) {
        if (ppt.represents(PropertyType.OBJECT)) {
            for (AbstractProperty<?> inner : ((ObjectProperty) ppt).getValue()) {
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
            Map<String, AbstractProperty<?>> pptyMap, Set<String> toCheckProperties) {

        AttributeModel attModel = modelAttrAssoc.getAttribute();
        String attPath = attModel.getJsonPropertyPath();

        // Only validate attribute that have a ComputationMode of GIVEN. Otherwise the attribute value will most likely
        // be missing and is added during the crawling process
        if (modelAttrAssoc.getMode() == ComputationMode.GIVEN) {
            LOGGER.debug("Computed key : \"{}\"", attPath);

            // Retrieve property
            AbstractProperty<?> att = pptyMap.get(attPath);

            // Null value check
            if (att == null) {
                if (!attModel.isOptional()) {
                    String messageKey = "error.missing.required.property.message";
                    String defaultMessage = String.format("Missing required property \"%s\".", attPath);
                    errors.reject(messageKey, defaultMessage);
                    return;
                }
                LOGGER.debug(String.format("Property \"%s\" is optional in current context.", attPath));
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
