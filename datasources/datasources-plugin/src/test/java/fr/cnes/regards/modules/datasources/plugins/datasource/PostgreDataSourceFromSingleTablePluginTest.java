/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.datasource;

import java.sql.SQLException;
import java.sql.Types;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DynamicAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.StaticAttributeMapping;
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
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.DataSourceEntity;
import fr.cnes.regards.modules.datasources.utils.IDataSourceRepositoryTest;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
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
public class PostgreDataSourceFromSingleTablePluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourceFromSingleTablePluginTest.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TENANT = "PGDB_TENANT";

    private static final String HELLO = "Hello Toulouse";

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

    private DataSourceModelMapping modelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    private static int nbElements;

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
     * @throws JwtException @
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
                .addParameterPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM,
                                                 getPostgreConnectionConfiguration())
                .addParameter(PostgreDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_PARAM, adapter.toJson(modelMapping))
                .addParameter(PostgreDataSourceFromSingleTablePlugin.REFRESH_RATE, "1800").getParameters();

        plgDBDataSource = PluginUtils.getPlugin(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                Arrays.asList(PLUGIN_CURRENT_PACKAGE));

        // Do not launch tests is Database is not available
        Assume.assumeTrue(plgDBDataSource.getDBConnection().testConnection());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_ARC_140")
    @Requirement("REGARDS_DSL_DAM_PLG_210")
    @Purpose("The system has a plugin that enables to define a datasource to a PostreSql database by introspection")
    public void getDataSourceIntrospection() throws SQLException {
        Assert.assertEquals(nbElements, repository.count());

        // Get first page
        Page<DataObject> ll = plgDBDataSource.findAll(TENANT, new PageRequest(0, 2));
        Assert.assertNotNull(ll);
        Assert.assertEquals(2, ll.getContent().size());

        ll.getContent().get(0).getProperties().forEach(attr -> {
            if (attr.getName().equals("name")) {
                Assert.assertTrue(attr.getValue().toString().contains(HELLO));
            }
        });

        ll.getContent().forEach(d -> Assert.assertNotNull(d.getIpId()));
        ll.getContent().forEach(d -> Assert.assertNotNull(d.getSipId()));
        ll.getContent().forEach(d -> Assert.assertTrue(0 < d.getProperties().size()));

        // Get second page
        ll = plgDBDataSource.findAll(TENANT, new PageRequest(1, 2));
        Assert.assertNotNull(ll);
        Assert.assertEquals(1, ll.getContent().size());

        ll.getContent().get(0).getProperties().forEach(attr -> {
            if (attr.getName().equals("name")) {
                Assert.assertTrue(attr.getValue().toString().contains(HELLO + ""));
            }
        });

        ll.getContent().forEach(d -> Assert.assertNotNull(d.getIpId()));
        ll.getContent().forEach(d -> Assert.assertNotNull(d.getSipId()));
        ll.getContent().forEach(d -> Assert.assertTrue(0 < d.getProperties().size()));
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_ARC_140")
    @Purpose("The system allows to define a mapping between the datasource's attributes and an internal model")
    public void getDataSourceIntrospectionFromPastDate() throws SQLException {
        Assert.assertEquals(nbElements, repository.count());

        OffsetDateTime date = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC).minusMinutes(2);
        Page<DataObject> ll = plgDBDataSource.findAll(TENANT, new PageRequest(0, 10), date);
        Assert.assertNotNull(ll);
        Assert.assertEquals(1, ll.getContent().size());

        ll.getContent().get(0).getProperties().forEach(attr -> {
            if (attr.getName().equals("name")) {
                Assert.assertTrue(attr.getValue().toString().contains(HELLO));
            }
        });

        ll.getContent().forEach(d -> Assert.assertNotNull(d.getIpId()));
        ll.getContent().forEach(d -> Assert.assertNotNull(d.getSipId()));
        ll.getContent().forEach(d -> Assert.assertTrue(0 < d.getProperties().size()));
    }

    @Test
    public void getDataSourceIntrospectionFromFutureDate() throws SQLException {
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

        return PluginUtils.getPluginConfiguration(parameters, DefaultPostgreConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private void buildModelAttributes() {
        List<AbstractAttributeMapping> attributes = new ArrayList<AbstractAttributeMapping>();

        attributes.add(new StaticAttributeMapping(AttributeType.LONG, "id", AbstractAttributeMapping.PRIMARY_KEY));
        attributes.add(new StaticAttributeMapping(AttributeType.STRING, "'" + HELLO + "- '||label as label", AbstractAttributeMapping.LABEL));
        attributes.add(new DynamicAttributeMapping("alt", "geometry", AttributeType.INTEGER, "altitude AS altitude"));
        attributes.add(new DynamicAttributeMapping("lat", "geometry", AttributeType.DOUBLE, "latitude"));
        attributes.add(new DynamicAttributeMapping("long", "geometry", AttributeType.DOUBLE, "longitude"));
        attributes.add(new DynamicAttributeMapping("creationDate1", "hello", AttributeType.DATE_ISO8601,
                                                    "timeStampWithoutTimeZone", Types.TIMESTAMP));
        attributes.add(new DynamicAttributeMapping("creationDate2", "hello", AttributeType.DATE_ISO8601,
                                                    "timeStampWithoutTimeZone"));
        attributes.add(new DynamicAttributeMapping("date", "hello", AttributeType.DATE_ISO8601, "date", Types.DATE));
        attributes.add(new StaticAttributeMapping(AttributeType.DATE_ISO8601, "timeStampWithTimeZone", Types.TIMESTAMP,
                                                  AbstractAttributeMapping.LAST_UPDATE));
        attributes.add(new DynamicAttributeMapping("isUpdate", "hello", AttributeType.BOOLEAN, "update"));

        modelMapping = new DataSourceModelMapping(123L, attributes);
    }

}
