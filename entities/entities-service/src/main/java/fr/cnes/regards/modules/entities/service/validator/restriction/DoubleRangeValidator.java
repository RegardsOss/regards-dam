/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import fr.cnes.regards.modules.entities.domain.attribute.DoubleArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.value.Interval;
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
    public boolean supports(Class<?> pClazz) {
        return (pClazz == DoubleAttribute.class) || (pClazz == DoubleArrayAttribute.class)
                || (pClazz == DoubleIntervalAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        pErrors.rejectValue(attributeKey, INCONSISTENT_ATTRIBUTE);
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
        Interval<Double> interval = pTarget.getValue();
        checkRange(interval.getLowerBound(), pErrors);
        checkRange(interval.getUpperBound(), pErrors);
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
        pErrors.rejectValue(attributeKey, "error.double.value.not.in.required.range",
                            "Value not constistent with restriction range.");
    }
}
