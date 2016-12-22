/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.service.validator.AbstractAttributeValidator;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;

/**
 * Validate {@link StringAttribute} or {@link StringArrayAttribute} value with a {@link PatternRestriction}
 *
 * @author Marc Sordi
 *
 */
public class PatternValidator extends AbstractAttributeValidator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternValidator.class);

    /**
     * Configured restriction
     */
    private final PatternRestriction restriction;

    public PatternValidator(PatternRestriction pRestriction, String pAttributeKey) {
        super(pAttributeKey);
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return (pClazz == StringAttribute.class) || (pClazz == StringArrayAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        pErrors.rejectValue(attributeKey, "error.inconsistent.attribute.type");
    }

    public void validate(StringAttribute pTarget, Errors pErrors) {
        // TODO TEST match
    }

    public void validate(StringArrayAttribute pTarget, Errors pErrors) {
        // TODO TEST match
    }

    private void reject(Errors pErrors) {
        // TODO
        pErrors.rejectValue(attributeKey, "error.enum.value.does.not.exist", "Value not acceptable.");
    }

}
