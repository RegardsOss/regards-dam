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
package fr.cnes.regards.modules.entities.service.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;

/**
 * @author Marc Sordi
 *
 */
public abstract class AbstractAttributeValidator implements Validator {

    /**
     * Attribute key
     */
    protected final String attributeKey;

    public AbstractAttributeValidator(String pAttributeKey) {
        this.attributeKey = pAttributeKey;
    }

    protected void rejectUnsupported(Errors pErrors) {
        pErrors.reject("error.unsupported.attribute.type.message", String
                .format("Unsupported attribute \"%s\" for validator \"%s\".", attributeKey, this.getClass().getName()));
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return AbstractAttribute.class.isAssignableFrom(pClazz);
    }
}
