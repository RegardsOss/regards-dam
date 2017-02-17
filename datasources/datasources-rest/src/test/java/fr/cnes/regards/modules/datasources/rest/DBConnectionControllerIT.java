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
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.service.DBConnectionService;
import fr.cnes.regards.modules.datasources.service.IDBConnectionService;

/**
 *
 * Test DBConnnection controller
 *
 * @author Christophe Mertz
 *
 */
@TestPropertySource(locations = { "classpath:datasource-test.properties" })
@MultitenantTransactional
public class DBConnectionControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectionControllerIT.class);

    /**
     * JSON path
     */
    private static final String JSON_ID = "$.content.id";

    @Value("${postgresql.datasource.url}")
    private String url;

    @Value("${postgresql.datasource.username}")
    private String user;

    @Value("${postgresql.datasource.password}")
    private String password;

    @Value("${postgresql.datasource.driver}")
    private String driver;

    @Autowired
    IDBConnectionService service;

    @Autowired
    DBConnectionService dbConnectionService;

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
    public void createEmptyDBConnectionWithPluginClassName() {

        final DBConnection dbConn = new DBConnection();
        dbConn.setPluginClassName("fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin");

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isServiceUnavailable());

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConn, expectations,
                           "Empty DBConnection shouldn't be created.");
    }

    @Test
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
    public void getAllDBConnection() throws ModuleException {
        service.createDBConnection(createADbConnection("Hello Toulouse",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin"));
        service.createDBConnection(createADbConnection("Hello Paris",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin"));
        service.createDBConnection(createADbConnection("Hello Paris",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin"));

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultGet(DBConnectionController.TYPE_MAPPING, expectations, "Could not get all DBConnection.");
    }

    @Test
    public void getDBConnection() throws ModuleException {
        PluginConfiguration plgConf = service
                .createDBConnection(createADbConnection("Hello Toulouse",
                                                        "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin"));
        service.createDBConnection(createADbConnection("Hello Paris",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin"));
        service.createDBConnection(createADbConnection("Hello Paris",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin"));

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
        PluginConfiguration plgConf = service
                .createDBConnection(createADbConnection("Hello Toulouse",
                                                        "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin"));
        service.createDBConnection(createADbConnection("Hello Paris",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin"));
        service.createDBConnection(createADbConnection("Hello Paris",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin"));

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{pConnectionId}", expectations,
                          "Could not get an unknown DBConnection.", plgConf.getId() + 1000);
    }

    @Test
    public void deleteDBConnection() throws ModuleException {
        PluginConfiguration plgConf = service
                .createDBConnection(createADbConnection("Hello Toulouse",
                                                        "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin"));
        service.createDBConnection(createADbConnection("Hello Paris",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin"));
        service.createDBConnection(createADbConnection("Hello Paris",
                                                       "fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin"));

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
    public void updateDBConnection() throws ModuleException {
        DBConnection dbConnection = createADbConnection("Hello",
                                                        "fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin");
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
    public void tesConnection() throws ModuleException {
        DBConnection dbConnection = createADbConnection("Hello",
                                                        "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin");
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
        DBConnection dbConnection = createADbConnection("Hello",
                                                        "fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin");
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

    private DBConnection createADbConnection(String pLabel, String pPluginClassName) {
        final DBConnection dbConnection = new DBConnection();
        dbConnection.setUser(user);
        dbConnection.setPassword(password);
        dbConnection.setDriver(driver);
        dbConnection.setUrl(url);
        dbConnection.setMinPoolSize(1);
        dbConnection.setMaxPoolSize(10);
        dbConnection.setLabel(pLabel);
        dbConnection.setPluginClassName(pPluginClassName);
        return dbConnection;
    }

}
