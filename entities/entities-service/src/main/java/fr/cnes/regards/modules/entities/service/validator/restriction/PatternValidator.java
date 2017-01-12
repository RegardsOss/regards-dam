/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import java.util.regex.Pattern;

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
        if (pTarget instanceof StringAttribute) {
            validate((StringAttribute) pTarget, pErrors);
        } else
            if (pTarget instanceof StringArrayAttribute) {
                validate((StringArrayAttribute) pTarget, pErrors);
            } else {
                rejectUnsupported(pErrors);
            }
    }

    public void validate(StringAttribute pTarget, Errors pErrors) {
        if (!Pattern.matches(restriction.getPattern(), pTarget.getValue())) {
            reject(pErrors);
        }
    }

    public void validate(StringArrayAttribute pTarget, Errors pErrors) {
        for (String val : pTarget.getValue()) {
            if (!Pattern.matches(restriction.getPattern(), val)) {
                reject(pErrors);
            }
        }
    }

    private void reject(Errors pErrors) {
        pErrors.rejectValue(attributeKey, "error.value.not.conform.to.pattern",
                            String.format("Value not conform to pattern %s.", restriction.getPattern()));
    }

}
