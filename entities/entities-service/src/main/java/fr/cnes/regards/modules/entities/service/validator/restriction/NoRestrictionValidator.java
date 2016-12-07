/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * No restriction validator
 *
 * @author Marc Sordi
 *
 */
public class NoRestrictionValidator implements Validator {

    @Override
    public boolean supports(Class<?> pClazz) {

        return true;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // Nothing to do
    }

}
