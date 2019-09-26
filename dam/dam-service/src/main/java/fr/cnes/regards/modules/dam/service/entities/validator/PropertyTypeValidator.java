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
package fr.cnes.regards.modules.dam.service.entities.validator;

import org.springframework.validation.Errors;

import fr.cnes.regards.modules.model.dto.properties.AbstractProperty;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Validate attribute type
 *
 * @author Marc Sordi
 *
 */
public class PropertyTypeValidator extends AbstractPropertyValidator {

    /**
     * {@link PropertyType}
     */
    private final PropertyType PropertyType;

    public PropertyTypeValidator(PropertyType PropertyType, String attributeKey) {
        super(attributeKey);
        this.PropertyType = PropertyType;
    }

    @Override
    public void validate(Object target, Errors errors) {
        AbstractProperty<?> att = (AbstractProperty<?>) target;
        if (!att.represents(PropertyType)) {
            errors.reject("error.inconsistent.attribute.type.message",
                          String.format("Attribute \"%s\" not consistent with model attribute type.", attributeKey));

        }
    }
}
