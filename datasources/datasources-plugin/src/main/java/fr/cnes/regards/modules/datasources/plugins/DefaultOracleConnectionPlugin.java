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

package fr.cnes.regards.modules.datasources.plugins;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginDestroy;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataSourceConnection;

/**
 * A default {@link Plugin} of type {@link IDBConnectionPlugin}.</br>
 * For the test of the connection :
 * @see http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "oracle-db-connection", version = "1.0-SNAPSHOT", description = "Connection to a Sql database",
        author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class DefaultOracleConnectionPlugin extends AbstractDataSourceConnection implements IDBConnectionPlugin {

    /**
     * The JDBC Oracle driver
     */
    private static final String ORACLE_JDBC_DRIVER = "oracle.jdbc.OracleDriver";

    /**
     * The user to used for the database connection
     */
    @PluginParameter(name = USER_PARAM)
    private String dbUser;

    /**
     * The user's password to used for the database connection
     */
    @PluginParameter(name = PASSWORD_PARAM)
    private String dbPassword;

    /**
     * The URL to the database's host
     */
    @PluginParameter(name = DB_HOST_PARAM)
    private String dbHost;

    /**
     * The PORT to the database's host
     */
    @PluginParameter(name = DB_PORT_PARAM)
    private String dbPort;

    /**
     * The NAME of the database
     */
    @PluginParameter(name = DB_NAME_PARAM)
    private String dbName;

    /**
     * Maximum number of Connections a pool will maintain at any given time.
     */
    @PluginParameter(name = MAX_POOLSIZE_PARAM)
    private Integer maxPoolSize;

    /**
     * Minimum number of Connections a pool will maintain at any given time.
     */
    @PluginParameter(name = MIN_POOLSIZE_PARAM)
    private Integer minPoolSize;

    /**
     * This class is used to initialize the {@link Plugin}
     */
    @PluginInit
    private void createPoolConnection() {
        createPoolConnection(dbUser, dbPassword, maxPoolSize, minPoolSize);
    }

    @PluginDestroy
    private void destroyPoolConnection() {
        closeConnection();
    }

    @Override
    public String buildUrl() {
        return "jdbc:oracle:thin:@//" + dbHost + ":" + dbPort + "/" + dbName;
    }

    @Override
    protected IDBConnectionPlugin getDBConnectionPlugin() {
        return this;
    }

    @Override
    protected String getJdbcDriver() {
        return ORACLE_JDBC_DRIVER;
    }

    @Override
    protected String getSqlRequestTestConnection() {
        return "select 1 from DUAL";
    }

}
