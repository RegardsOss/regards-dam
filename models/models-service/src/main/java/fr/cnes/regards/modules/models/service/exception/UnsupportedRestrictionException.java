/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 *
 * Error that occurs if attribute does not support affected restriction
 *
 * @author Marc Sordi
 *
 */
public class UnsupportedRestrictionException extends ModuleException {

    private static final long serialVersionUID = -6004577784330115012L;

    public UnsupportedRestrictionException(String pErrorMessage) {
        super(pErrorMessage);
    }
}
