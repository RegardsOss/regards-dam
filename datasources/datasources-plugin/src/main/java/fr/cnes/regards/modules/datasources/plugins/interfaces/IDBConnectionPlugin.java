/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

package fr.cnes.regards.modules.datasources.plugins.interfaces;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.Table;

/**
 * Allows to manage a connection pool to a {@link DataSource}.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@PluginInterface(description = "Plugin to manage a connection pool to a datasource")
public interface IDBConnectionPlugin extends IConnectionPlugin {

    /**
     * The max size of the pool
     */
    String MAX_POOLSIZE_PARAM = "maxPoolSize";

    /**
     * The min size of the pool
     */
    String MIN_POOLSIZE_PARAM = "minPoolSize";

    /**
     * Retrieve a {@link Connection} to a database
     * 
     * @return the {@link Connection}
     * 
     * @throws SQLException
     *             the {@link Connection} is not available
     */
    Connection getConnection() throws SQLException;

    /**
     * Requests the database to get the tables of a data source.
     * 
     * @return a {@link Map} of the database's table
     */
    Map<String, Table> getTables();

    /**
     * Requests the database to get the columns of a specific table.
     * 
     * @param pTableName
     *            the database's table name
     * @return a {@link Map} of the columns
     * 
     */
    Map<String, Column> getColumns(String pTableName);

}
