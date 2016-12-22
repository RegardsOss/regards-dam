/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.entities.domain.attribute.IntegerArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;

/**
 * Validate {@link IntegerAttribute}, {@link IntegerArrayAttribute} or {@link IntegerIntervalAttribute} with a
 * {@link IntegerRangeRestriction}
 *
 * @author Marc Sordi
 *
 */
public class IntegerRangeValidator implements Validator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegerRangeValidator.class);

    /**
     * Configured restriction
     */
    private final IntegerRangeRestriction restriction;

    public IntegerRangeValidator(IntegerRangeRestriction pRestriction) {
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return (pClazz == IntegerAttribute.class) || (pClazz == IntegerArrayAttribute.class)
                || (pClazz == IntegerIntervalAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // TODO check range
    }
}
