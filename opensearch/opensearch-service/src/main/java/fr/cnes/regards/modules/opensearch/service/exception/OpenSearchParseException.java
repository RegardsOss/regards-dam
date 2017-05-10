/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.exception;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;

/**
 * Thrown when an error occurs during the parsing of an OpenSearch request.
 * @author Xavier-Alexandre Brochard
 */
public class OpenSearchParseException extends ModuleException {

    /**
     * @param pMessage the message
     */
    public OpenSearchParseException(String pMessage) {
        super(pMessage);
    }

    /**
     * @param pCause the caught exception which triggered this exception
     */
    public OpenSearchParseException(Throwable pCause) {
        super("An error occured while parsing the OpenSearch request", pCause);
    }

    /**
     * @param pMessage the message
     * @param pCause the caught exception which triggered this exception
     */
    public OpenSearchParseException(String pMessage, Throwable pCause) {
        super(pMessage, pCause);
    }

}
