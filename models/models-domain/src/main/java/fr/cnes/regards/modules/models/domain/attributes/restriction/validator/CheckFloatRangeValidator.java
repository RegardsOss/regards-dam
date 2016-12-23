/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.models.domain.attributes.restriction.FloatRangeRestriction;

/**
 *
 * Validate range
 *
 * @author Marc Sordi
 *
 */
public class CheckFloatRangeValidator implements ConstraintValidator<CheckFloatRange, FloatRangeRestriction> {

    @Override
    public void initialize(CheckFloatRange pConstraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(FloatRangeRestriction pValue, ConstraintValidatorContext pContext) {
        return pValue.getMin() < pValue.getMax();
    }
}
