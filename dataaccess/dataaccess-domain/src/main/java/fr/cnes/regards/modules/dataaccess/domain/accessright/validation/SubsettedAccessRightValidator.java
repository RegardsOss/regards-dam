/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.validation;

import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class SubsettedAccessRightValidator implements ConstraintValidator<SubsettedAccessRight, AbstractAccessRight> {

    @Override
    public void initialize(SubsettedAccessRight pConstraintAnnotation) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(AbstractAccessRight pValue, ConstraintValidatorContext pContext) {
        if (pValue == null) {
            return true;
        }
        Set<AttributeModel> subsettingCriteria = pValue.getSubsettingCriteria();
        return onlyContainsSearchCritera(subsettingCriteria);
    }

    /**
     * @param pSubsettingCriteria
     * @return
     */
    private boolean onlyContainsSearchCritera(Set<AttributeModel> pSubsettingCriteria) {
        Iterator<AttributeModel> iterator = pSubsettingCriteria.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isQueryable()) {
                return false;
            }
        }
        return true;
    }

}
