/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright.validation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Retention(RUNTIME)
@Target({ ElementType.TYPE })
@Constraint(validatedBy = DataAccessRightValidator.class)
@Documented
public @interface DataAccessRightValidation {

    static final String CLASS_NAME = "fr.cnes.regards.modules.dataaccess.domain.accessright.validation.DataAccessRightValidation.";

    String message() default "{" + CLASS_NAME + "message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
