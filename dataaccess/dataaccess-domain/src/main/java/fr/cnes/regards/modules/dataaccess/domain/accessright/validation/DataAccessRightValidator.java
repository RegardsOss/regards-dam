/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.dataaccess.domain.accessright.DataAccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.DataAccessRight;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class DataAccessRightValidator implements ConstraintValidator<DataAccessRightValidation, DataAccessRight> {

    @Override
    public void initialize(DataAccessRightValidation pConstraintAnnotation) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(DataAccessRight pValue, ConstraintValidatorContext pContext) {
        return (pValue == null) || !(pValue.getDataAccessLevel().equals(DataAccessLevel.CUSTOM_ACCESS)
                && (pValue.getPluginConfiguration() == null));
    }

}
