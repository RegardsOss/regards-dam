/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.datasource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.Index;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.DataSourceEntity;
import fr.cnes.regards.modules.datasources.utils.IDataSourceRepositoryTest;
import fr.cnes.regards.modules.datasources.utils.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;
import fr.cnes.regards.modules.datasources.utils.exceptions.DataSourcesPluginException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Christophe Mertz
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PostgreDataSourcePluginTestConfiguration.class })
@ComponentScan(basePackages = { "fr.cnes.regards.modules.datasources.utils" })
public class PostgreDataSourceFromSingleTablePluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourceFromSingleTablePluginTest.class);

    private static final String TENANT = "PGDB_TENANT";

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

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

    private IDataSourceFromSingleTablePlugin plgDBDataSource;

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    /**
     * JPA Repository
     */
    @Autowired
    IDataSourceRepositoryTest repository;

    /**
     * Resolve tenant at runtime
     */
    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Initialize the plugin's parameter
     *
     * @throws DataSourcesPluginException
     *
     * @throws JwtException
     * @throws PluginUtilsException
     */
    @Before
    public void setUp() throws DataSourcesPluginException {

        runtimeTenantResolver.forceTenant("DEFAULT");

        /*
         * Add data to the data source
         */
        repository.deleteAll();
        repository.save(new DataSourceEntity("azertyuiop", 12345, 1.10203045607080901234568790123456789, 45.5444544454,
                LocalDateTime.now(), true));
        repository.save(new DataSourceEntity("Toulouse", 110, 3.141592653589793238462643383279, -15.2323654654564654,
                LocalDateTime.now().minusDays(5), false));
        repository.save(new DataSourceEntity("Paris", 350, -3.141592653589793238462643383279502884197169399375105,
                25.565465465454564654654654, LocalDateTime.now().plusHours(10), false));

        /*
         * Initialize the DataSourceAttributeMapping
         */
        this.buildModelAttributes();

        /*
         * Instantiate the SQL DataSource plugin
         */
        List<PluginParameter> parameters;
        try {
            parameters = PluginParametersFactory.build()
                    .addParameterPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM,
                                                     getPostgreConnectionConfiguration())
                    .addParameter(PostgreDataSourcePlugin.MODEL_PARAM, adapter.toJson(dataSourceModelMapping))
                    .getParameters();
        } catch (PluginUtilsException e) {
            throw new DataSourcesPluginException(e.getMessage());
        }

        try {
            plgDBDataSource = PluginUtils.getPlugin(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                    Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        } catch (PluginUtilsException e) {
            throw new DataSourcesPluginException(e.getMessage());
        }

    }

    @Test
    public void getTables() {
        Map<String, Table> tables = plgDBDataSource.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());
    }

    @Test
    public void getColumnsAndIndices() {
        Assert.assertEquals(3, repository.count());

        Map<String, Table> tables = plgDBDataSource.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());

        Map<String, Column> columns = plgDBDataSource.getColumns(tables.get(TABLE_NAME_TEST));
        Assert.assertNotNull(columns);
        Assert.assertEquals(7, columns.size());

        Map<String, Index> indices = plgDBDataSource.getIndexes(tables.get(TABLE_NAME_TEST));
        Assert.assertNotNull(indices);
        Assert.assertEquals(3, indices.size());
    }

    @Test
    public void getDataSourceIntrospection() {
        Assert.assertEquals(3, repository.count());

        plgDBDataSource.setMapping(TABLE_NAME_TEST, dataSourceModelMapping);

        Page<DataObject> ll = plgDBDataSource.findAll(TENANT, new PageRequest(0, 2));
        Assert.assertNotNull(ll);
        Assert.assertEquals(2, ll.getContent().size());

        ll = plgDBDataSource.findAll(TENANT, new PageRequest(1, 2));
        Assert.assertNotNull(ll);
        Assert.assertEquals(1, ll.getContent().size());
    }

    @After
    public void erase() {
        // repository.deleteAll();
    }

    /**
     * Define the {@link PluginConfiguration} for a {@link DefaultPostgreConnectionPlugin} to connect to the PostgreSql
     * database.
     *
     * @return the {@link PluginConfiguration}
     * @throws PluginUtilsException
     */
    private PluginConfiguration getPostgreConnectionConfiguration() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultPostgreConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private void buildModelAttributes() {
        List<DataSourceAttributeMapping> attributes = new ArrayList<DataSourceAttributeMapping>();

        attributes.add(new DataSourceAttributeMapping("id", AttributeType.LONG, "id", true));
        attributes.add(new DataSourceAttributeMapping("name", AttributeType.STRING, "label"));
        attributes.add(new DataSourceAttributeMapping("alt", "geometry", AttributeType.INTEGER, "altitude"));
        attributes.add(new DataSourceAttributeMapping("lat", "geometry", AttributeType.DOUBLE, "latitude"));
        attributes.add(new DataSourceAttributeMapping("long", "geometry", AttributeType.DOUBLE, "longitude"));
        attributes.add(new DataSourceAttributeMapping("creationDate", "hello", AttributeType.DATE_ISO8601, "date"));
        attributes.add(new DataSourceAttributeMapping("isUpdate", "hello", AttributeType.BOOLEAN, "update"));

        dataSourceModelMapping = new DataSourceModelMapping("ModelDeTest", attributes);
    }

}
