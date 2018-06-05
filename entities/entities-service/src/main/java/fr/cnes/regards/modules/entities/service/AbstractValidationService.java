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
package fr.cnes.regards.modules.entities.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.models.domain.ComputationMode;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;

/**
 * @author oroussel
 */
public abstract class AbstractValidationService<U extends AbstractEntity> implements IValidationService<U> {
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

        // Check model not empty
        if (CollectionUtils.isEmpty(modAtts) && !CollectionUtils.isEmpty(entity.getProperties())) {
            inErrors.rejectValue("properties", "error.no.properties.defined.but.set",
                                 "No properties defined in corresponding model but trying to create.");
        }

        // Loop over model attributes ... to validate each attribute
        for (ModelAttrAssoc modelAtt : modAtts) {
            checkModelAttribute(modelAtt, inErrors, manageAlterable, entity);
        }

        if (inErrors.hasErrors()) {
            List<String> errors = new ArrayList<>();
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
     */
    protected void checkModelAttribute(ModelAttrAssoc modelAttribute, Errors errors, boolean manageAlterable,
            AbstractEntity entity) {

        // only validate attribute that have a ComputationMode of GIVEN. Otherwise the attribute will most likely be
        // missing and is added during the crawling process
        if (ComputationMode.GIVEN.equals(modelAttribute.getMode())) {
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
                    String defaultMessage = String
                            .format("Unsupported validator \"%s\" for attribute \"%s\"", validator.getClass().getName(),
                                    attPath);
                    errors.reject("error.unsupported.validator.message", defaultMessage);
                }
            }
        }
    }

    abstract protected List<Validator> getValidators(ModelAttrAssoc modelAttribute, String attributeKey,
            boolean manageAlterable, AbstractEntity entity);
}