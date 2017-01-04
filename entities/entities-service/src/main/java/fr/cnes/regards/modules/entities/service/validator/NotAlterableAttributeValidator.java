/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import org.springframework.validation.Errors;

/**
 * Validate not alterable attribute
 *
 * @author Marc Sordi
 *
 */
public class NotAlterableAttributeValidator extends AbstractAttributeValidator {

    public NotAlterableAttributeValidator(String pAttributeKey) {
        super(pAttributeKey);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget != null) {
            pErrors.rejectValue(attributeKey, "error.attribute.not.alterable.message",
                                "Attribute not alterable must not be set.");
        }
    }

}
