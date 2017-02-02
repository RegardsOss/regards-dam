/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.sql.Connection;

import javax.sql.DataSource;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;

/**
 * Class IDBConnectionPlugin
 *
 * Allows to manage a {@link DataSource} connection pool
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to manager a datasource connection pool")
public interface IDBConnectionPlugin extends IConnectionPlugin {

    static final String MAX_POOLSIZE_PARAM = "maxPoolSize";

    static final String MIN_POOLSIZE_PARAM = "minPoolSize";

    /**
     * Retrieve a {@link Connection} to a database
     * 
     * @return the {@link Connection}
     */
    Connection getConnection();

}
