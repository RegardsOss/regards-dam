/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.rest;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.microservice.manager.MaintenanceManager;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.service.IDBConnectionService;

/**
 * Test DBConnnection controller
 *
 * @author Christophe Mertz
 */
@TestPropertySource(locations = { "classpath:datasource-test.properties" })
@MultitenantTransactional
public class DBConnectionControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectionControllerIT.class);

    private static final String ORACLE_PLUGIN_CONNECTION = "fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin";

    private static final String POSTGRESQL_PLUGIN_CONNECTION = "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin";

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

    @Autowired
    IDBConnectionService service;

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Test
    public void createEmptyDBConnection() {
        final DBConnection dbConn = new DBConnection();

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConn, expectations,
                           "Empty DBConnection shouldn't be created.");
    }

    @Test
    public void createDBConnectionBadPluginClassName() {
        final DBConnection dbConn = new DBConnection();
        dbConn.setPluginClassName("fr.cnes.regards.modules.datasources.plugins.DefaultPostgrConnectionPlugin");

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConn, expectations,
                           "Empty DBConnection shouldn't be created.");
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_230")
    @Purpose("If a HTTP request POST is unsupported or mal-formatted, the HTTP return code is 503")
    public void createEmptyDBConnectionWithPluginClassName() {
        final DBConnection dbConn = new DBConnection();
        dbConn.setPluginClassName(POSTGRESQL_PLUGIN_CONNECTION);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConn, expectations,
                           "Empty DBConnection shouldn't be created.");
        MaintenanceManager.unSetMaintenance(DEFAULT_TENANT); // FIXME: there should be validation on the POJO and if
                                                             // that validation is not passed then it should send back
                                                             // the normalized error code cd
                                                             // GlobalControllerAdvice#hibernateValidation rather than
                                                             // putting the system in maintenance mode
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_010")
    @Purpose("The system allows to create a connection by the configuration of a plugin's type IDBConnectionPlugin")
    public void createDBConnection() {
        final DBConnection dbConnection = createADbConnection("hello world!",
                                                              DefaultPostgreConnectionPlugin.class.getCanonicalName());
        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.pluginClassName",
                                                        Matchers.hasToString(dbConnection.getPluginClassName())));
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.content.label", Matchers.hasToString(dbConnection.getLabel())));

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConnection, expectations,
                           "Empty DBConnection shouldn't be created.");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_060")
    @Purpose("The system allows to get all existing connections")
    public void getAllDBConnection() throws ModuleException {
        initPluginConfDbConnections();

        Assert.assertTrue(0 < service.getAllDBConnections().size());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultGet(DBConnectionController.TYPE_MAPPING, expectations, "Could not get all DBConnection.");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_050")
    @Purpose("The system allows to get an existing connection")
    public void getDBConnection() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.content.id", Matchers.hasToString(plgConf.getId().toString())));

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", expectations,
                          "Could not get a DBConnection.", plgConf.getId());
    }

    @Test
    public void getUnknownDBConnection() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", expectations,
                          "Could not get an unknown DBConnection.", plgConf.getId() + 1000);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_030")
    @Purpose("The system allows to delete an existing connection")
    public void deleteDBConnection() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultDelete(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", expectations,
                             "Could not delete a DBConnection.", plgConf.getId());

        PluginConfiguration plgCondDelete = null;
        try {
            plgCondDelete = service.getDBConnection(plgConf.getId());
            Assert.fail();
        } catch (ModuleException e) {
            Assert.assertTrue(true);
        }
        Assert.assertNull(plgCondDelete);
    }

    @Test
    public void deleteUnknownDBConnection() throws ModuleException {
        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultDelete(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", expectations,
                             "Could not delete a DBConnection.", 123L);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_020")
    @Purpose("The system allows to modify an existing connection")
    public void updateDBConnection() throws ModuleException {
        DBConnection dbConnection = createADbConnection("Hello", ORACLE_PLUGIN_CONNECTION);
        dbConnection.setMinPoolSize(3);
        dbConnection.setMaxPoolSize(7);
        dbConnection.setUser("Bob");
        PluginConfiguration plgConf = service.createDBConnection(dbConnection);
        dbConnection.setPluginConfigurationId(plgConf.getId());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultPut(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", dbConnection, expectations,
                          "Could not update a DBConnection.", dbConnection.getPluginConfigurationId());
    }

    @Test
    public void updateBadDBConnection() throws ModuleException {
        DBConnection dbConnection = createADbConnection("Hello", ORACLE_PLUGIN_CONNECTION);
        PluginConfiguration plgConf = service.createDBConnection(dbConnection);
        dbConnection.setPluginConfigurationId(plgConf.getId());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isBadRequest());

        performDefaultPut(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", dbConnection, expectations,
                          "Could not update a DBConnection.", 456789L);
    }

    @Test
    public void updateUnknownDBConnection() throws ModuleException {
        DBConnection dbConnection = createADbConnection("Hello", ORACLE_PLUGIN_CONNECTION);
        dbConnection.setPluginConfigurationId(234568L);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultPut(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", dbConnection, expectations,
                          "Could not update a DBConnection.", dbConnection.getPluginConfigurationId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_040")
    @Purpose("The system allows to test the parameters of an existing connection")
    public void testConnection() throws ModuleException {
        DBConnection dbConnection = createADbConnection("Hello", POSTGRESQL_PLUGIN_CONNECTION);
        PluginConfiguration plgConf = service.createDBConnection(dbConnection);
        dbConnection.setPluginConfigurationId(plgConf.getId());
        dbConnection.setMinPoolSize(3);
        dbConnection.setMaxPoolSize(5);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultPost(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", dbConnection, expectations,
                           "The DBConnection is not valid.", dbConnection.getPluginConfigurationId());
    }

    @Test
    public void tesConnectionFailed() throws ModuleException {
        DBConnection dbConnection = createADbConnection("Hello", POSTGRESQL_PLUGIN_CONNECTION);
        dbConnection.setMinPoolSize(5);
        dbConnection.setMaxPoolSize(9);
        dbConnection.setUser("dardevil");

        PluginConfiguration plgConf = service.createDBConnection(dbConnection);
        dbConnection.setPluginConfigurationId(plgConf.getId());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultPost(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", dbConnection, expectations,
                           "The DBConnection is not valid.", dbConnection.getPluginConfigurationId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_070")
    @Purpose("The system allows to get the structure of the databse defined by a connection")
    public void getTables() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}/tables", expectations,
                          "Could not get the tables.", plgConf.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_070")
    @Purpose("The system allows to get the structure of the databse defined by a connection")
    public void getColumns() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}/tables/{pTableName}/columns",
                          expectations, "Could not get the columns.", plgConf.getId(), TABLE_NAME_TEST);
    }

    private DBConnection createADbConnection(String pLabel, String pPluginClassName) {
        final DBConnection dbConnection = new DBConnection();
        dbConnection.setUser(dbUser);
        dbConnection.setPassword(dbPassword);
        dbConnection.setDbHost(dbHost);
        dbConnection.setDbPort(dbPort);
        dbConnection.setDbName(dbName);
        dbConnection.setMinPoolSize(3);
        dbConnection.setMaxPoolSize(10);
        dbConnection.setLabel(pLabel);
        dbConnection.setPluginClassName(pPluginClassName);
        return dbConnection;
    }

    private List<PluginConfiguration> initPluginConfDbConnections() throws ModuleException {
        List<PluginConfiguration> plgConfs = new ArrayList<>();
        plgConfs.add(service.createDBConnection(createADbConnection("Hello Toulouse", POSTGRESQL_PLUGIN_CONNECTION)));
        plgConfs.add(service.createDBConnection(createADbConnection("Hello Paris", POSTGRESQL_PLUGIN_CONNECTION)));
        plgConfs.add(service.createDBConnection(createADbConnection("Hello Bordeaux", ORACLE_PLUGIN_CONNECTION)));
        return plgConfs;
    }

}
