/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import org.springframework.validation.Errors;

/**
 * Validate required attribute
 *
 * @author Marc Sordi
 *
 */
public class RequiredAttributeValidator extends AbstractAttributeValidator {

    public RequiredAttributeValidator(String pAttributeKey) {
        super(pAttributeKey);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget == null) {
            pErrors.rejectValue(attributeKey, "error.attribute.required.message", "Attribute required.");
        }
    }

}
