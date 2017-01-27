/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Christophe Mertz
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PostgreDataSourcePluginTestConfiguration.class })
public class PostgreSqlConnectionPluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlConnectionPluginTest.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    @Value("${postgresql.datasource.url}")
    private String url;

    @Value("${postgresql.datasource.username}")
    private String user;

    @Value("${postgresql.datasource.password}")
    private String password;

    @Value("${postgresql.datasource.driver}")
    private String driver;

    @Test
    public void getPostGreSqlConnection() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER_PARAM, driver)
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreSQLConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreSQLConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertTrue(sqlConn.testConnection());
    }

    @Test
    public void getMaxPoolSizeWithClose() throws PluginUtilsException, InterruptedException, SQLException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER_PARAM, driver)
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreSQLConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreSQLConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);

        final Connection conn1 = sqlConn.getConnection();
        Assert.assertNotNull(conn1);
        Assert.assertTrue(sqlConn.testConnection());

        final Connection conn2 = sqlConn.getConnection();
        Assert.assertNotNull(conn2);
        Assert.assertTrue(sqlConn.testConnection());

        final Connection conn3 = sqlConn.getConnection();
        Assert.assertNotNull(conn3);

        conn1.close();

        Assert.assertTrue(sqlConn.testConnection());
        final Connection conn4 = sqlConn.getConnection();
        Assert.assertNotNull(conn4);

        conn4.close();
        Assert.assertTrue(sqlConn.testConnection());
        conn2.close();
        conn3.close();
    }

    @Test
    public void getMaxPoolSizeWithoutClose() throws PluginUtilsException, InterruptedException, SQLException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER_PARAM, driver)
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreSQLConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreSQLConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);

        final Connection conn1 = sqlConn.getConnection();
        Assert.assertNotNull(conn1);
        final Connection conn2 = sqlConn.getConnection();
        Assert.assertNotNull(conn2);
        final Connection conn3 = sqlConn.getConnection();
        Assert.assertNotNull(conn3);

        final Connection conn4 = sqlConn.getConnection();
        Assert.assertNull(conn4);

        conn1.close();
        conn2.close();
        conn3.close();
    }

    @Test
    public void getMaxPoolSizeWithCloseByThread() throws PluginUtilsException, InterruptedException, SQLException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER_PARAM, driver)
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE_PARAM, "5")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreSQLConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreSQLConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);

        // Get all the available connections
        final Connection conn1 = sqlConn.getConnection();
        Assert.assertNotNull(conn1);
        final Connection conn2 = sqlConn.getConnection();
        Assert.assertNotNull(conn2);
        final Connection conn3 = sqlConn.getConnection();
        Assert.assertNotNull(conn3);
        final Connection conn4 = sqlConn.getConnection();
        Assert.assertNotNull(conn4);
        final Connection conn5 = sqlConn.getConnection();
        Assert.assertNotNull(conn5);

        // Lambda Runnable
        final Runnable closeConnection = () -> {
            try {
                // Close 2 connections
                conn1.close();
                conn2.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage());
            }
        };

        // start the thread
        new Thread(closeConnection).start();

        // Gest a connection
        final Connection conn6 = sqlConn.getConnection();
        Assert.assertNotNull(conn6);

        // Test the connection
        Assert.assertTrue(sqlConn.testConnection());

        conn3.close();
        conn4.close();
        conn5.close();
        conn6.close();

    }

    @Test
    public void getPostGreSqlConnectionError() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD_PARAM, "unknown")
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER_PARAM, driver)
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreSQLConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreSQLConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertFalse(sqlConn.testConnection());

    }

}
