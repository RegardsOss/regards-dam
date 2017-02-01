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

    static final String USER_PARAM = "user";

    static final String PWD_PARAM = "password";

    static final String URL_PARAM = "url";

    static final String DRIVER_PARAM = "driver";

    /**
     * Test the connection
     * 
     * @return true if the connection is active
     */
    boolean testConnection();

}
