/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

/**
 * Computation mode to set in model for an attribute.<br/>
 * Default to {@link ComputationMode#GIVEN}. {@link ComputationMode#COMPUTED} is only available for dataset model
 * attributes.
 *
 * @author Marc Sordi
 */
public enum ComputationMode {

    /**
     * Value must be given explicitly to create the entity / no computation task is applied!
     */
    GIVEN,

    /**
     * Value is computed according to a custom computation plugin.
     */
    COMPUTED;
}
