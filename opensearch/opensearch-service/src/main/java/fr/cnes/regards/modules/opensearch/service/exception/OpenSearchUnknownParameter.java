/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 *
 * Throws when a query parameter is unknown.
 * @author Marc Sordi
 *
 */
@SuppressWarnings("serial")
public class OpenSearchUnknownParameter extends ModuleException {

    /**
     * @param pErrorMessage
     */
    public OpenSearchUnknownParameter(String pErrorMessage) {
        super(pErrorMessage);
    }

}
