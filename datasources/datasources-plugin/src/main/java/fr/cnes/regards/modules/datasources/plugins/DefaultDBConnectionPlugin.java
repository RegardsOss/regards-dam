/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.plugins;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.modules.datasources.plugins.plugintypes.IConnectionPlugin;

/**
 * Class DefaultSqlConnectionPlugin
 *
 * A default {@link Plugin} of type {@link IConnectionPlugin}. Allows to connect to a SQL database.
 *
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@Plugin(author = "CSSI", version = "1.0-SNAPSHOT", description = "Connection to a SQL database")
public class DefaultDBConnectionPlugin implements IConnectionPlugin {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDBConnectionPlugin.class);

    public static final String USER = "user";

    public static final String PASSWORD = "password";

    public static final String URL = "url";

    public static final String DRIVER = "driver";

    @PluginParameter(name = USER)
    private String user;

    @PluginParameter(name = PASSWORD)
    private String password;

    @PluginParameter(name = URL)
    private String url;

    @PluginParameter(name = DRIVER)
    private String driver;

    private DataSource dataSource;

    @Override
    public boolean testConnection() {
        boolean isConnected = false;
        try {
            Connection connection = dataSource.getConnection();
            connection.close();
            isConnected = true;
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return isConnected;
    }

    @PluginInit
    private void createSqlDataSource() {
        final DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(driver);
        driverManagerDataSource.setUrl(url);
        driverManagerDataSource.setUsername(user);
        driverManagerDataSource.setPassword(password);
        this.dataSource = driverManagerDataSource;
    }

}
