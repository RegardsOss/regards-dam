/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.models.ComputationMode;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;

/**
 * @author oroussel
 */
public abstract class AbstractValidationService<U extends AbstractEntity<?>> implements IValidationService<U> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Attribute model service
     */
    protected IModelAttrAssocService modelAttributeService;

    protected AbstractValidationService(IModelAttrAssocService modelAttributeService) {
        this.modelAttributeService = modelAttributeService;
    }

    @Override
    public void validate(U entity, Errors inErrors, boolean manageAlterable) throws EntityInvalidException {
        Assert.notNull(entity, "Entity must not be null.");

        Model model = entity.getModel();

        Assert.notNull(model, "Model must be set on entity in order to be validated.");
        Assert.notNull(model.getId(), "Model identifier must be specified.");

        // Retrieve model attributes
        List<ModelAttrAssoc> modAtts = modelAttributeService.getModelAttrAssocs(model.getName());

        // Get a copy of entity attributes values to optimize the search of unexpected properties
        Set<String> toCheckProperties = entity.getMutableCopyOfPropertiesPaths();
        // Loop over model attributes ... to validate each properties
        for (ModelAttrAssoc modelAtt : modAtts) {
            checkModelAttribute(modelAtt, inErrors, manageAlterable, entity, toCheckProperties);
        }

        // If properties isn't empty it means some properties are unexpected by the model
        List<String> unexpectedPropertyErrors = new ArrayList<>();
        if (!toCheckProperties.isEmpty()) {
            toCheckProperties
                    .forEach(propPath -> inErrors.reject("error.unexpected.attribute.message",
                                                         String.format("%s isn't expected by the model", propPath)));
        }

        if (inErrors.hasErrors() || !unexpectedPropertyErrors.isEmpty()) {
            List<String> errors = new ArrayList<>();
            errors.addAll(unexpectedPropertyErrors);
            for (ObjectError error : inErrors.getAllErrors()) {
                String errorMessage = error.getDefaultMessage();
                logger.error(errorMessage);
                errors.add(errorMessage);
            }
            throw new EntityInvalidException(errors);
        }
    }

    /**
     * Validate an attribute with its corresponding model attribute
     * @param modelAttribute model attribute
     * @param errors validation errors
     * @param manageAlterable manage update or not
     * @param entity current entity to check
     * @param toCheckProperties properties not already checked
     */
    protected void checkModelAttribute(ModelAttrAssoc modelAttribute, Errors errors, boolean manageAlterable,
            AbstractEntity<?> entity, Set<String> toCheckProperties) {

        // only validate attribute that have a ComputationMode of GIVEN. Otherwise the attribute will most likely be
        // missing and is added during the crawling process
        if (modelAttribute.getMode() == ComputationMode.GIVEN) {
            AttributeModel attModel = modelAttribute.getAttribute();
            String attPath = attModel.getName();
            if (!attModel.getFragment().isDefaultFragment()) {
                attPath = attModel.getFragment().getName().concat(".").concat(attPath);
            }
            logger.debug(String.format("Computed key : \"%s\"", attPath));

            // Retrieve attribute
            AbstractAttribute<?> att = entity.getProperty(attPath);

            // Null value check
            if (att == null) {
                String messageKey = "error.missing.required.attribute.message";
                String defaultMessage = String.format("Missing required attribute \"%s\".", attPath);
                // if (pManageAlterable && attModel.isAlterable() && !attModel.isOptional()) {
                if (!attModel.isOptional()) {
                    errors.reject(messageKey, defaultMessage);
                    return;
                }
                logger.debug(String.format("Attribute \"%s\" not required in current context.", attPath));
                return;
            }

            // Do validation
            for (Validator validator : getValidators(modelAttribute, attPath, manageAlterable, entity)) {
                if (validator.supports(att.getClass())) {
                    validator.validate(att, errors);
                } else {
                    String defaultMessage = String.format("Unsupported validator \"%s\" for attribute \"%s\"",
                                                          validator.getClass().getName(), attPath);
                    errors.reject("error.unsupported.validator.message", defaultMessage);
                }
            }
            // Ok, attribute has been checked
            toCheckProperties.remove(attPath);
        }

    }

    abstract protected List<Validator> getValidators(ModelAttrAssoc modelAttribute, String attributeKey,
            boolean manageAlterable, AbstractEntity<?> entity);
}
