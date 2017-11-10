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
    String USER_PARAM = "user";

    /**
     * The user's password
     */
    String PASSWORD_PARAM = "password"; // NOSONAR

    /**
     * The databse's host
     */
    String DB_HOST_PARAM = "dbHost";

    /**
     * The databse's port
     */
    String DB_PORT_PARAM = "dbPort";

    /**
     * The database's name
     */
    String DB_NAME_PARAM = "dbName";

    /**
     * The databse's driver
     */
    String DRIVER_PARAM = "driver";

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
