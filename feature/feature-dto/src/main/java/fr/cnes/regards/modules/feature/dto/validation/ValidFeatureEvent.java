/**
 *
 */
package fr.cnes.regards.modules.feature.dto.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * @author kevin
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FeatureCreationRequestEventValidation.class)
public @interface ValidFeatureEvent {

	String CLASS_NAME = "fr.cnes.regards.modules.feature.dto.validation.ValidFeatureEvent.";

	String message() default "{" + CLASS_NAME + "message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
