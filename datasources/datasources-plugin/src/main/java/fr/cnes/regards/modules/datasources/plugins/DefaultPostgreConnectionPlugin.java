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
public class DefaultPostgreConnectionPlugin implements IDBConnectionPlugin {

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
    private String user;

    /**
     * The user's password to used for the database connection
     */
    @PluginParameter(name = PASSWORD_PARAM)
    private String password;

    /**
     * The URL_PARAM of the database
     */
    @PluginParameter(name = URL_PARAM)
    private String url;

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
        cpds.setJdbcUrl(url);
        cpds.setUser(user);
        cpds.setPassword(password);
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

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin#getConnection()
     */
    @Override
    public Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

}
