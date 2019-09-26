/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.framework.utils.plugins.PluginParameterTransformer;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.dam.domain.datasources.AbstractAttributeMapping;
import fr.cnes.regards.modules.dam.domain.datasources.DynamicAttributeMapping;
import fr.cnes.regards.modules.dam.domain.datasources.StaticAttributeMapping;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DBConnectionPluginConstants;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DataSourcePluginConstants;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;
import fr.cnes.regards.modules.dam.service.datasources.IDataSourceService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

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

    private static final String TABLE_NAME_TEST = "t_test_plugin_data_source";

    private final static String FROM_CLAUSE_TEST = "from T_TEST_PLUGIN_DATA_SOURCE";

    //    private static final String LABEL_DATA_SOURCE = "the label of the data source";

    private final static String JSON_PATH_LABEL = "$.content.label";

    private final static String JSON_PATH_FROM_CLAUSE = "$.content.parameters.[?(@.name == '"
            + DataSourcePluginConstants.FROM_CLAUSE + "')].value";

    private final static String JSON_PATH_TABLE_NAME = "$.content.parameters.[?(@.name == '"
            + DataSourcePluginConstants.TABLE_PARAM + "')].value";

    private final static String PLUGIN_CONNECTION_PARAM_PATH = "parameters.[?(@.name == '"
            + DataSourcePluginConstants.CONNECTION_PARAM + "')].value";

    private final static String JSON_PATH_PLUGIN_CONNECTION = "$.content." + PLUGIN_CONNECTION_PARAM_PATH;

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
        jwtService.injectToken(getDefaultTenant(), DEFAULT_ROLE, getDefaultUserEmail(), getDefaultUserEmail());
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

        RequestBuilderCustomizer expectations = customizer().expectStatusOk()
                .expectValue(JSON_PATH_LABEL, dataSource.getLabel())
                .expect(MockMvcResultMatchers.jsonPath(JSON_PATH_FROM_CLAUSE, Matchers
                        .contains(dataSource.getParameterValue(DataSourcePluginConstants.FROM_CLAUSE))));
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
        RequestBuilderCustomizer expectations = customizer().expectStatusOk()
                .expectValue(JSON_PATH_LABEL, dataSource.getLabel())
                .expect(MockMvcResultMatchers.jsonPath(JSON_PATH_TABLE_NAME, Matchers
                        .contains(dataSource.getParameterValue(DataSourcePluginConstants.TABLE_PARAM))));

        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");

        Assert.assertEquals(1, dataSourceService.getAllDataSources().size());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_140")
    @Purpose("The system allows to get a datasource")
    public void getDataSource() {

        // Create a DataSource
        final PluginConfiguration dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, customizer().expectStatusOk(),
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setId(pls.get(0).getId());

        // Define expectations
        RequestBuilderCustomizer expectations = customizer().expectValue(JSON_PATH_LABEL, dataSource.getLabel())
                .expect(MockMvcResultMatchers.jsonPath(JSON_PATH_PLUGIN_CONNECTION, Matchers
                        .hasItem(dataSource.getParameterValue(DataSourcePluginConstants.CONNECTION_PARAM))))
                .expect(MockMvcResultMatchers.jsonPath(JSON_PATH_FROM_CLAUSE, Matchers
                        .contains(dataSource.getParameterValue(DataSourcePluginConstants.FROM_CLAUSE))));

        performDefaultGet(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", expectations,
                          "DataSource shouldn't be retrieve.", dataSource.getId());
    }

    @Test
    public void getUnknownDataSource() {
        performDefaultGet(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", customizer().expectStatusNotFound(),
                          "DataSource shouldn't be retrieve.", Long.MAX_VALUE);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_120")
    @Purpose("The system allows to update a datasource by updating the SQL request")
    public void dataSourceUpdateFromClause() {

        // Create a DataSource
        PluginConfiguration dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, customizer().expectStatusOk(),
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setId(pls.get(0).getId());

        // Update the DataSource
        dataSource.getParameter(DataSourcePluginConstants.FROM_CLAUSE).value("from table where table.id>1000");

        // Define expectations
        RequestBuilderCustomizer expectations = customizer().expectStatusOk()
                .expectArrayContains(JSON_PATH_PLUGIN_CONNECTION,
                                     dataSource.getParameterValue(DataSourcePluginConstants.CONNECTION_PARAM))
                .expectArrayContains(JSON_PATH_FROM_CLAUSE,
                                     dataSource.getParameterValue(DataSourcePluginConstants.FROM_CLAUSE));
        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_120")
    @Purpose("The system allows to update a datasource by updating the connection")
    public void dataSourceUpdateDBConnection() throws ModuleException {

        // Create a DataSource
        performDefaultPost(DataSourceController.TYPE_MAPPING, createDataSourceWithFromClause(),
                           customizer().expectStatusOk(), "DataSource shouldn't be created.");

        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        PluginConfiguration dataSource = pls.get(0);

        // Update the DataSource
        PluginConfiguration otherDbConnection = pluginService
                .savePluginConfiguration(getPostGreSqlConnectionConfiguration());
        dataSource.getParameter(DataSourcePluginConstants.CONNECTION_PARAM).value(otherDbConnection.getBusinessId());

        // Define expectations
        RequestBuilderCustomizer expectations = customizer().expectStatusOk()
                .expectValue(JSON_PATH_LABEL, dataSource.getLabel())
                .expectArrayContains(JSON_PATH_PLUGIN_CONNECTION,
                                     dataSource.getParameterValue(DataSourcePluginConstants.CONNECTION_PARAM))
                .expectArrayContains(JSON_PATH_FROM_CLAUSE,
                                     dataSource.getParameterValue(DataSourcePluginConstants.FROM_CLAUSE));

        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_130")
    @Purpose("The system allows to delete a datasource")
    public void deleteDataSource() {
        // Create a DataSource
        final PluginConfiguration dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource,
                           customizer().expect(MockMvcResultMatchers.status().is2xxSuccessful()),
                           "DataSource should have been created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setId(pls.get(0).getId());

        performDefaultDelete(DataSourceController.TYPE_MAPPING + "/{pluginConfId}",
                             customizer().expectStatusNoContent(), "DataSource should have been deleted.",
                             dataSource.getBusinessId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_150")
    @Purpose("The system allows to get all datasources")
    public void getAllDataSources() {

        // Create a DataSource
        performDefaultPost(DataSourceController.TYPE_MAPPING, createDataSourceWithFromClause(),
                           customizer().expectStatusOk(), "DataSource shouldn't be created.");

        // Create a DataSource
        performDefaultPost(DataSourceController.TYPE_MAPPING, createDataSourceSingleTable(),
                           customizer().expectStatusOk(), "DataSource shouldn't be created.");

        performDefaultGet(DataSourceController.TYPE_MAPPING,
                          customizer().expectStatusOk()
                                  .expectArrayContains("$.[0].content." + PLUGIN_CONNECTION_PARAM_PATH,
                                                       pluginPostgreDbConnection.getBusinessId())
                                  .expectArrayContains("$.[1].content." + PLUGIN_CONNECTION_PARAM_PATH,
                                                       pluginPostgreDbConnection.getBusinessId()),
                          "DataSources shouldn't be retrieve.");
    }

    private PluginConfiguration createDataSourceWithFromClause() {
        Set<IPluginParam> parameters = IPluginParam
                .set(IPluginParam.build(DataSourcePluginConstants.FROM_CLAUSE, FROM_CLAUSE_TEST),
                     IPluginParam.build(DataSourcePluginConstants.MODEL_NAME_PARAM, DEFAULT_MODEL_NAME),
                     IPluginParam.build(DataSourcePluginConstants.MODEL_MAPPING_PARAM,
                                        PluginParameterTransformer.toJson(modelAttrMapping)),
                     IPluginParam.plugin(DataSourcePluginConstants.CONNECTION_PARAM,
                                         pluginPostgreDbConnection.getBusinessId()));
        return PluginUtils.getPluginConfiguration(parameters, MockDatasourcePlugin.class);
    }

    private PluginConfiguration createDataSourceSingleTable() {
        Set<IPluginParam> parameters = IPluginParam
                .set(IPluginParam.build(DataSourcePluginConstants.TABLE_PARAM, TABLE_NAME_TEST),
                     IPluginParam.build(DataSourcePluginConstants.MODEL_NAME_PARAM, DEFAULT_MODEL_NAME),
                     IPluginParam.build(DataSourcePluginConstants.MODEL_MAPPING_PARAM,
                                        PluginParameterTransformer.toJson(modelAttrMapping)),
                     IPluginParam.plugin(DataSourcePluginConstants.CONNECTION_PARAM,
                                         pluginPostgreDbConnection.getBusinessId()));
        return PluginUtils.getPluginConfiguration(parameters, MockDatasourcePlugin.class);
    }

    @Test
    @Ignore
    public void createDataSourceWithJson() {
        String dataSourceRequest = readJsonContract("request-datasource.json");

        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSourceRequest,
                           customizer().expectStatusOk()
                                   .expect(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue())),
                           "DataSource creation request error.");
    }

    private void buildModelAttributes() {
        modelAttrMapping = new ArrayList<AbstractAttributeMapping>();

        modelAttrMapping.add(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY, "id"));
        modelAttrMapping.add(new StaticAttributeMapping(AbstractAttributeMapping.LABEL, "name"));
        modelAttrMapping.add(new DynamicAttributeMapping("alt", "geometry", PropertyType.INTEGER, "altitude"));
        modelAttrMapping.add(new DynamicAttributeMapping("lat", "geometry", PropertyType.DOUBLE, "latitude"));
        modelAttrMapping.add(new DynamicAttributeMapping("long", "geometry", PropertyType.DOUBLE, "longitude"));
        modelAttrMapping.add(new DynamicAttributeMapping("creationDate1", "hello", PropertyType.DATE_ISO8601,
                "timeStampWithoutTimeZone"));
        modelAttrMapping.add(new DynamicAttributeMapping("creationDate2", "hello", PropertyType.DATE_ISO8601,
                "timeStampWithoutTimeZone"));
        modelAttrMapping.add(new DynamicAttributeMapping("date", "hello", PropertyType.DATE_ISO8601, "date"));
        modelAttrMapping.add(new DynamicAttributeMapping("timeStampWithTimeZone", "hello", PropertyType.DATE_ISO8601,
                "timeStampWithTimeZone"));
        modelAttrMapping.add(new DynamicAttributeMapping("isUpdate", "hello", PropertyType.BOOLEAN, "update"));
    }

    private PluginConfiguration getPostGreSqlConnectionConfiguration() {

        Set<IPluginParam> parameters = IPluginParam
                .set(IPluginParam.build(DBConnectionPluginConstants.USER_PARAM, dbUser),
                     IPluginParam.build(DBConnectionPluginConstants.PASSWORD_PARAM, dbPassword),
                     IPluginParam.build(DBConnectionPluginConstants.DB_HOST_PARAM, dbHost),
                     IPluginParam.build(DBConnectionPluginConstants.DB_PORT_PARAM, dbPort),
                     IPluginParam.build(DBConnectionPluginConstants.DB_NAME_PARAM, dbName));

        return PluginUtils.getPluginConfiguration(parameters, MockConnectionPlugin.class);
    }

}
