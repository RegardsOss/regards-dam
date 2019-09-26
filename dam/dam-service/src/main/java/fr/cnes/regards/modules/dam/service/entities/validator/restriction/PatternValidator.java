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
package fr.cnes.regards.modules.dam.service.entities.validator.restriction;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import fr.cnes.regards.modules.dam.service.entities.validator.AbstractPropertyValidator;
import fr.cnes.regards.modules.model.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.model.dto.properties.StringArrayProperty;
import fr.cnes.regards.modules.model.dto.properties.StringProperty;

/**
 * Validate {@link StringProperty} or {@link StringArrayProperty} value with a {@link PatternRestriction}
 *
 * @author Marc Sordi
 *
 */
public class PatternValidator extends AbstractPropertyValidator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternValidator.class);

    /**
     * Configured restriction
     */
    private final PatternRestriction restriction;

    public PatternValidator(PatternRestriction pRestriction, String pAttributeKey) {
        super(pAttributeKey);
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == StringProperty.class || clazz == StringArrayProperty.class;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget instanceof StringProperty) {
            validate((StringProperty) pTarget, pErrors);
        } else if (pTarget instanceof StringArrayProperty) {
            validate((StringArrayProperty) pTarget, pErrors);
        } else {
            rejectUnsupported(pErrors);
        }
    }

    public void validate(StringProperty pTarget, Errors pErrors) {
        if (!Pattern.matches(restriction.getPattern(), pTarget.getValue())) {
            reject(pErrors);
        }
    }

    public void validate(StringArrayProperty pTarget, Errors pErrors) {
        for (String val : pTarget.getValue()) {
            if (!Pattern.matches(restriction.getPattern(), val)) {
                reject(pErrors);
            }
        }
    }

    private void reject(Errors pErrors) {
        pErrors.reject("error.value.not.conform.to.pattern", String
                .format("Value of attribute %s is not conform to pattern %s.", attributeKey, restriction.getPattern()));

    }

}
