/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 *
 * Check geometry
 *
 * @author Marc Sordi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Constraint(validatedBy = { CheckGeometryValidator.class })
@Documented
public @interface CheckGeometry {

    /**
     * @return error message key
     */
    String message() default "{fr.cnes.regards.modules.entities.validator.CheckGeometry.message}";

    /**
     *
     * @return validation groups
     */
    Class<?>[] groups() default {};

    /**
     * @return custom payload object
     */
    Class<? extends Payload>[] payload() default {};
}
