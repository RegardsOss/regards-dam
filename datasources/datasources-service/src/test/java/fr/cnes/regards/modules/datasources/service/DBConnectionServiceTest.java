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
     * A constant for a user name for a database connection test
     */
    private static final String JOHN_DOE = "john.doe";

    /**
     * A constant for a password for a database connection test
     */
    private static final String PWD_JOHN = "azertyuiop";

    /**
     * A constant for a driver for a database connection test
     */
    private static final String DRIVER = "oracle.jdbc.OracleDriver";

    /**
     * A constant for an URL for a database connection test
     */
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/BDD";

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
        plgConfs.add(new PluginConfiguration(this.initializePluginMetaDataOracle(), "first configuration", parameters,
                0));
        plgConfs.add(new PluginConfiguration(this.initializePluginMetaDataPostGre(), "second configuration", parameters,
                0));
    }

    @Test
    public void getAllDBConnection() {
        Mockito.when(pluginServiceMock.getPluginConfigurationsByType(IDBConnectionPlugin.class)).thenReturn(plgConfs);
        List<PluginConfiguration> connections = dbConnectionServiceMock.getAllDBConnections();
        Assert.assertNotNull(connections);
        Assert.assertEquals(plgConfs.size(), connections.size());
    }

    @Test
    public void createdConnection() throws ModuleException {
        DBConnection dbConnection = new DBConnection();
        dbConnection.setPluginClassName("fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin");
        dbConnection.setUser(JOHN_DOE);
        dbConnection.setPassword(PWD_JOHN);
        dbConnection.setDriver(DRIVER);
        dbConnection.setUrl(URL);
        dbConnection.setMinPoolSize(1);
        dbConnection.setMaxPoolSize(10);
        dbConnection.setLabel("the label of the new connection");
        Mockito.when(pluginServiceMock.getPluginsByType(IDBConnectionPlugin.class))
                .thenReturn(Arrays.asList(initializePluginMetaDataOracle(), initializePluginMetaDataPostGre()));
        dbConnectionServiceMock.createDBConnection(dbConnection);
        Assert.assertTrue(true);
    }

    @Test(expected = EntityInvalidException.class)
    public void createdConnectionUnknownPluginClassName() throws ModuleException {
        DBConnection dbConnection = new DBConnection();
        dbConnection.setPluginClassName("fr.cnes.regards.modules.datasources.plugins.DefaultOrConnectionPlugin");
        dbConnection.setUser(JOHN_DOE);
        dbConnection.setPassword(PWD_JOHN);
        dbConnection.setDriver(DRIVER);
        dbConnection.setUrl(URL);
        dbConnection.setMinPoolSize(1);
        dbConnection.setMaxPoolSize(10);
        dbConnection.setLabel("the label of the new connection");
        Mockito.when(pluginServiceMock.getPluginsByType(IDBConnectionPlugin.class))
                .thenReturn(Arrays.asList(initializePluginMetaDataOracle(), initializePluginMetaDataPostGre()));
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
        pluginMetaData.setPluginId("plugin-id");
        pluginMetaData.setAuthor("CS-SI");
        pluginMetaData.setVersion("1.0");
        pluginMetaData.setParameters(initializePluginParameterType());
        return pluginMetaData;
    }

    private List<PluginParameter> initializePluginParameter() {
        return PluginParametersFactory.build().addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, JOHN_DOE)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, PWD_JOHN)
                .addParameter(DefaultPostgreConnectionPlugin.URL_PARAM, URL)
                .addParameter(DefaultPostgreConnectionPlugin.DRIVER_PARAM, DRIVER)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();
    }

    private List<PluginParameterType> initializePluginParameterType() {
        return Arrays.asList(new PluginParameterType("model", String.class.getName(), ParamType.PRIMITIVE),
                             new PluginParameterType("connection", IConnectionPlugin.class.getCanonicalName(),
                                     ParamType.PLUGIN));
    }

}
