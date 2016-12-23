/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;

/**
 *
 * Validate range
 *
 * @author Marc Sordi
 *
 */
public class CheckIntegerRangeValidator implements ConstraintValidator<CheckIntegerRange, IntegerRangeRestriction> {

    @Override
    public void initialize(CheckIntegerRange pConstraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(IntegerRangeRestriction pValue, ConstraintValidatorContext pContext) {
        return pValue.getMin() < pValue.getMax();
    }
}
