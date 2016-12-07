/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator.restriction;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 *
 * Occurs when a restriction validator cannot be found.
 *
 * @author Marc Sordi
 *
 */
public class UnknownRestrictionValidatorException extends ModuleException {

    private static final long serialVersionUID = -8464454977943797858L;

    public UnknownRestrictionValidatorException(String pErrorMessage) {
        super(pErrorMessage);
    }
}
