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

import fr.cnes.regards.modules.model.domain.attributes.restriction.DoubleRangeRestriction;
import fr.cnes.regards.modules.model.dto.properties.DoubleArrayProperty;
import fr.cnes.regards.modules.model.dto.properties.DoubleIntervalProperty;
import fr.cnes.regards.modules.model.dto.properties.DoubleProperty;
import fr.cnes.regards.modules.model.service.validation.validator.AbstractPropertyValidator;

/**
 * Validate {@link DoubleProperty}, {@link DoubleArrayProperty} or {@link DoubleIntervalProperty} value with a
 * {@link DoubleRangeRestriction}
 *
 * @author Marc Sordi
 *
 */
public class DoubleRangeValidator extends AbstractPropertyValidator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleRangeValidator.class);

    /**
     * Configured restriction
     */
    private final DoubleRangeRestriction restriction;

    public DoubleRangeValidator(DoubleRangeRestriction pRestriction, String pAttributeKey) {
        super(pAttributeKey);
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == DoubleProperty.class || clazz == DoubleArrayProperty.class
                || clazz == DoubleIntervalProperty.class;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget instanceof DoubleProperty) {
            validate((DoubleProperty) pTarget, pErrors);
        } else if (pTarget instanceof DoubleArrayProperty) {
            validate((DoubleArrayProperty) pTarget, pErrors);
        } else if (pTarget instanceof DoubleIntervalProperty) {
            validate((DoubleIntervalProperty) pTarget, pErrors);
        } else {
            rejectUnsupported(pErrors);
        }
    }

    public void validate(DoubleProperty pTarget, Errors pErrors) {
        checkRange(pTarget.getValue(), pErrors);
    }

    public void validate(DoubleArrayProperty pTarget, Errors pErrors) {
        for (Double value : pTarget.getValue()) {
            checkRange(value, pErrors);
        }
    }

    public void validate(DoubleIntervalProperty pTarget, Errors pErrors) {
        // Interval<Double> interval = pTarget.getValue();
        Range<Double> interval = pTarget.getValue();
        // checkRange(interval.getLowerBound(), pErrors);
        // checkRange(interval.getUpperBound(), pErrors);
        checkRange(interval.lowerEndpoint(), pErrors);
        checkRange(interval.upperEndpoint(), pErrors);
    }

    /**
     * Check value is in restriction range
     *
     * @param pValue
     *            value
     * @param pErrors
     *            errors
     */
    private void checkRange(Double pValue, Errors pErrors) {
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
        pErrors.reject("error.double.value.not.in.required.range", String
                .format("Value not consistent with restriction range for attribute \"%s\".", attributeKey));
    }
}
