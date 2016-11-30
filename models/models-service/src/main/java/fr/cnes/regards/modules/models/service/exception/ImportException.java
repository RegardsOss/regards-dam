/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 *
 * Error that occurs during XML import
 *
 * @author Marc Sordi
 *
 */
public class ImportException extends ModuleException {

    private static final long serialVersionUID = 3811461816893141264L;

    public ImportException(String pErrorMessage) {
        super(pErrorMessage);
    }
}
