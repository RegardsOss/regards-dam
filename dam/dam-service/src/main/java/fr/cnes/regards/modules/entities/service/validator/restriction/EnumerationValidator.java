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
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.springframework.validation.Errors;

import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.service.validator.AbstractAttributeValidator;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;

/**
 * Validate {@link StringAttribute} or {@link StringArrayAttribute} value with an {@link EnumerationRestriction}
 *
 * @author Marc Sordi
 *
 */
public class EnumerationValidator extends AbstractAttributeValidator {

    /**
     * Configured restriction
     */
    private final EnumerationRestriction restriction;

    public EnumerationValidator(EnumerationRestriction pRestriction, String pAttributeKey) {
        super(pAttributeKey);
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return (clazz == StringAttribute.class) || (clazz == StringArrayAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget instanceof StringAttribute) {
            validate((StringAttribute) pTarget, pErrors);
        } else
            if (pTarget instanceof StringArrayAttribute) {
                validate((StringArrayAttribute) pTarget, pErrors);
            } else {
                rejectUnsupported(pErrors);
            }
    }

    public void validate(StringAttribute pTarget, Errors pErrors) {
        if (!restriction.getAcceptableValues().contains(pTarget.getValue())) {
            reject(pErrors);
        }
    }

    public void validate(StringArrayAttribute pTarget, Errors pErrors) {
        for (String val : pTarget.getValue()) {
            if (!restriction.getAcceptableValues().contains(val)) {
                reject(pErrors);
            }
        }
    }

    private void reject(Errors pErrors) {
        pErrors.reject("error.enum.value.does.not.exist",
                       String.format("Value not acceptable for attribute \"%s\".", attributeKey));
    }
}
