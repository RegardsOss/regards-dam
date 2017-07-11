/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.datasource;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
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
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.DynamicAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.domain.StaticAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.DataSourceEntity;
import fr.cnes.regards.modules.datasources.utils.IDataSourceRepositoryTest;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;
import fr.cnes.regards.modules.datasources.utils.exceptions.DataSourcesPluginException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.plugins.utils.PluginUtils;

/**
 * @author Christophe Mertz
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PostgreDataSourcePluginTestConfiguration.class })
@ComponentScan(basePackages = { "fr.cnes.regards.modules.datasources.utils" })
public class PostgreDataSourcePluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourcePluginTest.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TENANT = "PG_TENANT";

    private static final String HELLO = "hello world from ";

    private static final String NAME_ATTR = "name";

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

    private IDataSourcePlugin plgDBDataSource;

    private DataSourceModelMapping modelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    private static int nbElements;

    private Map<Long, Object> pluginCacheMap = new HashMap<>();

    /**
     * JPA Repository
     */
    @Autowired
    IDataSourceRepositoryTest repository;

    /**
     * Populate the datasource as a legacy catalog
     *
     * @throws DataSourcesPluginException
     * @throws SQLException
     */
    @Before
    public void setUp() throws DataSourcesPluginException, SQLException {
        /*
         * Add data to the data source
         */
        repository.deleteAll();
        repository.save(new DataSourceEntity("azertyuiop", 12345, 1.10203045607080901234568790123456789, 45.5444544454,
                                             LocalDate.now().minusDays(10), LocalTime.now().minusHours(9),
                                             LocalDateTime.now(), OffsetDateTime.now().minusMinutes(33), true));
        repository.save(new DataSourceEntity("Toulouse", 110, 3.141592653589793238462643383279, -15.2323654654564654,
                                             LocalDate.now().minusMonths(1), LocalTime.now().minusMinutes(10),
                                             LocalDateTime.now().plusHours(33), OffsetDateTime.now().minusSeconds(22),
                                             true));
        repository.save(new DataSourceEntity("Paris", 350, -3.141592653589793238462643383279502884197169399375105,
                                             25.565465465454564654654654, LocalDate.now().minusDays(10),
                                             LocalTime.now().minusHours(9), LocalDateTime.now().minusMonths(2),
                                             OffsetDateTime.now().minusHours(7), false));
        nbElements = 3;

        /*
         * Initialize the AbstractAttributeMapping
         */
        buildModelAttributes();

        /*
         * Instantiate the data source plugin
         */
        List<PluginParameter> parameters;
        parameters = PluginParametersFactory.build()
                .addParameterPluginConfiguration(PostgreDataSourcePlugin.CONNECTION_PARAM,
                                                 getPostgreConnectionConfiguration())
                .addParameter(PostgreDataSourcePlugin.MODEL_PARAM, adapter.toJson(modelMapping))
                .addParameter(PostgreDataSourcePlugin.FROM_CLAUSE, "from\n\n\nT_TEST_PLUGIN_DATA_SOURCE")
                .getParameters();

        plgDBDataSource = PluginUtils
                .getPlugin(parameters, PostgreDataSourcePlugin.class, Arrays.asList(PLUGIN_CURRENT_PACKAGE),
                           pluginCacheMap);

        // Do not launch tests is Database is not available
        Assume.assumeTrue(plgDBDataSource.getDBConnection().testConnection());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_100")
    @Requirement("REGARDS_DSL_DAM_SRC_110")
    @Requirement("REGARDS_DSL_DAM_SRC_140")
    @Requirement("REGARDS_DSL_DAM_PLG_200")
    @Purpose(
            "The system has a plugin that enables to define a datasource to a PostreSql database by setting a SQL request")
    public void getDataSourceIntrospection() throws SQLException, DataSourceException {
        Assert.assertEquals(nbElements, repository.count());

        Page<DataObject> ll = plgDBDataSource.findAll(TENANT, new PageRequest(0, 10));
        Assert.assertNotNull(ll);
        Assert.assertEquals(nbElements, ll.getContent().size());

        ll.getContent().forEach(dataObj -> {
            LOG.info("------------------->");
            dataObj.getProperties().forEach(attr -> {
                LOG.info(attr.getName() + " : " + attr.getValue());
                if (attr.getName().equals(NAME_ATTR)) {
                    Assert.assertTrue(attr.getValue().toString().contains(HELLO));
                }
            });
        });

        ll.getContent().forEach(d -> Assert.assertNotNull(d.getIpId()));
        ll.getContent().forEach(d -> Assert.assertNotNull(d.getSipId()));
        ll.getContent().forEach(d -> Assert.assertTrue(0 < d.getProperties().size()));
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_ARC_120")
    @Requirement("REGARDS_DSL_DAM_ARC_130")
    @Purpose("The system allows to define a request to a data source to get a subset of the data")
    public void getDataSourceIntrospectionFromPastDate() throws SQLException, DataSourceException {
        Assert.assertEquals(nbElements, repository.count());

        OffsetDateTime date = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).minusMinutes(2);
        Page<DataObject> ll = plgDBDataSource.findAll(TENANT, new PageRequest(0, 10), date);
        Assert.assertNotNull(ll);
        Assert.assertEquals(1, ll.getContent().size());

        ll.getContent().forEach(dataObj -> {
            LOG.info("------------------->");
            dataObj.getProperties().forEach(attr -> {
                LOG.info(attr.getName() + " : " + attr.getValue());
                if (attr.getName().equals(NAME_ATTR)) {
                    Assert.assertTrue(attr.getValue().toString().contains(HELLO));
                }
            });
        });

        ll.getContent().forEach(d -> Assert.assertNotNull(d.getIpId()));
        ll.getContent().forEach(d -> Assert.assertNotNull(d.getSipId()));
        ll.getContent().forEach(d -> Assert.assertTrue(0 < d.getProperties().size()));
    }

    @Test
    public void getDataSourceIntrospectionFromFutureDate() throws SQLException, DataSourceException {
        Assert.assertEquals(nbElements, repository.count());

        OffsetDateTime ldt = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).plusSeconds(10);
        Page<DataObject> ll = plgDBDataSource.findAll(TENANT, new PageRequest(0, 10), ldt);
        Assert.assertNotNull(ll);
        Assert.assertEquals(0, ll.getContent().size());
    }

    @After
    public void erase() {
        // repository.deleteAll();
    }

    /**
     * Define the {@link PluginConfiguration} for a {@link DefaultPostgreConnectionPlugin} to connect to the PostgreSql
     * database
     *
     * @return the {@link PluginConfiguration} @
     */
    private PluginConfiguration getPostgreConnectionConfiguration() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultPostgreConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        PluginConfiguration plgConf = PluginUtils
                .getPluginConfiguration(parameters, DefaultPostgreConnectionPlugin.class,
                                        Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        pluginCacheMap.put(plgConf.getId(), PluginUtils
                .getPlugin(plgConf, plgConf.getPluginClassName(), Arrays.asList(PLUGIN_CURRENT_PACKAGE),
                           pluginCacheMap));
        return plgConf;
    }

    private void buildModelAttributes() {
        List<AbstractAttributeMapping> attributes = new ArrayList<AbstractAttributeMapping>();

        attributes.add(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY, AttributeType.LONG, "id"));
        attributes.add(new DynamicAttributeMapping(NAME_ATTR, AttributeType.STRING,
                                                   "'" + HELLO + "--> '||label as label"));
        attributes.add(new DynamicAttributeMapping("alt", "geometry", AttributeType.INTEGER, "altitude AS altitude"));
        attributes.add(new DynamicAttributeMapping("lat", "geometry", AttributeType.DOUBLE, "latitude"));
        attributes.add(new DynamicAttributeMapping("long", "geometry", AttributeType.DOUBLE, "longitude"));
        attributes.add(new DynamicAttributeMapping("creationDate1", "hello", AttributeType.DATE_ISO8601,
                                                   "timeStampWithoutTimeZone"));
        attributes.add(new DynamicAttributeMapping("creationDate2", "hello", AttributeType.DATE_ISO8601,
                                                   "timeStampWithoutTimeZone"));
        attributes.add(new DynamicAttributeMapping("date", "hello", AttributeType.DATE_ISO8601, "date"));
        attributes.add(new StaticAttributeMapping(AbstractAttributeMapping.LAST_UPDATE, AttributeType.DATE_ISO8601,
                                                  "timeStampWithTimeZone"));
        attributes.add(new DynamicAttributeMapping("isUpdate", "hello", AttributeType.BOOLEAN, "update"));

        modelMapping = new DataSourceModelMapping(123L, attributes);
    }

}
