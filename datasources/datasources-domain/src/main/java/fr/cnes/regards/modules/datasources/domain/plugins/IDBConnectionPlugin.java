/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnes.regards.modules.datasources.domain.plugins;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.Table;

/**
 * Allows to manage a connection pool to a {@link DataSource}.
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to manage a connection pool to a datasource")
public interface IDBConnectionPlugin extends IConnectionPlugin {

    /**
     * User name
     */
    String USER_PARAM = "user";

    /**
     * User password
     */
    String PASSWORD_PARAM = "password"; // NOSONAR

    /**
     * Database host
     */
    String DB_HOST_PARAM = "dbHost";

    /**
     * Database port
     */
    String DB_PORT_PARAM = "dbPort";

    /**
     * Database name
     */
    String DB_NAME_PARAM = "dbName";

    /**
     * Databse driver
     */
    String DRIVER_PARAM = "driver";

    /**
     * Retrieve a {@link Connection} to a database
     * @return the {@link Connection}
     * @throws SQLException the {@link Connection} is not available
     */
    Connection getConnection() throws SQLException;

    /**
     * Requests the database to get the tables of a data source.
     * @param schemaPattern schema pattern or null
     * @param tableNamePattern table name pattern or null
     * @return a {@link Map} of the database's table
     */
    Map<String, Table> getTables(String schemaPattern, String tableNamePattern);

    /**
     * Requests the database to get the columns of a specific table.
     * @param tableName the database's table name
     * @return a {@link Map} of the columns
     */
    Map<String, Column> getColumns(String tableName);

}
