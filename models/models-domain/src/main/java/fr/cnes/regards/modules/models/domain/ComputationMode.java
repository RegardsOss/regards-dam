/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain;

/**
 * Computation mode to set in model for an attribute.<br/>
 * Default to {@link ComputationMode} This computation mode is only available for collection and dataset model
 * attributes.
 *
 * @author Marc Sordi
 *
 */
public enum ComputationMode {

    /**
     * Value must be given explicitly to create the entity / no computation task is applied!
     */
    GIVEN,

    /**
     * Value is computed according to children data. Same attribute has to be set on children models. Default behaviour
     * is applied according to attribute type.
     */
    FROM_DESCENDANTS,

    /**
     * Value is computed according to a custom computation plugin.<br/>
     * TODO : interface
     */
    CUSTOM;
}
