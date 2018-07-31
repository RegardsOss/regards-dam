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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import com.google.common.collect.Range;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleIntervalAttribute;
import fr.cnes.regards.modules.entities.service.validator.AbstractAttributeValidator;
import fr.cnes.regards.modules.models.domain.attributes.restriction.DoubleRangeRestriction;

/**
 * Validate {@link DoubleAttribute}, {@link DoubleArrayAttribute} or {@link DoubleIntervalAttribute} value with a
 * {@link DoubleRangeRestriction}
 *
 * @author Marc Sordi
 *
 */
public class DoubleRangeValidator extends AbstractAttributeValidator {

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
        return (clazz == DoubleAttribute.class) || (clazz == DoubleArrayAttribute.class)
                || (clazz == DoubleIntervalAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget instanceof DoubleAttribute) {
            validate((DoubleAttribute) pTarget, pErrors);
        } else
            if (pTarget instanceof DoubleArrayAttribute) {
                validate((DoubleArrayAttribute) pTarget, pErrors);
            } else
                if (pTarget instanceof DoubleIntervalAttribute) {
                    validate((DoubleIntervalAttribute) pTarget, pErrors);
                } else {
                    rejectUnsupported(pErrors);
                }
    }

    public void validate(DoubleAttribute pTarget, Errors pErrors) {
        checkRange(pTarget.getValue(), pErrors);
    }

    public void validate(DoubleArrayAttribute pTarget, Errors pErrors) {
        for (Double value : pTarget.getValue()) {
            checkRange(value, pErrors);
        }
    }

    public void validate(DoubleIntervalAttribute pTarget, Errors pErrors) {
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
