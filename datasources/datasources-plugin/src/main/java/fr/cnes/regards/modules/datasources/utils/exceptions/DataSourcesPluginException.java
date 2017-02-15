/* license_placeholder */
/*
 * VERSION-HISTORY
 *
 * VERSION : 1.0-SNAPSHOT : FR : FR-REGARDS-1 : 02/06/2015 : Creation
 *
 * END-VERSION-HISTORY
 */

package fr.cnes.regards.modules.datasources.utils.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Exception for Datasources Plugin
 *
 * @author Christophe Mertz
 * 
 */
public class DataSourcesPluginException extends Exception {

    /**
     * Class Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourcesPluginException.class);

    /**
     * serialVersionUID field.
     */
    private static final long serialVersionUID = 3517283873163069739L;

    /**
     *
     * Constructor
     *
     * @param pMessage
     *            an error message
     */
    public DataSourcesPluginException(String pMessage) {
        super(pMessage);
        LOGGER.error(pMessage);
    }

    /**
     *
     * Constructor
     *
     * @param pMessage
     *            an error message
     * @param pCause
     *            the exception
     */
    public DataSourcesPluginException(String pMessage, Throwable pCause) {
        super(pMessage, pCause);
        LOGGER.error(pMessage, pCause);
    }

    /**
     *
     * Constructor
     *
     * @param pCause
     *            the exception
     */
    public DataSourcesPluginException(Throwable pCause) {
        super(pCause);
    }
}
