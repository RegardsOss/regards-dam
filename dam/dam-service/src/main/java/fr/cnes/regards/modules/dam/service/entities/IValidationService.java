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

import org.springframework.validation.Errors;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;

/**
 * Validation interface (implemented by AbstractEntityService for (Collection|Dataset|Document)Service via
 * AbstractValidationService and DataObjectService
 * @author oroussel
 */
public interface IValidationService<U extends AbstractEntity<?>> {

    void validate(U pAbstractEntity, Errors pErrors, boolean pManageAlterable) throws EntityInvalidException;
}