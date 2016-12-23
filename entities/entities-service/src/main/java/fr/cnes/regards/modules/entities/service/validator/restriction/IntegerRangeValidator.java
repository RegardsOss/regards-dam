/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import fr.cnes.regards.modules.entities.domain.attribute.IntegerArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.value.Interval;
import fr.cnes.regards.modules.entities.service.validator.AbstractAttributeValidator;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;

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
    public boolean supports(Class<?> pClazz) {
        return (pClazz == IntegerAttribute.class) || (pClazz == IntegerArrayAttribute.class)
                || (pClazz == IntegerIntervalAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        pErrors.rejectValue(attributeKey, INCONSISTENT_ATTRIBUTE);
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
        Interval<Integer> interval = pTarget.getValue();
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
