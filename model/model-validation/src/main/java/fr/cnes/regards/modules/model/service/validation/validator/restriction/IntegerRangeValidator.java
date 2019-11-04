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
package fr.cnes.regards.modules.model.service.validation.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import com.google.common.collect.Range;

import fr.cnes.regards.modules.model.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.model.dto.properties.IntegerArrayProperty;
import fr.cnes.regards.modules.model.dto.properties.IntegerIntervalProperty;
import fr.cnes.regards.modules.model.dto.properties.IntegerProperty;
import fr.cnes.regards.modules.model.service.validation.validator.AbstractPropertyValidator;

/**
 * Validate {@link IntegerProperty}, {@link IntegerArrayProperty} or {@link IntegerIntervalProperty} with a
 * {@link IntegerRangeRestriction}
 *
 * @author Marc Sordi
 *
 */
public class IntegerRangeValidator extends AbstractPropertyValidator {

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
        return clazz == IntegerProperty.class || clazz == IntegerArrayProperty.class
                || clazz == IntegerIntervalProperty.class;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget instanceof IntegerProperty) {
            validate((IntegerProperty) pTarget, pErrors);
        } else if (pTarget instanceof IntegerArrayProperty) {
            validate((IntegerArrayProperty) pTarget, pErrors);
        } else if (pTarget instanceof IntegerIntervalProperty) {
            validate((IntegerIntervalProperty) pTarget, pErrors);
        } else {
            rejectUnsupported(pErrors);
        }
    }

    public void validate(IntegerProperty pTarget, Errors pErrors) {
        checkRange(pTarget.getValue(), pErrors);
    }

    public void validate(IntegerArrayProperty pTarget, Errors pErrors) {
        for (Integer value : pTarget.getValue()) {
            checkRange(value, pErrors);
        }
    }

    public void validate(IntegerIntervalProperty pTarget, Errors pErrors) {
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
