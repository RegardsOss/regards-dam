/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.datasource;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.OracleDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.domain.AttributeMappingAdapter;
import fr.cnes.regards.modules.datasources.plugins.domain.Column;
import fr.cnes.regards.modules.datasources.plugins.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.domain.Index;
import fr.cnes.regards.modules.datasources.plugins.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.DataSourceUtilsException;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;
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
public class OracleDBDataSourcePluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(OracleDBDataSourcePluginTest.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "T_DATA_OBJECTS";

    @Value("${oracle.datasource.url}")
    private String url;

    @Value("${oracle.datasource.username}")
    private String user;

    @Value("${oracle.datasource.password}")
    private String password;

    @Value("${oracle.datasource.driver}")
    private String driver;

    private IDBDataSourcePlugin plgDBDataSource;

    private List<DataSourceAttributeMapping> attributes = new ArrayList<DataSourceAttributeMapping>();

    private final AttributeMappingAdapter adapter = new AttributeMappingAdapter();

    /**
     * Initialize the plugin's parameter
     * 
     * @throws DataSourceUtilsException
     * 
     * @throws JwtException
     * @throws PluginUtilsException
     */
    @Before
    public void setUp() throws DataSourceUtilsException {

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
                    .addParameterPluginConfiguration(OracleDBDataSourcePlugin.CONNECTION_PARAM,
                                                     getOracleConnectionConfiguration())
                    .addParameter(PostgreDataSourcePlugin.MODEL_PARAM, adapter.toJson(attributes)).getParameters();
        } catch (PluginUtilsException e) {
            throw new DataSourceUtilsException(e.getMessage());
        }

        try {
            plgDBDataSource = PluginUtils.getPlugin(parameters, OracleDBDataSourcePlugin.class,
                                                    Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        } catch (PluginUtilsException e) {
            throw new DataSourceUtilsException(e.getMessage());
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
        Map<String, Table> tables = plgDBDataSource.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());

        Map<String, Column> columns = plgDBDataSource.getColumns(tables.get(TABLE_NAME_TEST));
        Assert.assertNotNull(columns);

        Map<String, Index> indices= plgDBDataSource.getIndices(tables.get(TABLE_NAME_TEST));
        Assert.assertNotNull(indices);
    }

    @Test
    public void getDataSourceIntrospection() {
        // Assert.assertEquals(3, repository.count());
        //
        // plgDBDataSource.setMapping(TABLE_NAME_TEST, "id", "altitude", "latitude", "longitude", "label");
        //
        // Page<AbstractEntity> ll = plgDBDataSource.findAll(new PageRequest(0, 2));
        // Assert.assertNotNull(ll);
        // Assert.assertEquals(2, ll.getContent().size());
        //
        // ll = plgDBDataSource.findAll(new PageRequest(1, 2));
        // Assert.assertNotNull(ll);
        // Assert.assertEquals(1, ll.getContent().size());
    }

    @After
    public void erase() {
        // repository.deleteAll();
    }

    /**
     * Define the {@link PluginConfiguration} for a {@link DefaultOracleConnectionPlugin} to connect to the Oracle
     * database.
     * 
     * @return the {@link PluginConfiguration}
     * @throws PluginUtilsException
     */
    private PluginConfiguration getOracleConnectionConfiguration() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(DefaultOracleConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultOracleConnectionPlugin.DRIVER_PARAM, driver)
                .addParameter(DefaultOracleConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultOracleConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultOracleConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private void buildModelAttributes() {
        attributes.add(new DataSourceAttributeMapping("name", AttributeType.STRING, "label"));
        attributes.add(new DataSourceAttributeMapping("alt", AttributeType.INTEGER, "altitude", "geometry"));
        attributes.add(new DataSourceAttributeMapping("lat", AttributeType.DOUBLE, "latitude", "geometry"));
        attributes.add(new DataSourceAttributeMapping("long", AttributeType.DOUBLE, "longitude", "geometry"));
        attributes.add(new DataSourceAttributeMapping("creationDate", AttributeType.DATE_ISO8601, "date", "hello"));
        attributes.add(new DataSourceAttributeMapping("isUpdate", AttributeType.BOOLEAN, "update", "hello"));

    }

}
