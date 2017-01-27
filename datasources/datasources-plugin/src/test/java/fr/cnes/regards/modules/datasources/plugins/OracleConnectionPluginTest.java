/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins;

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
public class OracleConnectionPluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(OracleConnectionPluginTest.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";
    
    @Value("${oracle.datasource.url}")
    private String url;

    @Value("${oracle.datasource.username}")
    private String user;

    @Value("${oracle.datasource.password}")
    private String password;

    @Value("${oracle.datasource.driver}")
    private String driver;

    @Test
    public void getOracleSqlConnection() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER_PARAM, driver)
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        final DefaultOracleConnectionPlugin sqlConn = PluginUtils
                .getPlugin(parameters, DefaultOracleConnectionPlugin.class, Arrays.asList(PLUGIN_PACKAGE));

        Assert.assertNotNull(sqlConn);
        Assert.assertTrue(sqlConn.testConnection());
    }

}
