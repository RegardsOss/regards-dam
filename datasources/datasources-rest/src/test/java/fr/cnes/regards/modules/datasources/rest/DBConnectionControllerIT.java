/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.datasources.rest;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
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
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;
import fr.cnes.regards.modules.datasources.service.IDBConnectionService;

/**
 * Test DBConnnection controller
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

    private int pluginConfCount = 0;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IRuntimeTenantResolver resolver;

    @Autowired
    IPluginConfigurationRepository pluginConfR;

    @Autowired
    IDBConnectionService service;

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Test
    public void createPostgresDBConnection() throws ModuleException {

        pluginService.addPluginPackage(DefaultPostgreConnectionPlugin.class.getPackage().getName());

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().is2xxSuccessful());

        performDefaultPost(DBConnectionController.TYPE_MAPPING, readJsonContract("newConnection.json"), customizer,
                           "Configuration should be saved!");

        resolver.forceTenant(DEFAULT_TENANT);
        List<PluginConfiguration> dbConfs = pluginService.getPluginConfigurationsByType(IConnectionPlugin.class);
        Assert.assertNotNull(dbConfs);
        Assert.assertTrue(dbConfs.size() == 1);

        PluginConfiguration dbConf = pluginService.loadPluginConfiguration(dbConfs.get(0).getId());

        performDefaultPut(DBConnectionController.TYPE_MAPPING + "/{connectionId}", dbConf, customizer,
                          "Configuration should be saved!", dbConf.getId());
    }

    @Test
    public void createEmptyDBConnection() {
        PluginConfiguration dbConn = new PluginConfiguration();

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConn, expectations,
                           "Empty DBConnection shouldn't be created.");
    }

    @Test
    public void createDBConnectionBadPluginClassName() {
        final PluginConfiguration dbConn = new PluginConfiguration();
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
        PluginConfiguration dbConn = new PluginConfiguration();
        dbConn.setPluginClassName(POSTGRESQL_PLUGIN_CONNECTION);

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConn, expectations,
                           "Empty DBConnection shouldn't be created.");
        MaintenanceManager.unSetMaintenance(DEFAULT_TENANT); // FIXME: there should be validation on the POJO and if
        // that validation is not passed then it should send back
        // the normalized error code cf
        // GlobalControllerAdvice#hibernateValidation rather than
        // putting the system in maintenance mode
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_010")
    @Purpose("The system allows to create a connection by the configuration of a plugin's type IDBConnectionPlugin")
    public void createDBConnection() {
        PluginConfiguration dbConnection = createADbConnection("hello world!",
                                                               DefaultPostgreConnectionPlugin.class.getCanonicalName());
        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.pluginClassName",
                                                        Matchers.hasToString(dbConnection.getPluginClassName())));
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.content.label", Matchers.hasToString(dbConnection.getLabel())));

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConnection, expectations,
                           "DBConnection creation request error");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_010")
    @Purpose("The system allows to create a connection by the configuration of a plugin's type IDBConnectionPlugin")
    public void createDBConnectionWithJson() {
        String dbConnectionRequest = readJsonContract("request-dbconnection.json");

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));

        performDefaultPost(DBConnectionController.TYPE_MAPPING, dbConnectionRequest, expectations,
                           "DBConnection creation request error");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_060")
    @Purpose("The system allows to get all existing connections")
    public void getAllDBConnection() throws ModuleException {
        initPluginConfDbConnections();
        addFakePluginConf();

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$.length()", Matchers.is(pluginConfCount)));

        performDefaultGet(DBConnectionController.TYPE_MAPPING, expectations, "Could not get all DBConnection.");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_050")
    @Purpose("The system allows to get an existing connection")
    public void getDBConnection() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.content.id", Matchers.hasToString(plgConf.getId().toString())));

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{connectionId}", expectations,
                          "Could not get a DBConnection.", plgConf.getId());
    }

    @Test
    public void getUnknownDBConnection() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{connectionId}", expectations,
                          "Could not get an unknown DBConnection.", plgConf.getId() + 1000);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_030")
    @Purpose("The system allows to delete an existing connection")
    public void deleteDBConnection() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultDelete(DBConnectionController.TYPE_MAPPING + "/{connectionId}", expectations,
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
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultDelete(DBConnectionController.TYPE_MAPPING + "/{connectionId}", expectations,
                             "Could not delete a DBConnection.", 123L);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_020")
    @Purpose("The system allows to modify an existing connection")
    @Ignore // Reactivate to test Oracle DB
    public void updateDBConnection() throws ModuleException {
        PluginConfiguration dbConnection = createADbConnection("Hello", ORACLE_PLUGIN_CONNECTION);
        PluginParametersFactory.updateParameter(dbConnection.getParameter(IDBConnectionPlugin.MIN_POOLSIZE_PARAM), 3);
        PluginParametersFactory.updateParameter(dbConnection.getParameter(IDBConnectionPlugin.MAX_POOLSIZE_PARAM), 7);
        PluginParametersFactory.updateParameter(dbConnection.getParameter(IDBConnectionPlugin.USER_PARAM), "Bob");
        PluginConfiguration plgConf = service.createDBConnection(dbConnection);
        dbConnection.setId(plgConf.getId());

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultPut(DBConnectionController.TYPE_MAPPING + "/{connectionId}", dbConnection, expectations,
                          "Could not update a DBConnection.", dbConnection.getId());
    }

    @Test
    @Ignore // Reactivate to test Oracle DB
    public void updateBadDBConnection() throws ModuleException {
        PluginConfiguration dbConnection = createADbConnection("Hello", ORACLE_PLUGIN_CONNECTION);
        PluginConfiguration plgConf = service.createDBConnection(dbConnection);
        dbConnection.setId(plgConf.getId());

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isBadRequest());

        performDefaultPut(DBConnectionController.TYPE_MAPPING + "/{connectionId}", dbConnection, expectations,
                          "Could not update a DBConnection.", 456789L);
    }

    @Test
    public void updateUnknownDBConnection() throws ModuleException {
        PluginConfiguration dbConnection = createADbConnection("Hello", ORACLE_PLUGIN_CONNECTION);
        dbConnection.setId(234568L);

        // Define expectations
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultPut(DBConnectionController.TYPE_MAPPING + "/{connectionId}", dbConnection, expectations,
                          "Could not update a DBConnection.", dbConnection.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_040")
    @Purpose("The system allows to test the parameters of an existing connection")
    public void testConnection() throws ModuleException {
        PluginConfiguration dbConnection = createADbConnection("Hello", POSTGRESQL_PLUGIN_CONNECTION);
        PluginConfiguration plgConf = service.createDBConnection(dbConnection);
        dbConnection.setId(plgConf.getId());
        PluginParametersFactory.updateParameter(dbConnection.getParameter(IDBConnectionPlugin.MIN_POOLSIZE_PARAM), 3);
        PluginParametersFactory.updateParameter(dbConnection.getParameter(IDBConnectionPlugin.MAX_POOLSIZE_PARAM), 5);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultPost(DBConnectionController.TYPE_MAPPING + "/{connectionId}", dbConnection, expectations,
                           "The DBConnection is not valid.", dbConnection.getId());
    }

    @Test
    public void testConnectionFailed() throws ModuleException {
        PluginConfiguration dbConnection = createADbConnection("Hello", POSTGRESQL_PLUGIN_CONNECTION);
        PluginParametersFactory.updateParameter(dbConnection.getParameter(IDBConnectionPlugin.MIN_POOLSIZE_PARAM), 5);
        PluginParametersFactory.updateParameter(dbConnection.getParameter(IDBConnectionPlugin.MAX_POOLSIZE_PARAM), 9);
        PluginParametersFactory.updateParameter(dbConnection.getParameter(IDBConnectionPlugin.USER_PARAM), "daredevil");

        PluginConfiguration plgConf = service.createDBConnection(dbConnection);
        dbConnection.setId(plgConf.getId());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isBadRequest());

        performDefaultPost(DBConnectionController.TYPE_MAPPING + "/{connectionId}", dbConnection, expectations,
                           "The DBConnection is not valid.", dbConnection.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_070")
    @Purpose("The system allows to get the structure of the databse defined by a connection")
    public void getTables() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{connectionId}/tables", expectations,
                          "Could not get the tables.", plgConf.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_070")
    @Purpose("The system allows to get the structure of the databse defined by a connection")
    public void getColumns() throws ModuleException {
        PluginConfiguration plgConf = initPluginConfDbConnections().get(0);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultGet(DBConnectionController.TYPE_MAPPING + "/{connectionId}/tables/{tableName}/columns",
                          expectations, "Could not get the columns.", plgConf.getId(), TABLE_NAME_TEST);
    }

    private PluginConfiguration createADbConnection(String label, String pluginClassName) {
        PluginConfiguration dbConnection = new PluginConfiguration();
        dbConnection.setParameters(PluginParametersFactory.build().addParameter(IDBConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(IDBConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(IDBConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(IDBConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(IDBConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(IDBConnectionPlugin.MIN_POOLSIZE_PARAM, 3)
                .addParameter(IDBConnectionPlugin.MAX_POOLSIZE_PARAM, 10).getParameters());
        dbConnection.setLabel(label);
        dbConnection.setPluginClassName(pluginClassName);
        return dbConnection;
    }

    private List<PluginConfiguration> initPluginConfDbConnections() throws ModuleException {
        List<PluginConfiguration> plgConfs = new ArrayList<>();
        plgConfs.add(service.createDBConnection(createADbConnection("Hello Toulouse", POSTGRESQL_PLUGIN_CONNECTION)));
        plgConfs.add(service.createDBConnection(createADbConnection("Hello Paris", POSTGRESQL_PLUGIN_CONNECTION)));
        // Feel free to uncomment if you want to test oracle DB
        // plgConfs.add(service.createDBConnection(createADbConnection("Hello Bordeaux", ORACLE_PLUGIN_CONNECTION)));
        pluginConfCount = plgConfs.size();
        return plgConfs;
    }

    /**
     * Add another plugin configuration to verify only db connections are retrieved.
     */
    private void addFakePluginConf() {
        // Add fake plugin
        List<String> prefixes = new ArrayList<>();
        prefixes.add(this.getClass().getPackage().getName());
        PluginMetaData metadata = PluginUtils.createPluginMetaData(FakeConnectionPlugin.class, prefixes);

        PluginConfiguration configuration = new PluginConfiguration(metadata, "Fake");
        pluginConfR.save(configuration);
    }
}
