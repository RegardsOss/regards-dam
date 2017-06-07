/*
 * LICENSE_PLACEHOLDER
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
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.datasources.domain.*;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.service.IDataSourceService;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.plugins.utils.PluginUtils;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test {@link DataSource} controller
 *
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

    private final static String JSON_PATH_FROM_CLAUSE = "$.content.fromClause";

    private final static String JSON_PATH_TABLE_NAME = "$.content.tableName";

    private final static String JSON_PATH_PLUGIN_CONNECTION = "$.content.pluginConfigurationConnectionId";

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

    private PluginConfiguration pluginPostgreDbConnection;

    private DataSourceModelMapping modelMapping;

    private final static ModelMappingAdapter adapter = new ModelMappingAdapter();

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Before
    public void setUp() throws ModuleException, JwtException {
        jwtService.injectToken(DEFAULT_TENANT, DEFAULT_ROLE, DEFAULT_USER_EMAIL);

        pluginConfRepos.deleteAll();

        /*
         * Initialize the AbstractAttributeMapping
         */
        buildModelAttributes();

        /*
         * Save a PluginConfiguration for plugin's type IDBConnectionPlugin
         */
        pluginPostgreDbConnection = pluginService.savePluginConfiguration(getPostGreSqlConnectionConfiguration());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_110")
    @Purpose("The system allows to define a datasource by setting a SQL request")
    public void createDataSourceWithFromClauseTest() {
        final DataSource dataSource = createDataSourceWithFromClause();

        Assert.assertEquals(0, dataSourceService.getAllDataSources().size());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_FROM_CLAUSE,
                                                        Matchers.equalTo(dataSource.getFromClause())));

        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");

        Assert.assertEquals(1, dataSourceService.getAllDataSources().size());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_100")
    @Purpose("The system allows to define a datasource by the configuration a plugin's type IDataSourcePlugin")
    public void createDataSourceWithSingleTableTest() {
        final DataSource dataSource = createDataSourceSingleTable();

        Assert.assertEquals(0, dataSourceService.getAllDataSources().size());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations
                .add(MockMvcResultMatchers.jsonPath(JSON_PATH_TABLE_NAME, Matchers.equalTo(dataSource.getTableName())));

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
        final DataSource dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setPluginConfigurationId(pls.get(0).getId());

        // Define expectations
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_PLUGIN_CONNECTION,
                          Matchers.hasToString(dataSource.getPluginConfigurationConnectionId().toString())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_FROM_CLAUSE,
                                                        Matchers.equalTo(dataSource.getFromClause())));

        performDefaultGet(DataSourceController.TYPE_MAPPING + "/{pPluginConfId}", expectations,
                          "DataSource shouldn't be retrieve.", dataSource.getPluginConfigurationId());
    }

    @Test
    public void getUnknownDataSource() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        performDefaultGet(DataSourceController.TYPE_MAPPING + "/{pPluginConfId}", expectations,
                          "DataSource shouldn't be retrieve.", Long.MAX_VALUE);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_120")
    @Purpose("The system allows to update a datasource by updating the SQL request")
    public void dataSourceUpdateFromClause() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Create a DataSource
        final DataSource dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setPluginConfigurationId(pls.get(0).getId());

        // Update the DataSource
        dataSource.setFromClause("from table where table.id>1000");

        // Define expectations
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_PLUGIN_CONNECTION,
                          Matchers.hasToString(dataSource.getPluginConfigurationConnectionId().toString())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_FROM_CLAUSE,
                                                        Matchers.equalTo(dataSource.getFromClause())));

        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pPluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getPluginConfigurationId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_120")
    @Purpose("The system allows to update a datasource by updating the connection")
    public void dataSourceUpdateDBConnection() throws ModuleException {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Create a DataSource
        final DataSource dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setPluginConfigurationId(pls.get(0).getId());

        // Update the DataSource
        PluginConfiguration otherDbConnection = pluginService
                .savePluginConfiguration(getPostGreSqlConnectionConfiguration());
        dataSource.setPluginConfigurationConnectionId(otherDbConnection.getId());

        // Define expectations
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_PLUGIN_CONNECTION,
                          Matchers.hasToString(dataSource.getPluginConfigurationConnectionId().toString())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_FROM_CLAUSE,
                                                        Matchers.equalTo(dataSource.getFromClause())));

        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pPluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getPluginConfigurationId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_120")
    @Purpose("The system allows to update a datasource by updating the table and the mapping")
    public void dataSourceChangeFromClauseToSimpleTable() {
        final List<ResultMatcher> expectations = new ArrayList<>();

        // Create a DataSource
        final DataSource dataSource = createDataSourceWithFromClause();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_FROM_CLAUSE,
                                                        Matchers.equalTo(dataSource.getFromClause())));
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setPluginConfigurationId(pls.get(0).getId());

        // Update the DataSource
        dataSource.setFromClause(null);
        dataSource.setTableName(TABLE_NAME_TEST);

        // Define expectations
        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_PLUGIN_CONNECTION,
                          Matchers.hasToString(dataSource.getPluginConfigurationConnectionId().toString())));
        expectations
                .add(MockMvcResultMatchers.jsonPath(JSON_PATH_TABLE_NAME, Matchers.equalTo(dataSource.getTableName())));

        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pPluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getPluginConfigurationId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_120")
    @Purpose("The system allows to update a datasource by switching from a table configuration to a SQL request configuration")
    public void dataSourceChangeSimpleTableToFromClause() {
        final List<ResultMatcher> expectations = new ArrayList<>();

        // Create a DataSource
        final DataSource dataSource = createDataSourceSingleTable();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations
                .add(MockMvcResultMatchers.jsonPath(JSON_PATH_TABLE_NAME, Matchers.equalTo(dataSource.getTableName())));
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setPluginConfigurationId(pls.get(0).getId());

        // Update the DataSource
        dataSource.setFromClause(FROM_CLAUSE_TEST);
        dataSource.setTableName(null);

        // Define expectations
        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_LABEL, Matchers.equalTo(dataSource.getLabel())));
        expectations.add(MockMvcResultMatchers
                .jsonPath(JSON_PATH_PLUGIN_CONNECTION,
                          Matchers.hasToString(dataSource.getPluginConfigurationConnectionId().toString())));
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_FROM_CLAUSE,
                                                        Matchers.equalTo(dataSource.getFromClause())));

        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pPluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getPluginConfigurationId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_130")
    @Purpose("The system allows to delete a datasource")
    public void deleteDataSource() {
        final List<ResultMatcher> expectations = new ArrayList<>();

        // Create a DataSource
        final DataSource dataSource = createDataSourceWithFromClause();
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setPluginConfigurationId(pls.get(0).getId());

        // Define expectations
        expectations.add(status().isNoContent());

        performDefaultDelete(DataSourceController.TYPE_MAPPING + "/{pPluginConfId}", expectations,
                             "DataSource shouldn't be deleted.", dataSource.getPluginConfigurationId());
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
                .jsonPath("$.[0].content.pluginConfigurationConnectionId",
                          Matchers.hasToString(pluginPostgreDbConnection.getId().toString())));
        expectations.add(MockMvcResultMatchers
                .jsonPath("$.[1].content.pluginConfigurationConnectionId",
                          Matchers.hasToString(pluginPostgreDbConnection.getId().toString())));

        performDefaultGet(DataSourceController.TYPE_MAPPING, expectations, "DataSources shouldn't be retrieve.");
    }

    private DataSource createDataSourceWithFromClause() {
        final DataSource dataSource = new DataSource();
        dataSource.setFromClause(FROM_CLAUSE_TEST);
        dataSource.setPluginClassName(PostgreDataSourcePlugin.class.getCanonicalName());
        dataSource.setPluginConfigurationConnectionId(pluginPostgreDbConnection.getId());
        dataSource.setMapping(modelMapping);
        dataSource.setLabel(LABEL_DATA_SOURCE + " with from clause");

        return dataSource;
    }

    private DataSource createDataSourceSingleTable() {
        final DataSource dataSource = new DataSource();
        dataSource.setTableName(TABLE_NAME_TEST);
        dataSource.setPluginClassName(PostgreDataSourceFromSingleTablePlugin.class.getCanonicalName());
        dataSource.setPluginConfigurationConnectionId(pluginPostgreDbConnection.getId());
        dataSource.setMapping(modelMapping);
        dataSource.setLabel(LABEL_DATA_SOURCE + " with table name");

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
        List<AbstractAttributeMapping> attributes = new ArrayList<AbstractAttributeMapping>();

        attributes.add(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY, "id"));
        attributes.add(new StaticAttributeMapping(AbstractAttributeMapping.LABEL, "name"));
        attributes.add(new DynamicAttributeMapping("alt", "geometry", AttributeType.INTEGER, "altitude"));
        attributes.add(new DynamicAttributeMapping("lat", "geometry", AttributeType.DOUBLE, "latitude"));
        attributes.add(new DynamicAttributeMapping("long", "geometry", AttributeType.DOUBLE, "longitude"));
        attributes.add(new DynamicAttributeMapping("creationDate1", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithoutTimeZone"));
        attributes.add(new DynamicAttributeMapping("creationDate2", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithoutTimeZone"));
        attributes.add(new DynamicAttributeMapping("date", "hello", AttributeType.DATE_ISO8601, "date"));
        attributes.add(new DynamicAttributeMapping("timeStampWithTimeZone", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithTimeZone"));
        attributes.add(new DynamicAttributeMapping("isUpdate", "hello", AttributeType.BOOLEAN, "update"));

        modelMapping = new DataSourceModelMapping(123L, attributes);
    }

    private PluginConfiguration getPostGreSqlConnectionConfiguration() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultPostgreConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_PACKAGE));
    }

}
