/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.urn.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Annotation allowing to Validate {@link UniformResourceName} thanks to {@link RegardsOaisUrnValidator}
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Constraint(validatedBy = RegardsOaisUrnValidator.class)
@Documented
public @interface RegardsOaisUrn {

    /**
     * Class to validate
     */
    static final String CLASS_NAME = "fr.cnes.regards.modules.entities.urn.validator.RegardsOaisUrn.";

    /**
     * @return error message key
     */
    String message() default "{" + CLASS_NAME + "message}";

    /**
     * @return validation groups
     */
    Class<?>[] groups() default {};

    /**
     * @return custom payload
     */
    Class<? extends Payload>[] payload() default {};
}
