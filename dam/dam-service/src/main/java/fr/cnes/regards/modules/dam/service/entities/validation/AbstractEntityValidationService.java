/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.entities.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.validation.ErrorTranslator;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.feature.EntityFeature;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.service.validation.AbstractValidationService;
import fr.cnes.regards.modules.model.service.validation.IModelFinder;

/**
 * @author Marc SORDI
 *
 */
public abstract class AbstractEntityValidationService<F extends EntityFeature, U extends AbstractEntity<F>>
        extends AbstractValidationService<F> implements IEntityValidationService<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityValidationService.class);

    public AbstractEntityValidationService(IModelFinder modelFinder) {
        super(modelFinder);
    }

    @Override
    public void validate(U entity, Errors inErrors, boolean manageAlterable) throws EntityInvalidException {

        Assert.notNull(entity, "Entity must not be null.");

        Model model = entity.getModel();

        Assert.notNull(model, "Model must be set on entity in order to be validated.");
        Assert.notNull(model.getId(), "Model identifier must be specified.");

        // Validate dynamic model
        Errors modelErrors = validate(model.getName(), entity.getFeature(), manageAlterable);
        inErrors.addAllErrors(modelErrors);

        if (inErrors.hasErrors()) {
            ErrorTranslator.getErrors(inErrors).forEach(e -> LOGGER.error(e));
            throw new EntityInvalidException(ErrorTranslator.getErrorsAsString(inErrors));
        }
    }
}
