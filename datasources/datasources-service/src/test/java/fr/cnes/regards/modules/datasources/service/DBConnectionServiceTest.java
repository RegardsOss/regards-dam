/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameterType;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameterType.ParamType;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;

/**
 *
 * Unit testing of {@link DBConnectionService}.
 *
 * @author Christophe Mertz
 */
public class DBConnectionServiceTest {

    /**
     * The JDBC PostgreSQL driver
     */
    private static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";

    @Value("${postgresql.datasource.url}")
    private String url;

    @Value("${postgresql.datasource.username}")
    private String user;

    @Value("${postgresql.datasource.password}")
    private String password;


    /**
     *
     */
    private IPluginService pluginServiceMock;

    /**
     * A mock of {@link IDBConnectionService}
     */
    private IDBConnectionService dbConnectionServiceMock;

    /**
     * A {@link List} of {@link PluginConfiguration}
     */
    private List<PluginConfiguration> plgConfs = new ArrayList<>();

    /**
     * This method is run before all tests
     */
    @Before
    public void init() {
        // create mock services
        pluginServiceMock = Mockito.mock(IPluginService.class);
        dbConnectionServiceMock = new DBConnectionService(pluginServiceMock);

        // create PluginConfiguration
        List<PluginParameter> parameters = initializePluginParameter();
        plgConfs.add(new PluginConfiguration(this.initializePluginMetaDataOracle(), "first configuration", parameters));
        plgConfs.add(new PluginConfiguration(this.initializePluginMetaDataPostGre(), "second configuration", parameters,
                5));
    }

    @Test
    public void getAllDBConnection() {
        Mockito.when(pluginServiceMock.getPluginConfigurationsByType(IDBConnectionPlugin.class)).thenReturn(plgConfs);
        List<PluginConfiguration> connections = dbConnectionServiceMock.getAllDBConnections();
        Assert.assertNotNull(connections);
        Assert.assertEquals(plgConfs.size(), connections.size());
    }

    @Test
    public void createConnection() throws ModuleException {
        DBConnection dbConnection = new DBConnection();
        String className = "fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin";
        dbConnection.setPluginClassName(className);
        dbConnection.setUser(user);
        dbConnection.setPassword(password);
        dbConnection.setUrl(url);
        dbConnection.setMinPoolSize(1);
        dbConnection.setMaxPoolSize(10);
        dbConnection.setLabel("the label of the new connection");
        Mockito.when(pluginServiceMock.checkPluginClassName(IDBConnectionPlugin.class, className))
                .thenReturn(initializePluginMetaDataPostGre());
        dbConnectionServiceMock.createDBConnection(dbConnection);
        Assert.assertTrue(true);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = EntityInvalidException.class)
    public void createConnectionUnknownPluginClassName() throws ModuleException {
        DBConnection dbConnection = new DBConnection();
        String className = "fr.cnes.regards.modules.datasources.plugins.DefaultOrcleConnectionPlugin";
        dbConnection.setPluginClassName(className);
        dbConnection.setUser(user);
        dbConnection.setPassword(password);
        dbConnection.setUrl(url);
        dbConnection.setMinPoolSize(1);
        dbConnection.setMaxPoolSize(10);
        dbConnection.setLabel("the label of the new connection failed");
        Mockito.when(pluginServiceMock.checkPluginClassName(IDBConnectionPlugin.class, className))
                .thenThrow(EntityInvalidException.class);
        dbConnectionServiceMock.createDBConnection(dbConnection);
        Assert.fail();
    }

    private PluginMetaData initializePluginMetaDataOracle() {
        final PluginMetaData pluginMetaData = new PluginMetaData();
        pluginMetaData.setPluginClassName(DefaultOracleConnectionPlugin.class.getCanonicalName());
        pluginMetaData.setPluginId("plugin-id");
        pluginMetaData.setAuthor("CS-SI");
        pluginMetaData.setVersion("1.0");
        pluginMetaData.setParameters(initializePluginParameterType());
        return pluginMetaData;
    }

    private PluginMetaData initializePluginMetaDataPostGre() {
        final PluginMetaData pluginMetaData = new PluginMetaData();
        pluginMetaData.setPluginClassName(DefaultPostgreConnectionPlugin.class.getCanonicalName());
        pluginMetaData.setPluginId("plugin-id-01");
        pluginMetaData.setAuthor("CS-SI");
        pluginMetaData.setVersion("1.1");
        pluginMetaData.setParameters(initializePluginParameterType());
        return pluginMetaData;
    }

    private List<PluginParameter> initializePluginParameter() {
        return PluginParametersFactory.build().addParameter(IDBConnectionPlugin.USER_PARAM, user)
                .addParameter(IDBConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(IDBConnectionPlugin.URL_PARAM, url)
                .addParameter(IDBConnectionPlugin.DRIVER_PARAM, POSTGRESQL_JDBC_DRIVER)
                .addParameter(IDBConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(IDBConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();
    }

    private List<PluginParameterType> initializePluginParameterType() {
        return Arrays.asList(new PluginParameterType("model", String.class.getName(), ParamType.PRIMITIVE),
                             new PluginParameterType("connection", IDBConnectionPlugin.class.getCanonicalName(),
                                     ParamType.PLUGIN));
    }

}
