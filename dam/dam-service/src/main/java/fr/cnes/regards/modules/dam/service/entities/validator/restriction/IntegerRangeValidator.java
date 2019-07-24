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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import com.google.common.collect.Range;

import fr.cnes.regards.modules.dam.domain.entities.attribute.IntegerArrayAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.IntegerAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.dam.service.entities.validator.AbstractAttributeValidator;

/**
 * Validate {@link IntegerAttribute}, {@link IntegerArrayAttribute} or {@link IntegerIntervalAttribute} with a
 * {@link IntegerRangeRestriction}
 *
 * @author Marc Sordi
 *
 */
public class IntegerRangeValidator extends AbstractAttributeValidator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegerRangeValidator.class);

    /**
     * Configured restriction
     */
    private final IntegerRangeRestriction restriction;

    public IntegerRangeValidator(IntegerRangeRestriction pRestriction, String pAttributeKey) {
        super(pAttributeKey);
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return (clazz == IntegerAttribute.class) || (clazz == IntegerArrayAttribute.class)
                || (clazz == IntegerIntervalAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget instanceof IntegerAttribute) {
            validate((IntegerAttribute) pTarget, pErrors);
        } else
            if (pTarget instanceof IntegerArrayAttribute) {
                validate((IntegerArrayAttribute) pTarget, pErrors);
            } else
                if (pTarget instanceof IntegerIntervalAttribute) {
                    validate((IntegerIntervalAttribute) pTarget, pErrors);
                } else {
                    rejectUnsupported(pErrors);
                }
    }

    public void validate(IntegerAttribute pTarget, Errors pErrors) {
        checkRange(pTarget.getValue(), pErrors);
    }

    public void validate(IntegerArrayAttribute pTarget, Errors pErrors) {
        for (Integer value : pTarget.getValue()) {
            checkRange(value, pErrors);
        }
    }

    public void validate(IntegerIntervalAttribute pTarget, Errors pErrors) {
        Range<Integer> range = pTarget.getValue();
        checkRange(range.lowerEndpoint(), pErrors);
        checkRange(range.upperEndpoint(), pErrors);
    }

    /**
     * Check value is in restriction range
     *
     * @param pValue
     *            value
     * @param pErrors
     *            errors
     */
    private void checkRange(Integer pValue, Errors pErrors) {
        if (restriction.isMinExcluded()) {
            if (pValue <= restriction.getMin()) {
                reject(pErrors);
            }
        } else {
            if (pValue < restriction.getMin()) {
                reject(pErrors);
            }
        }
        if (restriction.isMaxExcluded()) {
            if (pValue >= restriction.getMax()) {
                reject(pErrors);
            }
        } else {
            if (pValue > restriction.getMax()) {
                reject(pErrors);
            }
        }

    }

    private void reject(Errors pErrors) {
        pErrors.rejectValue(attributeKey, "error.integer.value.not.in.required.range",
                            "Value not constistent with restriction range.");
    }
}
