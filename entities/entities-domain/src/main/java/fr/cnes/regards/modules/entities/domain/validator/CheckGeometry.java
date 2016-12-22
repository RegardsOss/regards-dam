/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 *
 * Custom model validation annotation
 *
 * @author Marc Sordi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Constraint(validatedBy = { CheckGeometryValidator.class })
@Documented
public @interface CheckGeometry {

    String message() default "{fr.cnes.regards.modules.entities.validator.CheckGeometry.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
