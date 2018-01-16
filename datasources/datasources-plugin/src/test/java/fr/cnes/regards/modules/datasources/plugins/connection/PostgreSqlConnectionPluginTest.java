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
package fr.cnes.regards.modules.datasources.plugins.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;

/**
 * @author Christophe Mertz
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PostgreDataSourcePluginTestConfiguration.class })
@TestPropertySource(locations = { "classpath:datasource-test.properties" })
public class PostgreSqlConnectionPluginTest extends AbstractRegardsServiceIT {

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

    @Before
    public void setUp() throws SQLException {
        IDBConnectionPlugin plgConn;

        plgConn = PluginUtils.getPlugin(getPostGreSqlParameters(), DefaultPostgreConnectionPlugin.class,
                                        Arrays.asList(PLUGIN_PACKAGE), new HashMap<>());

        // Do not launch tests is Database is not available
        Assume.assumeTrue(plgConn.testConnection());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_ARC_100")
    @Purpose("The system allows to define a connection to a data source")
    public void getPostGreSqlConnection() {
        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(getPostGreSqlParameters(), DefaultPostgreConnectionPlugin.class,
                           Arrays.asList(PLUGIN_PACKAGE), new HashMap<>());

        Assert.assertNotNull(sqlConn);

        // Do not launch tests is Database is not available
        Assume.assumeTrue(sqlConn.testConnection());
    }

    @Test
    public void getMaxPoolSizeWithClose() throws InterruptedException, SQLException {
        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(getPostGreSqlParameters(), DefaultPostgreConnectionPlugin.class,
                           Arrays.asList(PLUGIN_PACKAGE), new HashMap<>());

        Assert.assertNotNull(sqlConn);

        try (Connection conn1 = sqlConn.getConnection()) {
            Assert.assertNotNull(conn1);
        }
        Assert.assertTrue(sqlConn.testConnection());
    }

    @Test
    public void getMaxPoolSizeWithoutClose() throws InterruptedException {
        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(getPostGreSqlParameters(), DefaultPostgreConnectionPlugin.class,
                           Arrays.asList(PLUGIN_PACKAGE), new HashMap<>());

        Assert.assertNotNull(sqlConn);

        try (Connection conn1 = sqlConn.getConnection()) {
        } catch (SQLException e) {
            LOG.error("unable to get a connection", e);
            Assert.fail();
        }

    }

    @Test
    public void getMaxPoolSizeWithCloseByThread() throws InterruptedException, SQLException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName).getParameters();

        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE),
                           new HashMap<>());

        Assert.assertNotNull(sqlConn);

        // Get all the available connections
        final Connection conn1 = sqlConn.getConnection();
        Assert.assertNotNull(conn1);

        // Lambda Runnable
        Future<?> closeConnection =  Executors.newSingleThreadExecutor().submit(() -> {
            try {
                conn1.close();
            } catch (SQLException e) {
                LOG.error(e.getMessage());
            }
        });

        // Wait for the thread toi finish
        try {
            closeConnection.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Test the connection (create a new connection, as previous one is ended, this should be ok)
        Assert.assertTrue(sqlConn.testConnection());
    }

    @Test
    public void getPostGreSqlConnectionError() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, "unknown")
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName).getParameters();

        final DefaultPostgreConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultPostgreConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE),
                           new HashMap<>());

        Assert.assertNotNull(sqlConn);
        Assert.assertFalse(sqlConn.testConnection());
    }

    private List<PluginParameter> getPostGreSqlParameters() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName).getParameters();

        return parameters;
    }

}
