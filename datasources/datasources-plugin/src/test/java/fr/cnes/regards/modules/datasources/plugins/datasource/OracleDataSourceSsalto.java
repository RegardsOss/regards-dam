/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.datasource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.DynamicAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.domain.StaticAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.OracleDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.exceptions.DataSourcesPluginException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.plugins.utils.PluginUtils;

/**
 * @author Christophe Mertz
 */
@RunWith(SpringRunner.class)
@TestPropertySource(locations = { "classpath:datasource-test.properties" })
@ComponentScan(basePackages = { "fr.cnes.regards.modules.datasources.utils" })
@Ignore
public class OracleDataSourceSsalto {

    private static final Logger LOG = LoggerFactory.getLogger(OracleDataSourceSsalto.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "T_DATA_OBJECTS";

    private static final String TENANT = "SSALTO_TENANT";

    private String dbHost = "172.26.8.122";

    private String dbPort = "1521";

    private String dbName = "SIPAD";

    private String dbUser = "ssalto_dba";

    private String dbPassword = "ssalto_dba";

    private IDataSourceFromSingleTablePlugin plgDBDataSource;

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    private Map<Long, Object> pluginCacheMap = new HashMap<>();

    /**
     * Initialize the plugin's parameter
     *
     * @throws DataSourcesPluginException
     * @throws SQLException
     */
    @Before
    public void setUp() throws DataSourcesPluginException, SQLException {

        /*
         * Initialize the AbstractAttributeMapping
         */
        buildModelAttributes();

        /*
         * Instantiate the SQL DataSource plugin
         */
        List<PluginParameter> parameters;

        parameters = PluginParametersFactory.build()
                .addParameterPluginConfiguration(OracleDataSourceFromSingleTablePlugin.CONNECTION_PARAM,
                                                 getOracleConnectionConfiguration())
                .addParameter(PostgreDataSourceFromSingleTablePlugin.REFRESH_RATE, "1800")
                .addParameter(OracleDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST)
                .addParameter(OracleDataSourceFromSingleTablePlugin.MODEL_PARAM, adapter.toJson(dataSourceModelMapping))
                .getParameters();

        plgDBDataSource = PluginUtils.getPlugin(parameters, OracleDataSourceFromSingleTablePlugin.class,
                                                Arrays.asList(PLUGIN_CURRENT_PACKAGE), pluginCacheMap);

        // Do not launch tests is Database is not available
        Assume.assumeTrue(plgDBDataSource.getDBConnection().testConnection());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_100")
    @Requirement("REGARDS_DSL_DAM_SRC_110")
    @Requirement("REGARDS_DSL_DAM_SRC_140")
    @Purpose("The system allows to create a plugin to get a subset of the datasource's data")
    public void getDataSourceIntrospection() throws SQLException, DataSourceException {
        Page<DataObject> ll = plgDBDataSource.findAll(TENANT, new PageRequest(0, 1000));
        Assert.assertNotNull(ll);
        Assert.assertEquals(1000, ll.getContent().size());

        ll.getContent().forEach(d -> Assert.assertNotNull(d.getIpId()));
        ll.getContent().forEach(d -> Assert.assertNotNull(d.getSipId()));
        ll.getContent().forEach(d -> Assert.assertTrue(0 < d.getProperties().size()));

        ll = plgDBDataSource.findAll(TENANT, new PageRequest(1, 1000));
        Assert.assertNotNull(ll);
        Assert.assertEquals(1000, ll.getContent().size());

        ll.getContent().forEach(d -> Assert.assertNotNull(d.getIpId()));
        ll.getContent().forEach(d -> Assert.assertNotNull(d.getSipId()));
        ll.getContent().forEach(d -> Assert.assertTrue(0 < d.getProperties().size()));
    }

    /**
     * Define the {@link PluginConfiguration} for a {@link DefaultOracleConnectionPlugin} to connect to the Oracle
     * database.
     *
     * @return the {@link PluginConfiguration} @
     */
    private PluginConfiguration getOracleConnectionConfiguration() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultOracleConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultOracleConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultOracleConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultOracleConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultOracleConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        PluginConfiguration plgConfConn = PluginUtils
                .getPluginConfiguration(parameters, DefaultOracleConnectionPlugin.class,
                                        Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        pluginCacheMap.put(plgConfConn.getId(), (Object)PluginUtils
                .getPlugin(plgConfConn, plgConfConn.getPluginClassName(), Arrays.asList(PLUGIN_CURRENT_PACKAGE),
                           pluginCacheMap));
        return plgConfConn;
    }

    private void buildModelAttributes() {
        List<AbstractAttributeMapping> attributes = new ArrayList<AbstractAttributeMapping>();

        attributes.add(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY, AttributeType.INTEGER,
                                                  "DATA_OBJECTS_ID"));
        attributes.add(new StaticAttributeMapping(AbstractAttributeMapping.LABEL, "NODE_IDENTIFIER"));
        attributes.add(new DynamicAttributeMapping("PHASE", AttributeType.STRING, "PHASE"));
        attributes.add(new DynamicAttributeMapping("PRODUCT_OPTION", AttributeType.STRING, "PRODUCT_OPTION"));
        attributes.add(new DynamicAttributeMapping("PARAMETER", AttributeType.STRING, "PARAMETER"));
        attributes.add(new DynamicAttributeMapping("RADICAL", AttributeType.STRING, "RADICAL"));
        attributes.add(new DynamicAttributeMapping("FILE_CREATION_DATE", AttributeType.DATE_ISO8601, "FILE_CREATION_DATE"));
        attributes.add(new DynamicAttributeMapping("OBJECT_VERSION", AttributeType.STRING, "VERSION"));
        attributes.add(new DynamicAttributeMapping("FILE_SIZE", AttributeType.INTEGER, "FILE_SIZE"));
        
        attributes.add(new DynamicAttributeMapping("START_DATE", "TIME_PERIOD", AttributeType.DATE_ISO8601, "START_DATE"));
        attributes.add(new DynamicAttributeMapping("STOP_DATE", "TIME_PERIOD", AttributeType.DATE_ISO8601, "STOP_DATE"));
        
        attributes.add(new DynamicAttributeMapping("CYCLE_MAX", "CYCLE_RANGE", AttributeType.INTEGER, "CYCLE_MAX"));
        attributes.add(new DynamicAttributeMapping("CYCLE_MIN", "CYCLE_RANGE", AttributeType.INTEGER, "CYCLE_MIN"));
        
        attributes.add(new DynamicAttributeMapping("ORBIT_MAX", "CYCLE_RANGE", AttributeType.INTEGER, "ORBIT_MAX"));
        attributes.add(new DynamicAttributeMapping("ORBIT_MIN", "CYCLE_RANGE", AttributeType.INTEGER, "ORBIT_MIN"));
        
        attributes.add(new DynamicAttributeMapping("LATITUDE_MIN", "GEO_CORDINATES", AttributeType.INTEGER, "MIN_LATITUDE"));
        attributes.add(new DynamicAttributeMapping("LATITUDE_MAX", "GEO_CORDINATES", AttributeType.INTEGER, "MAX_LATITUDE"));
        attributes.add(new DynamicAttributeMapping("LONGITUDE_MIN", "GEO_CORDINATES", AttributeType.INTEGER, "MIN_LONGITUDE"));
        attributes.add(new DynamicAttributeMapping("LONGITUDE_MAX", "GEO_CORDINATES", AttributeType.INTEGER, "MAX_LONGITUDE"));
        
        dataSourceModelMapping = new DataSourceModelMapping(123L, attributes);
    }

}
