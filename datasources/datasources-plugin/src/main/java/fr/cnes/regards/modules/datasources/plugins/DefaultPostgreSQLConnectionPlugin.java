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
import fr.cnes.regards.modules.datasources.plugins.plugintypes.IDBConnectionPlugin;

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
@Plugin(author = "CSSI", version = "1.0-SNAPSHOT", description = "Connection to a Sql database")
public class DefaultPostgreSQLConnectionPlugin implements IDBConnectionPlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPostgreSQLConnectionPlugin.class);

    public static final String USER = "user";

    public static final String PASSWORD = "password";

    public static final String URL = "url";

    public static final String DRIVER = "driver";

    /**
     * The user to used for the database connection
     */
    @PluginParameter(name = USER)
    private String user;

    /**
     * The user's password to used for the database connection
     */
    @PluginParameter(name = PASSWORD)
    private String password;

    /**
     * The URL of the database
     */
    @PluginParameter(name = URL)
    private String url;

    /**
     * The JDBC driver to used
     */
    @PluginParameter(name = DRIVER)
    private String driver;

    /**
     * Maximum number of Connections a pool will maintain at any given time.
     */
    @PluginParameter(name = MAX_POOLSIZE)
    private Integer maxPoolSize;

    /**
     * Minimum number of Connections a pool will maintain at any given time.
     */
    @PluginParameter(name = MIN_POOLSIZE)
    private Integer minPoolSize;

    /**
     * A JDBC standard parameter for controlling the statement polling
     */
    @PluginParameter(name = MAX_STATEMENTS)
    Integer maxStatements;

    /**
     * A {@link ComboPooledDataSource} to used to connect to a data source
     */
    ComboPooledDataSource cpds;

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
        cpds.setMaxStatements(maxStatements);

        try {
            cpds.setDriverClass(driver);
        } catch (PropertyVetoException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean testConnection() {
        boolean isConnected = false;
        try {
            // Get a connection
            Connection conn = cpds.getConnection();
            Statement statement = conn.createStatement();

            // Execute a simple SQL request
            ResultSet rs = statement.executeQuery("select 1");

            rs.close();
            statement.close();
            conn.close();
            isConnected = true;
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return isConnected;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.cnes.regards.modules.datasources.plugins.plugintypes.IDBConnectionPlugin#getConnection()
     */
    @Override
    public Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
            LOG.equals(e);
        }
        return null;
    }

}
