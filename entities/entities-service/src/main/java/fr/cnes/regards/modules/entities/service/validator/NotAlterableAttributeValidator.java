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
        pErrors.reject("error.attribute.not.alterable.message",
                       String.format("Attribute \"%s\" not alterable must not be set.", attributeKey));
    }

}
