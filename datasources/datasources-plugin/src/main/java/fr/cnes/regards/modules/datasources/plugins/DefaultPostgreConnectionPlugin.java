/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.AbstractDataSourceIntrospection;

/**
 * Class DefaultESConnectionPlugin
 *
 * A default {@link Plugin} of type {@link IDBConnectionPlugin}.
 *
 * For the test of the connection :
 *
 * @see http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(id = "postgresql-db-connection", author = "CSSI", version = "1.0-SNAPSHOT",
        description = "Connection to a PostgreSql database")
public class DefaultPostgreConnectionPlugin extends AbstractDataSourceIntrospection implements IDBConnectionPlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPostgreConnectionPlugin.class);

    /**
     * The JDBC PostgreSQL driver
     */
    private static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";

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
     * A {@link ComboPooledDataSource} to used to connect to a data source
     */
    private ComboPooledDataSource cpds;

    /**
     * This class is used to initialize the {@link Plugin}
     */
    @PluginInit
    private void createPoolConnection() {
        cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl(buildUrl());
        cpds.setUser(dbUser);
        cpds.setPassword(dbPassword);
        cpds.setMaxPoolSize(maxPoolSize);
        cpds.setMinPoolSize(minPoolSize);

        try {
            cpds.setDriverClass(POSTGRESQL_JDBC_DRIVER);
        } catch (PropertyVetoException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean testConnection() {
        boolean isConnected = false;
        try {
            // Get a connection
            try (Connection conn = cpds.getConnection()) {
                try (Statement statement = conn.createStatement()) {
                    // Execute a simple SQL request
                    try (ResultSet rs = statement.executeQuery("select 1")) {
                    }
                }
            }
            isConnected = true;
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return isConnected;
    }

    @Override
    public Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String buildUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
    }

    @Override
    protected IDBConnectionPlugin getDBConnectionPlugin() {
        return this;
    }

}
