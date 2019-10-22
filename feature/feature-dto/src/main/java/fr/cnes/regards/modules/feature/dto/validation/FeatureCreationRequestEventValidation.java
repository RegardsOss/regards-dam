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
        // We will seek invalid cases, there are 3 cases
        // When there are metadata but they aren't valid
        if (value.getMetadata().hasStorage()) {
            Errors errors = new MapBindingResult(new HashMap<>(), FeatureCreationRequestEvent.class.getName());
            validator.validate(value.getMetadata(), errors);
            return !errors.hasErrors();
        }
        // When there are no metada and no files
        if (value.getFeature().getFiles().isEmpty()) {
            context.buildConstraintViolationWithTemplate("There are no metada and no files").addConstraintViolation();
            return false;
        }

        // When there are no metadata and files without locations
        boolean valid = !value.getFeature().getFiles().stream()
                .anyMatch(file -> file.getLocations().stream().anyMatch(loc -> loc.getStorage() == null));
        if (!valid) {
            context.buildConstraintViolationWithTemplate("There are no metadata and files without locations")
                    .addConstraintViolation();
        }
        return valid;
    }
}
