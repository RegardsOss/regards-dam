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
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
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
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DynamicAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.StaticAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.plugins.IDBDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.domain.plugins.IDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.domain.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.service.IDataSourceService;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IModelService;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test sata source PluginConfiguration controller
 * @author Christophe Mertz
 */
@TestPropertySource(locations = { "classpath:datasource-test.properties" })
@MultitenantTransactional
public class DataSourceControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceControllerIT.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "t_test_plugin_data_source";

    private final static String FROM_CLAUSE_TEST = "from T_TEST_PLUGIN_DATA_SOURCE";

    private static final String LABEL_DATA_SOURCE = "the label of the data source";

    private final static String JSON_PATH_LABEL = "$.content.label";

    private final static String JSON_PATH_FROM_CLAUSE = "$.content.parameters.[0].value";

    private final static String JSON_PATH_TABLE_NAME = "$.content.parameters.[0].value";

    private final static String JSON_PATH_PLUGIN_CONNECTION = "$.content.parameters.[3].pluginConfiguration.id";

    private static final String DEFAULT_MODEL_NAME = "VALIDATION_MODEL_2";

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
    private IPluginService pluginService;

    @Autowired
    private IDataSourceService dataSourceService;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepos;

    @Autowired
    private IModelService modelService;

    private PluginConfiguration pluginPostgreDbConnection;

    private List<AbstractAttributeMapping> modelAttrMapping;

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Before
    public void setUp() throws ModuleException, JwtException {
        jwtService.injectToken(DEFAULT_TENANT, DEFAULT_ROLE, DEFAULT_USER_EMAIL);
        try {
            // Remove the model if existing
            modelService.getModelByName(DEFAULT_MODEL_NAME);
            modelService.deleteModel(DEFAULT_MODEL_NAME);
        } catch (ModuleException e) {
            // There is nothing to do - we create the model later
        }
        modelService.createModel(Model.build(DEFAULT_MODEL_NAME, "", EntityType.DATA));

        pluginConfRepos.deleteAll();

        // Initialize the AbstractAttributeMapping
        buildModelAttributes();

        // Save a PluginConfiguration for plugin's type IDBConnectionPlugin
        pluginPostgreDbConnection = pluginService.savePluginConfiguration(getPostGreSqlConnectionConfiguration());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_110")
    @Purpose("The system allows to define a datasource by setting a SQL request")
    public void createDataSourceWithFromClauseTest() {
        final PluginConfiguration dataSource = createDataSourceWithFromClause();

        Assert.assertEquals(0, dataSourceService.getAllDataSources().size());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_FROM_CLAUSE,
                          Matchers.equalTo(dataSource.getStripParameterValue(IDBDataSourcePlugin.FROM_CLAUSE))));

        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");

        Assert.assertEquals(1, dataSourceService.getAllDataSources().size());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_100")
    @Purpose("The system allows to define a datasource by the configuration a plugin's type IDataSourcePlugin")
    public void createDataSourceWithSingleTableTest() {
        final PluginConfiguration dataSource = createDataSourceSingleTable();

        Assert.assertEquals(0, dataSourceService.getAllDataSources().size());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_TABLE_NAME, Matchers
                .equalTo(dataSource.getStripParameterValue(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM))));

        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");

        Assert.assertEquals(1, dataSourceService.getAllDataSources().size());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_140")
    @Purpose("The system allows to get a datasource")
    public void getDataSource() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Create a DataSource
        final PluginConfiguration dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setId(pls.get(0).getId());

        // Define expectations
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_PLUGIN_CONNECTION, Matchers.hasToString(dataSource
                .getParameterConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM).getId().toString())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_FROM_CLAUSE,
                          Matchers.equalTo(dataSource.getStripParameterValue(IDBDataSourcePlugin.FROM_CLAUSE))));

        performDefaultGet(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", expectations,
                          "DataSource shouldn't be retrieve.", dataSource.getId());
    }

    @Test
    public void getUnknownDataSource() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultGet(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", expectations,
                          "DataSource shouldn't be retrieve.", Long.MAX_VALUE);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_120")
    @Purpose("The system allows to update a datasource by updating the SQL request")
    public void dataSourceUpdateFromClause() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Create a DataSource
        PluginConfiguration dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setId(pls.get(0).getId());

        // Update the DataSource
        dataSource.getParameter(IDBDataSourcePlugin.FROM_CLAUSE).setValue("\"from table where table.id>1000\"");

        // Define expectations
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_PLUGIN_CONNECTION, Matchers.hasToString(dataSource
                .getParameterConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM).getId().toString())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_FROM_CLAUSE,
                          Matchers.equalTo(dataSource.getStripParameterValue(IDBDataSourcePlugin.FROM_CLAUSE))));

        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_120")
    @Purpose("The system allows to update a datasource by updating the connection")
    public void dataSourceUpdateDBConnection() throws ModuleException {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Create a DataSource
        final PluginConfiguration dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setId(pls.get(0).getId());

        // Update the DataSource
        PluginConfiguration otherDbConnection = pluginService
                .savePluginConfiguration(getPostGreSqlConnectionConfiguration());
        dataSource.getParameter(IDBDataSourcePlugin.CONNECTION_PARAM).setPluginConfiguration(otherDbConnection);

        // Define expectations
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_PLUGIN_CONNECTION, Matchers.hasToString(dataSource
                .getParameterConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM).getId().toString())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_FROM_CLAUSE,
                          Matchers.equalTo(dataSource.getStripParameterValue(IDBDataSourcePlugin.FROM_CLAUSE))));

        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_130")
    @Purpose("The system allows to delete a datasource")
    public void deleteDataSource() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().is2xxSuccessful());
        // Create a DataSource
        final PluginConfiguration dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource should have been created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setId(pls.get(0).getId());

        // Define expectations
        expectations.add(status().isNoContent());

        performDefaultDelete(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", expectations,
                             "DataSource should have been deleted.", dataSource.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_150")
    @Purpose("The system allows to get all datasources")
    public void getAllDataSources() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Create a DataSource
        performDefaultPost(DataSourceController.TYPE_MAPPING, createDataSourceWithFromClause(), expectations,
                           "DataSource shouldn't be created.");

        // Create a DataSource
        performDefaultPost(DataSourceController.TYPE_MAPPING, createDataSourceSingleTable(), expectations,
                           "DataSource shouldn't be created.");

        expectations.add(MockMvcResultMatchers
                .jsonPath("$.[0].content.parameters.[3].pluginConfiguration.id",
                          Matchers.hasToString(pluginPostgreDbConnection.getId().toString())));
        expectations.add(MockMvcResultMatchers
                .jsonPath("$.[1].content.parameters.[3].pluginConfiguration.id",
                          Matchers.hasToString(pluginPostgreDbConnection.getId().toString())));

        performDefaultGet(DataSourceController.TYPE_MAPPING, expectations, "DataSources shouldn't be retrieve.");
    }

    private PluginConfiguration createDataSourceWithFromClause() {
        PluginConfiguration dataSource = new PluginConfiguration();
        dataSource.setParameters(PluginParametersFactory.build()
                .addParameter(IDBDataSourcePlugin.FROM_CLAUSE, FROM_CLAUSE_TEST)
                .addParameter(IDBDataSourcePlugin.MODEL_NAME_PARAM, DEFAULT_MODEL_NAME)
                .addParameter(IDBDataSourcePlugin.MODEL_MAPPING_PARAM, modelAttrMapping)
                .addPluginConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM, pluginPostgreDbConnection)
                .getParameters());
        dataSource.setPluginId("dataSourceTest");
        dataSource.setLabel(LABEL_DATA_SOURCE + " with from clause");
        dataSource.setPluginClassName(MockDatasourcePlugin.class.getName());
        dataSource.setVersion("alpha");
        return dataSource;

        // final DataSource dataSource = new DataSource();
        // dataSource.setFromClause(FROM_CLAUSE_TEST);
        // dataSource.setPluginClassName(PostgreDataSourcePlugin.class.getCanonicalName());
        // dataSource.setPluginConfigurationConnectionId(pluginPostgreDbConnection.getId());
        // dataSource.setMapping(modelMapping);
        // dataSource.setLabel(LABEL_DATA_SOURCE + " with from clause");
        //
        // return dataSource;
    }

    private PluginConfiguration createDataSourceSingleTable() {
        PluginConfiguration dataSource = new PluginConfiguration();
        PluginParametersFactory factory = PluginParametersFactory.build();
        factory.addParameter(IDBDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST);
        factory.addParameter(IDBDataSourcePlugin.MODEL_NAME_PARAM, DEFAULT_MODEL_NAME);
        factory.addParameter(IDBDataSourcePlugin.MODEL_MAPPING_PARAM, modelAttrMapping);
        factory.addPluginConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM, pluginPostgreDbConnection);
        dataSource.setPluginId("dataSourceTest");
        dataSource.setParameters(factory.getParameters());
        dataSource.setLabel(LABEL_DATA_SOURCE + " with table name");
        dataSource.setPluginClassName(MockDatasourcePlugin.class.getName());
        dataSource.setVersion("alpha");
        
        return dataSource;
    }

    @Test
    @Ignore
    public void createDataSourceWithJson() {
        String dataSourceRequest = readJsonContract("request-datasource.json");

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));

        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSourceRequest, expectations,
                           "DataSource creation request error.");
    }

    private void buildModelAttributes() {
        modelAttrMapping = new ArrayList<AbstractAttributeMapping>();

        modelAttrMapping.add(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY, "id"));
        modelAttrMapping.add(new StaticAttributeMapping(AbstractAttributeMapping.LABEL, "name"));
        modelAttrMapping.add(new DynamicAttributeMapping("alt", "geometry", AttributeType.INTEGER, "altitude"));
        modelAttrMapping.add(new DynamicAttributeMapping("lat", "geometry", AttributeType.DOUBLE, "latitude"));
        modelAttrMapping.add(new DynamicAttributeMapping("long", "geometry", AttributeType.DOUBLE, "longitude"));
        modelAttrMapping.add(new DynamicAttributeMapping("creationDate1", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithoutTimeZone"));
        modelAttrMapping.add(new DynamicAttributeMapping("creationDate2", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithoutTimeZone"));
        modelAttrMapping.add(new DynamicAttributeMapping("date", "hello", AttributeType.DATE_ISO8601, "date"));
        modelAttrMapping.add(new DynamicAttributeMapping("timeStampWithTimeZone", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithTimeZone"));
        modelAttrMapping.add(new DynamicAttributeMapping("isUpdate", "hello", AttributeType.BOOLEAN, "update"));
    }

    private PluginConfiguration getPostGreSqlConnectionConfiguration() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(MockConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(MockConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(MockConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(MockConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(MockConnectionPlugin.DB_NAME_PARAM, dbName).getParameters();

        return PluginUtils.getPluginConfiguration(parameters, MockConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_PACKAGE));
    }

}
