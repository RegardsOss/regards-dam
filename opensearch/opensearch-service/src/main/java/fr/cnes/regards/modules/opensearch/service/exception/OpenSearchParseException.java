/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.exception;

/**
 * Thrown when an error occurs during the parsing of an OpenSearch request.
 * @author Xavier-Alexandre Brochard
 */
public class OpenSearchParseException extends Exception {

    /**
     * @param pCause the caught exception which triggered this exception
     */
    public OpenSearchParseException(Throwable pCause) {
        super("An error occured while parsing the OpenSearch request", pCause);
    }

}
