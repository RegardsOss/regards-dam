/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Marc Sordi
 *
 */
public class UrlValidator implements Validator {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> pClazz) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // TODO Auto-generated method stub

    }

}
