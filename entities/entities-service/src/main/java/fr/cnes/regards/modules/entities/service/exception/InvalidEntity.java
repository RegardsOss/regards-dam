/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 *
 * Error that occurs when trying to add an entity that not fits its model.
 *
 * @author Marc Sordi
 *
 */
public class InvalidEntity extends ModuleException {

    private static final long serialVersionUID = -6004577784330115012L;

    public InvalidEntity(String pErrorMessage) {
        super(pErrorMessage);
    }
}
