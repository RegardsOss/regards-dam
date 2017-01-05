/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.models.domain.attributes.restriction.DoubleRangeRestriction;

/**
 *
 * Validate range
 *
 * @author Marc Sordi
 *
 */
public class CheckFloatRangeValidator implements ConstraintValidator<CheckFloatRange, DoubleRangeRestriction> {

    @Override
    public void initialize(CheckFloatRange pConstraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(DoubleRangeRestriction pValue, ConstraintValidatorContext pContext) {
        return pValue.getMin() < pValue.getMax();
    }
}
