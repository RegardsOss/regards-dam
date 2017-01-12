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
        if (!ComputationMode.GIVEN.equals(computationMode)) {
            pErrors.reject("error.computed.attribute.given.message",
                           String.format("Computed value for attribute \"%s\" must not be set.", attributeKey));
        }
    }

}
