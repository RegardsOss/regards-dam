/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Christophe Mertz
 *
 */
public class OracleConnectionPluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(OracleConnectionPluginTest.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    @Test
    public void getOracleSqlConnection() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER, "generic")
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD, "generic")
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL, "jdbc:oracle:thin:@//172.26.8.122:1521/SIPADIC")
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER, "oracle.jdbc.OracleDriver")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE, "3")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE, "1").getParameters();

        final DefaultOracleConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultOracleConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertTrue(sqlConn.testConnection());
    }

}
