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
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;

/**
 * Validate {@link StringAttribute} or {@link StringArrayAttribute} value with an {@link EnumerationRestriction}
 *
 * @author Marc Sordi
 *
 */
public class EnumerationValidator implements Validator {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumerationValidator.class);

    /**
     * Configured restriction
     */
    private final EnumerationRestriction restriction;

    public EnumerationValidator(EnumerationRestriction pRestriction) {
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return (pClazz == StringAttribute.class) || (pClazz == StringArrayAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // TODO
        restriction.getAcceptableValues();
        LOGGER.debug("Validate string or string array");
    }

}
