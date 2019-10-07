/**
 *
 */
package fr.cnes.regards.modules.feature.dto.validation;

import javax.validation.Payload;

/**
 * @author kevin
 *
 */
public @interface ValidFeatureEvent {

	String CLASS_NAME = "fr.cnes.regards.modules.feature.dto.validation.ValidFeatureEvent.";

	String message() default "{" + CLASS_NAME + "message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
