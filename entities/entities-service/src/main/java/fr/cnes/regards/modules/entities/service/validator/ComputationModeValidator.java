/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import org.springframework.validation.Errors;

import fr.cnes.regards.modules.models.domain.ComputationMode;

/**
 * Validate computation mode
 *
 * @author Marc Sordi
 *
 */
public class ComputationModeValidator extends AbstractAttributeValidator {

    /**
     * {@link ComputationMode}
     */
    private final ComputationMode computationMode;

    public ComputationModeValidator(ComputationMode pComputationMode, String pAttributeKey) {
        super(pAttributeKey);
        this.computationMode = pComputationMode;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        if (pTarget == null) {
            return;
        }

        if (!ComputationMode.GIVEN.equals(computationMode)) {
            pErrors.rejectValue(attributeKey, "error.computed.attribute.given.message",
                                "Computed attribute value must not be set.");
        }
    }

}
