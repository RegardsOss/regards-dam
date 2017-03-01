/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.connection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Christophe Mertz
 *
 */
@RunWith(SpringRunner.class)
@TestPropertySource(locations = { "classpath:datasource-test.properties" })
public class OracleConnectionPluginIntrospectionTest {

    private static final Logger LOG = LoggerFactory.getLogger(OracleConnectionPluginIntrospectionTest.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    @Value("${oracle.datasource.host}")
    private String dbHost;

    @Value("${oracle.datasource.port}")
    private String dbPort;

    @Value("${oracle.datasource.name}")
    private String dbName;

    @Value("${oracle.datasource.username}")
    private String dbUser;

    @Value("${oracle.datasource.password}")
    private String dbPpassword;

    private DefaultOracleConnectionPlugin oracleDBConn;

    @Before
    public void setUp() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, dbPpassword)
                .addParameter(DefaultOracleConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultOracleConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultOracleConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultOracleConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultOracleConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        oracleDBConn = PluginUtils.getPlugin(parameters, DefaultOracleConnectionPlugin.class,
                                             Arrays.asList(PLUGIN_PACKAGE));
    }

    @Test
    public void getTables() {

        Assert.assertTrue(oracleDBConn.testConnection());

        Map<String, Table> tables = oracleDBConn.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());
    }

    @Test
    public void getColumnsAndIndices() {

        Map<String, Table> tables = oracleDBConn.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());

        // Map<String, Column> columns = oracleDBConn.getColumns(tables.get(TABLE_NAME_TEST));
        // Assert.assertNotNull(columns);
        // Assert.assertEquals(7, columns.size());
    }

}
