/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.connection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Christophe Mertz
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PostgreDataSourcePluginTestConfiguration.class })
public class PostgreConnectionPluginIntrospectionTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreConnectionPluginIntrospectionTest.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "t_test_plugin_data_source";

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

    private DefaultPostgreConnectionPlugin postgreDBConn;

    @Before
    public void setUp() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "5")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        postgreDBConn = PluginUtils.getPlugin(parameters, DefaultPostgreConnectionPlugin.class,
                                              Arrays.asList(PLUGIN_PACKAGE));
    }

    @Test
    public void getTables() {
        Assert.assertTrue(postgreDBConn.testConnection());
        
        Map<String, Table> tables = postgreDBConn.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());
    }

    @Test
    public void getColumnsAndIndices() {
        Assert.assertTrue(postgreDBConn.testConnection());

        Map<String, Table> tables = postgreDBConn.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());

        Map<String, Column> columns = postgreDBConn.getColumns(tables.get(TABLE_NAME_TEST));
        Assert.assertNotNull(columns);
        Assert.assertEquals(7, columns.size());
    }

}
