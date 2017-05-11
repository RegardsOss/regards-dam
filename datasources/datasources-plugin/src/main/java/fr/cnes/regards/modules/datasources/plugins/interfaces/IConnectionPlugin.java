/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import javax.sql.DataSource;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;

/**
 * Class IConnectionPlugin
 *
 * Allows to manage a connection to a {@link DataSource}
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to manage a connection to a datasource")
public interface IConnectionPlugin {

    /**
     * The user name
     */
    static final String USER_PARAM = "user";

    /**
     * The user's password
     */
    static final String PASSWORD_PARAM = "password"; // NOSONAR

    /**
     * The databse's host
     */
    static final String DB_HOST_PARAM = "dbHost";

    /**
     * The databse's port
     */
    static final String DB_PORT_PARAM = "dbPort";

    /**
     * The database's name
     */
    static final String DB_NAME_PARAM = "dbName";

    /**
     * The databse's driver
     */
    static final String DRIVER_PARAM = "driver";

    /**
     * Test the connection
     *
     * @return true if the connection is active
     */
    boolean testConnection();

    /**
     * Close a connection
     */
    void closeConnection();

}
