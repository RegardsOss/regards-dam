/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.validator;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
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
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
@Constraint(validatedBy = {})
@Documented
public @interface CheckModel {

    String message() default "{fr.cnes.regards.modules.entities.validator." + "message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
