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
package fr.cnes.regards.modules.dam.service.entities;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.feature.DataObjectFeature;
import fr.cnes.regards.modules.dam.service.entities.validation.AbstractEntityValidationService;
import fr.cnes.regards.modules.model.service.validation.IModelFinder;
import fr.cnes.regards.modules.model.service.validation.ValidationMode;

/**
 * Specific EntityService for data objects.
 * By now it concerns only data object validation.
 * <b>NOTE : this service is not transactional because data objects are not persisted into database, only
 * ElasticSearch</b>
 * @author oroussel
 */
@Service
public class DataObjectService extends AbstractEntityValidationService<DataObjectFeature, DataObject> {

    private final Validator dataObjectValidator;

    public DataObjectService(IModelFinder modelFinder, Validator dataObjectValidator) {
        super(modelFinder);
        this.dataObjectValidator = dataObjectValidator;
    }

    @Override
    public void validate(DataObject entity, Errors inErrors, ValidationMode mode) throws EntityInvalidException {
        // First validate data object regarding its annotations
        this.dataObjectValidator.validate(entity, inErrors);
        // Then validate its associated attributes using inherited validation service
        super.validate(entity, inErrors, mode);
    }
}
