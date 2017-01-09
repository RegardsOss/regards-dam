/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Christophe Mertz
 *
 */
public class SqlConnectionPluginTest {

    // private static final Logger LOGGER = LoggerFactory.getLogger(PluginUtilsTest.class);

    private static String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    @Test
    public void getPostGreSqlConnection() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultSqlConnectionPlugin.USER, "azertyuiop123456789")
                .addParameter(DefaultSqlConnectionPlugin.PASSWORD, "azertyuiop123456789")
                .addParameter(DefaultSqlConnectionPlugin.URL, "jdbc:postgresql://172.26.47.52:5432/rs_testdb_cmertz")
                .addParameter(DefaultSqlConnectionPlugin.DRIVER, "org.postgresql.Driver").getParameters();

        DefaultSqlConnectionPlugin sqlConn = PluginUtils.getPlugin(parameters, DefaultSqlConnectionPlugin.class,
                                                            Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertTrue(sqlConn.testConnection());
    }
    
    
    @Test
    public void getPostGreSqlConnectionError() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultSqlConnectionPlugin.USER, "azertyuiop123456789")
                .addParameter(DefaultSqlConnectionPlugin.PASSWORD, "unknown")
                .addParameter(DefaultSqlConnectionPlugin.URL, "jdbc:postgresql://172.26.47.52:5432/rs_testdb_cmertz")
                .addParameter(DefaultSqlConnectionPlugin.DRIVER, "org.postgresql.Driver").getParameters();

        DefaultSqlConnectionPlugin sqlConn = PluginUtils.getPlugin(parameters, DefaultSqlConnectionPlugin.class,
                                                            Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertFalse(sqlConn.testConnection());
    }
    
    // oracle.jdbc.OracleDriver
    @Test
    public void getOracleSqlConnection() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultSqlConnectionPlugin.USER, "generic")
                .addParameter(DefaultSqlConnectionPlugin.PASSWORD, "generic")
                .addParameter(DefaultSqlConnectionPlugin.URL, "jdbc:oracle:thin:@//172.26.8.122:1521/SIPADIC")
                .addParameter(DefaultSqlConnectionPlugin.DRIVER, "oracle.jdbc.OracleDriver").getParameters();

        DefaultSqlConnectionPlugin sqlConn = PluginUtils.getPlugin(parameters, DefaultSqlConnectionPlugin.class,
                                                            Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertTrue(sqlConn.testConnection());
    }

    
}


