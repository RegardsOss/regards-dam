package fr.cnes.regards.modules.model.domain.attributes.restriction.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.model.domain.attributes.restriction.LongRangeRestriction;

/**
* Validate range
*
* @author oroussel
*/
public class CheckLongRangeValidator implements ConstraintValidator<CheckLongRange, LongRangeRestriction> {

    @Override
    public void initialize(CheckLongRange pConstraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(LongRangeRestriction pValue, ConstraintValidatorContext pContext) {
        return pValue.getMin() < pValue.getMax();
    }
}
