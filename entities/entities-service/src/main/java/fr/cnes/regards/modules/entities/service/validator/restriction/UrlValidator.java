/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.entities.domain.attribute.UrlAttribute;

/**
 * Validate {@link UrlAttribute}
 *
 * @author Marc Sordi
 *
 */
public class UrlValidator implements Validator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlValidator.class);

    @Override
    public boolean supports(Class<?> pClazz) {
        return pClazz == UrlAttribute.class;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // TODO check format
    }

}
