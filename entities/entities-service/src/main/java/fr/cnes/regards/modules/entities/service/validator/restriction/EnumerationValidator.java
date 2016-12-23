/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.springframework.validation.Errors;

import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.service.validator.AbstractAttributeValidator;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;

/**
 * Validate {@link StringAttribute} or {@link StringArrayAttribute} value with an {@link EnumerationRestriction}
 *
 * @author Marc Sordi
 *
 */
public class EnumerationValidator extends AbstractAttributeValidator {

    /**
     * Configured restriction
     */
    private final EnumerationRestriction restriction;

    public EnumerationValidator(EnumerationRestriction pRestriction, String pAttributeKey) {
        super(pAttributeKey);
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return (pClazz == StringAttribute.class) || (pClazz == StringArrayAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        pErrors.rejectValue(attributeKey, INCONSISTENT_ATTRIBUTE);
    }

    public void validate(StringAttribute pTarget, Errors pErrors) {
        if (!restriction.getAcceptableValues().contains(pTarget.getValue())) {
            reject(pErrors);
        }
    }

    public void validate(StringArrayAttribute pTarget, Errors pErrors) {
        for (String val : pTarget.getValue()) {
            if (!restriction.getAcceptableValues().contains(val)) {
                reject(pErrors);
            }
        }
    }

    private void reject(Errors pErrors) {
        pErrors.rejectValue(attributeKey, "error.enum.value.does.not.exist", "Value not acceptable.");
    }
}
