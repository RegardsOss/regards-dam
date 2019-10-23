/**
 *
 */
package fr.cnes.regards.modules.feature.dto.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

/**
 *
 * Validate file feature consistency.
 * @author kevin
 *
 */
public class FeatureCreationRequestEventValidation
        implements ConstraintValidator<ValidFeatureEvent, FeatureCreationRequestEvent> {

    @Override
    public boolean isValid(FeatureCreationRequestEvent value, ConstraintValidatorContext context) {

        // If storage metadata exists, request is valid
        if (value.getMetadata().hasStorage()) {
            return true;
        }

        // If there are files and at least one does not have a storage, request is invalid!
        // A null storage id can only exist with storage metadata!
        if (value.getFeature().hasFiles()) {
            boolean valid = !value.getFeature().getFiles().stream()
                    .anyMatch(file -> file.getLocations().stream().anyMatch(loc -> loc.getStorage() == null));
            if (!valid) {
                context.buildConstraintViolationWithTemplate("There is no metadata and file without locations")
                        .addConstraintViolation();
            }
            return valid;
        }

        return true;
    }
}
