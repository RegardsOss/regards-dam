/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.entities.domain.attribute.GeometryAttribute;

/**
 *
 * TODO
 *
 * @author Marc Sordi
 *
 */
public class CheckGeometryValidator implements ConstraintValidator<CheckGeometry, GeometryAttribute> {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckGeometryValidator.class);

    @Override
    public void initialize(CheckGeometry pConstraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(GeometryAttribute pValue, ConstraintValidatorContext pContext) {
        LOGGER.debug("Validating geometry");
        return false;
    }
}
