/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;

/**
 * Validate {@link StringAttribute} or {@link StringArrayAttribute} value with a {@link PatternRestriction}
 *
 * @author Marc Sordi
 *
 */
public class PatternValidator implements Validator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternValidator.class);

    /**
     * Configured restriction
     */
    private final PatternRestriction restriction;

    public PatternValidator(PatternRestriction pRestriction) {
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return (pClazz == StringAttribute.class) || (pClazz == StringArrayAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // TODO check pattern matches

    }

}
