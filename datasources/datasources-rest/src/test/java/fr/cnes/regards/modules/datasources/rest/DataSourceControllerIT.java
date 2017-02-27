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
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.service.IDataSourceService;
import fr.cnes.regards.modules.datasources.utils.ModelMappingAdapter;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 *
 * Test {@link DataSource} controller
 *
 * @author Christophe Mertz
 *
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

    private static final String LABEL_DATA_SOURCE = "the label of the data source";

    private final static String JSON_PATH_LABEL = "$.content.label";

    private final static String JSON_PATH_MAPPING = "$.content.mapping";

    private final static String JSON_PATH_FROM_CLAUSE = "$.content.fromClause";

    private final static String JSON_PATH_TABLE_NAME = "$.content.tableName";

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
    IPluginService pluginService;

    @Autowired
    IDataSourceService dataSourceService;

    PluginConfiguration pluginPostgreDbConnection;

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Before
    public void setUp() throws ModuleException, PluginUtilsException {
        /*
         * Initialize the DataSourceAttributeMapping
         */
        this.buildModelAttributes();

        /*
         * Save a PluginConfiguration for plugin's type IDBConnectionPlugin
         */
        pluginPostgreDbConnection = pluginService.savePluginConfiguration(getPostGreSqlConnectionConfiguration());
    }

    @Test
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
    public void updateDataSource() {
        final List<ResultMatcher> expectations = new ArrayList<>();

        // Create a DataSource
        final DataSource dataSource = createDataSourceWithFromClause();
        expectations.add(MockMvcResultMatchers.status().isOk());
        performDefaultPost(DataSourceController.TYPE_MAPPING, dataSource, expectations,
                           "DataSource shouldn't be created.");
        List<PluginConfiguration> pls = pluginService.getPluginConfigurationsByType(IDataSourcePlugin.class);
        dataSource.setPluginConfigurationId(pls.get(0).getId());

        // Define expectations
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultPut(DataSourceController.TYPE_MAPPING + "/{pPluginConfId}", dataSource, expectations,
                          "DataSource shouldn't be created.", dataSource.getPluginConfigurationId());
    }

    private DataSource createDataSourceWithFromClause() {
        final DataSource dataSource = new DataSource();
        dataSource.setFromClause("from T_TEST_PLUGIN_DATA_SOURCE");
        dataSource.setPluginClassName(PostgreDataSourcePlugin.class.getCanonicalName());
        dataSource.setPluginConfigurationConnectionId(pluginPostgreDbConnection.getId());
        dataSource.setMapping(dataSourceModelMapping);
        dataSource.setLabel(LABEL_DATA_SOURCE);

        return dataSource;
    }

    private DataSource createDataSourceSingleTable() {
        final DataSource dataSource = new DataSource();
        dataSource.setTableName(TABLE_NAME_TEST);
        dataSource.setPluginClassName(PostgreDataSourceFromSingleTablePlugin.class.getCanonicalName());
        dataSource.setPluginConfigurationConnectionId(pluginPostgreDbConnection.getId());
        dataSource.setMapping(dataSourceModelMapping);
        dataSource.setLabel(LABEL_DATA_SOURCE);

        return dataSource;
    }

    private void buildModelAttributes() {
        List<DataSourceAttributeMapping> attributes = new ArrayList<DataSourceAttributeMapping>();

        attributes.add(new DataSourceAttributeMapping("id", AttributeType.LONG, "id", true));
        attributes
                .add(new DataSourceAttributeMapping("name", AttributeType.STRING, "'Hello Toulouse-'||label as label"));
        attributes.add(new DataSourceAttributeMapping("alt", "geometry", AttributeType.INTEGER, "altitude"));
        attributes.add(new DataSourceAttributeMapping("lat", "geometry", AttributeType.DOUBLE, "latitude"));
        attributes.add(new DataSourceAttributeMapping("long", "geometry", AttributeType.DOUBLE, "longitude"));
        attributes.add(new DataSourceAttributeMapping("creationDate", "hello", AttributeType.DATE_ISO8601, "date"));
        attributes.add(new DataSourceAttributeMapping("isUpdate", "hello", AttributeType.BOOLEAN, "update"));

        dataSourceModelMapping = new DataSourceModelMapping(123L, attributes);
    }

    private PluginConfiguration getPostGreSqlConnectionConfiguration() throws PluginUtilsException {
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
