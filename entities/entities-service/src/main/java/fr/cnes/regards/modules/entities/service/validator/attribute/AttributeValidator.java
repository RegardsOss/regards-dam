/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.attribute;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Marc Sordi
 *
 */
public class AttributeValidator implements Validator {

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
