/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import org.springframework.validation.Errors;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Validate attribute type
 *
 * @author Marc Sordi
 *
 */
public class AttributeTypeValidator extends AbstractAttributeValidator {

    /**
     * {@link AttributeType}
     */
    private final AttributeType attributeType;

    public AttributeTypeValidator(AttributeType pAttributeType, String pAttributeKey) {
        super(pAttributeKey);
        this.attributeType = pAttributeType;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        AbstractAttribute<?> att = (AbstractAttribute<?>) pTarget;
        if (!att.represents(attributeType)) {
            pErrors.reject("error.inconsistent.attribute.type.message",
                           String.format("Attribute \"%s\" not consistent with model attribute type.", attributeKey));

        }
    }
}