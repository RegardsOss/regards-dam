/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.entities.domain.attribute.GeometryAttribute;

/**
 *
 * Validate {@link GeometryAttribute}
 *
 * @author Marc Sordi
 *
 */
public class GeometryValidator implements Validator {

    /**
     * Class logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryValidator.class);

    @Override
    public boolean supports(Class<?> pClazz) {
        return pClazz == GeometryAttribute.class;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        // TODO check supported shape type : polygon, linestring, point, etc.
    }
}
