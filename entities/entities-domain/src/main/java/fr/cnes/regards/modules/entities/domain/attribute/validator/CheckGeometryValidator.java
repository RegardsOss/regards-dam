/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.entities.domain.attribute.GeometryAttribute;

/**
 *
 * Validate supported geometry
 *
 * @author Marc Sordi
 *
 */
public class CheckGeometryValidator implements ConstraintValidator<CheckGeometry, GeometryAttribute> {

    @Override
    public void initialize(CheckGeometry pConstraintAnnotation) {
        // Nothing to do
    }

    @Override
    public boolean isValid(GeometryAttribute pValue, ConstraintValidatorContext pContext) {
        // FIXME control supported geometry
        return true;
    }
}
