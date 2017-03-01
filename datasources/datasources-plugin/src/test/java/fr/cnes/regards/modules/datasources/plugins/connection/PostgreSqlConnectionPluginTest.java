/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.connection;

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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Christophe Mertz
 *
 */
@RunWith(SpringRunner.class)
@TestPropertySource(locations = { "classpath:datasource-test.properties" })
public class PostgreSqlConnectionPluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlConnectionPluginTest.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    @Value("${postgresql.datasource.host}")
    private String dbHost;

    @Value("${postgresql.datasource.port}")
    private String dbPort;

    @Value("${postgresql.datasource.name}")
    private String dbName;

    @Value("${postgresql.datasource.username}")
    private String dbUser;

    @Value("${postgresql.datasource.password}")
    private String dbPassword;

    @Test
    public void getPostGreSqlConnection() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertTrue(sqlConn.testConnection());
    }

    @Test
    public void getMaxPoolSizeWithClose() throws PluginUtilsException, InterruptedException, SQLException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

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
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

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
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "5")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

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
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, "unknown")
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertFalse(sqlConn.testConnection());
    }

}
