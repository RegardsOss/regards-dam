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
package fr.cnes.regards.modules.model.service.validation;

import java.util.List;
import java.util.Set;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.model.dto.properties.AbstractProperty;

/**
 * Validate properties according to model definition
 *
 * @author Marc SORDI
 */
public interface IModelValidationService {

    /**
     * Validate a set of properties
     * @param model related model
     * @param properties properties to validate
     * @param extraValidators optional extra validators
     */
    Errors validate(String model, Set<AbstractProperty<?>> properties, List<Validator> extraValidators);
}