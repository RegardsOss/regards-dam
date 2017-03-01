/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.Table;

/**
 * Allows to manage a connection pool to a {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to manager a connection pool to a datasource")
public interface IDBConnectionPlugin extends IConnectionPlugin {

    /**
     * The max size of the pool  
     */
    static final String MAX_POOLSIZE_PARAM = "maxPoolSize";

    /**
     * The min size of the pool
     */
    static final String MIN_POOLSIZE_PARAM = "minPoolSize";

    /**
     * Retrieve a {@link Connection} to a database
     * 
     * @return the {@link Connection}
     */
    Connection getConnection();

    /**
     * Requests the database to get the tables of a data source.
     * 
     * @return a {@link Map} of the database's table
     */
    public Map<String, Table> getTables();

    /**
     * Requests the database to get the columns of a specific table.
     * 
     * @param pTableName
     *            the database's table name
     * @return a {@link Map} of the columns
     * 
     */
    public Map<String, Column> getColumns(String pTableName);

}
