/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;

import fr.cnes.regards.modules.entities.domain.attribute.FloatArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.FloatAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.FloatIntervalAttribute;
import fr.cnes.regards.modules.entities.service.validator.AbstractAttributeValidator;
import fr.cnes.regards.modules.models.domain.attributes.restriction.FloatRangeRestriction;

/**
 * Validate {@link FloatAttribute}, {@link FloatArrayAttribute} or {@link FloatIntervalAttribute} value with a
 * {@link FloatRangeRestriction}
 *
 * @author Marc Sordi
 *
 */
public class FloatRangeValidator extends AbstractAttributeValidator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(FloatRangeValidator.class);

    /**
     * Configured restriction
     */
    private final FloatRangeRestriction restriction;

    public FloatRangeValidator(FloatRangeRestriction pRestriction, String pAttributeKey) {
        super(pAttributeKey);
        this.restriction = pRestriction;
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return (pClazz == FloatAttribute.class) || (pClazz == FloatArrayAttribute.class)
                || (pClazz == FloatIntervalAttribute.class);
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // TODO Auto-generated method stub
    }

}
