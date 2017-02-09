/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.rest;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.service.IDBConnectionService;

/**
 *
 * Test DBConnnection controller
 *
 * @author Christophe Mertz
 *
 */
@TestPropertySource(locations = { "classpath:test.properties" })
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
        service.createDBConnection(createADbConnection("Hello Toulouse", "coucou"));

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultGet(DBConnectionController.TYPE_MAPPING, expectations, "Could not get all DBConnection.");
    }

    private DBConnection createADbConnection(String pLabel, String pPluginClassName) {
        final DBConnection dbConnection = new DBConnection();
        dbConnection.setUser(JOHN_DOE);
        dbConnection.setPassword(PWD_JOHN);
        dbConnection.setDriver(DRIVER);
        dbConnection.setUrl(URL);
        dbConnection.setMinPoolSize(1);
        dbConnection.setMaxPoolSize(10);
        dbConnection.setLabel(pLabel);
        dbConnection.setPluginClassName("fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin");
        return dbConnection;
    }

}
