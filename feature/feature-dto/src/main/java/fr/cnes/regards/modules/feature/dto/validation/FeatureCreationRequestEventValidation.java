/**
 *
 */
package fr.cnes.regards.modules.feature.dto.validation;

import java.util.HashMap;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

/**
 * @author kevin
 *
 */
public class FeatureCreationRequestEventValidation
		implements ConstraintValidator<ValidFeatureEvent, FeatureCreationRequestEvent> {

	@Autowired
	private Validator validator;

	@Override
	public boolean isValid(FeatureCreationRequestEvent value, ConstraintValidatorContext context) {
		if ((value.getMetadata() != null) && !value.getMetadata().isEmpty()) {
			Errors errors = new MapBindingResult(new HashMap<>(), FeatureCreationRequestEvent.class.getName());
			validator.validate(value.getMetadata(), errors);
			return !errors.hasErrors();
		}
		if (value.getFeature().getFiles() == null) {
			return false;
		}
		return value.getFeature().getFiles().stream()
				.anyMatch(file -> file.getLocations().stream().anyMatch(loc -> loc.getStorage() == null));
	}
}
