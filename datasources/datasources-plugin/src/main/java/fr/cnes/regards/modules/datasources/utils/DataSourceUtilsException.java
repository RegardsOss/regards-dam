/* license_placeholder */
/*
 * VERSION-HISTORY
 *
 * VERSION : 1.0-SNAPSHOT : FR : FR-REGARDS-1 : 02/06/2015 : Creation
 *
 * END-VERSION-HISTORY
 */

package fr.cnes.regards.modules.datasources.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Exception for DAO utils package
 *
 * @author msordi
 * @since 1.0-SNAPSHOT
 */
public class DataSourceUtilsException extends Exception {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceUtilsException.class);

    /**
     * serialVersionUID field.
     *
     * @author CS
     * @since 1.0-SNAPSHOT
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     * Constructeur
     *
     * @param pMessage
     *            an error message
     * @since 1.0-SNAPSHOT
     */
    public DataSourceUtilsException(String pMessage) {
        super(pMessage);
        LOGGER.error(pMessage);
    }

    /**
     *
     * Constructeur
     *
     * @param pMessage
     *            an error message
     * @param pCause
     *            the exception
     * @since 1.0-SNAPSHOT
     */
    public DataSourceUtilsException(String pMessage, Throwable pCause) {
        super(pMessage, pCause);
        LOGGER.error(pMessage, pCause);
    }

    /**
     *
     * Constructor
     *
     * @param pCause
     *            the exception
     * @since 1.0-SNAPSHOT
     */
    public DataSourceUtilsException(Throwable pCause) {
        super(pCause);
    }
}
