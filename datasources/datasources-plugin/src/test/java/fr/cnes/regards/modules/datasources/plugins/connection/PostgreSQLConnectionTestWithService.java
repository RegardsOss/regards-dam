/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.plugins.utils.PluginUtils;

/**
 * @author Christophe Mertz
 */
@RunWith(SpringRunner.class)
@TestPropertySource(locations = { "classpath:datasource-test.properties" })
@ComponentScan(basePackages = { "fr.cnes.regards.modules.datasources.utils" })
public class PostgreSQLConnectionTestWithService {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLConnectionTestWithService.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

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

    private IPluginConfigurationRepository pluginConfRepositoryMocked;

    private IPluginService pluginServiceMocked;

    @Before
    public void setUp() {
        // create a mock repository
        pluginConfRepositoryMocked = Mockito.mock(IPluginConfigurationRepository.class);
        pluginServiceMocked = new PluginService(pluginConfRepositoryMocked, Mockito.mock(IPublisher.class));
        pluginServiceMocked.addPluginPackage("fr.cnes.regards.modules.datasources.plugins");
    }

    @Test
    public void testPoolConnectionWithGetFirstPluginByType() throws ModuleException {
        // Save a PluginConfiguration
        final Long anId = 33L;
        final PluginConfiguration aPluginConfiguration = getPostGreSqlConnectionConfiguration();
        aPluginConfiguration.setId(anId);
        Mockito.when(pluginConfRepositoryMocked.save(aPluginConfiguration)).thenReturn(aPluginConfiguration);
        pluginServiceMocked.savePluginConfiguration(aPluginConfiguration);

        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        pluginConfs.add(aPluginConfiguration);

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc("postgresql-db-connection"))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        // Get the first Plugin
        final DefaultPostgreConnectionPlugin aa = pluginServiceMocked.getFirstPluginByType(IDBConnectionPlugin.class);

        Assert.assertNotNull(aa);
        Assert.assertTrue(aa.testConnection());

        // Get the first Plugin : the same than the previous
        final DefaultPostgreConnectionPlugin bb = pluginServiceMocked.getFirstPluginByType(IDBConnectionPlugin.class);

        Assert.assertNotNull(bb);
        Assert.assertTrue(bb.testConnection());
        Assert.assertEquals(aa, bb);
    }

    @Test
    public void testPoolConnectionWithGetPlugin() throws ModuleException {
        // Save a PluginConfiguration
        final Long anId = 33L;
        final PluginConfiguration aPluginConfiguration = getPostGreSqlConnectionConfiguration();
        aPluginConfiguration.setId(anId);
        Mockito.when(pluginConfRepositoryMocked.save(aPluginConfiguration)).thenReturn(aPluginConfiguration);
        pluginServiceMocked.savePluginConfiguration(aPluginConfiguration);

        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        pluginConfs.add(aPluginConfiguration);

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc("postgresql-db-connection"))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        // Get a Plugin for a specific configuration
        final DefaultPostgreConnectionPlugin aa = pluginServiceMocked.getPlugin(anId);

        Assert.assertNotNull(aa);
        Assert.assertTrue(aa.testConnection());

        // Get a Plugin for a specific configuration
        final DefaultPostgreConnectionPlugin bb = pluginServiceMocked.getPlugin(anId);

        Assert.assertNotNull(bb);
        Assert.assertTrue(bb.testConnection());
        Assert.assertEquals(aa, bb);
    }

    @After
    public void erase() {
        // repository.deleteAll();
    }

    /**
     * Define the {@link PluginConfiguration} for a {@link DefaultPostgreConnectionPlugin} to connect to the PostgreSql
     * database
     *
     * @return the {@link PluginConfiguration}
     */
    private PluginConfiguration getPostGreSqlConnectionConfiguration() {
        final List<PluginParameter> params = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        return PluginUtils.getPluginConfiguration(params, DefaultPostgreConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));

    }

}
