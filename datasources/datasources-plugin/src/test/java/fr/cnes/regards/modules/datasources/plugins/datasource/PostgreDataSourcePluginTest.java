/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.datasource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hsqldb.Types;
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
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
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
public class PostgreDataSourcePluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourcePluginTest.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TENANT = "PG_TENANT";

    private static final String HELLO = "Hello ";

    /**
     * JPA Repository
     */
    @Autowired
    IDataSourceRepositoryTest repository;

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

    private IDataSourcePlugin plgDataSource;

    private DataSourceModelMapping modelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    private static int nbElements;

    /**
     * Populate the datasource as a legacy catalog
     *
     * @throws DataSourcesPluginException
     *
     * @throws JwtException
     * @throws PluginUtilsException
     */
    @Before
    public void setUp() throws DataSourcesPluginException {
        /*
         * Add data to the data source
         */
        repository.deleteAll();
        repository.save(new DataSourceEntity("azertyuiop", 12345, 1.10203045607080901234568790123456789, 45.5444544454,
                LocalDate.now().minusDays(10), LocalTime.now().minusHours(9), LocalDateTime.now(),
                OffsetDateTime.now().minusMinutes(33), true));
        repository.save(new DataSourceEntity("Toulouse", 110, 3.141592653589793238462643383279, -15.2323654654564654,
                LocalDate.now().minusMonths(1), LocalTime.now().minusMinutes(35), LocalDateTime.now().plusHours(33),
                OffsetDateTime.now().minusSeconds(22), true));
        repository.save(new DataSourceEntity("Paris", 350, -3.141592653589793238462643383279502884197169399375105,
                25.565465465454564654654654, LocalDate.now().minusDays(10), LocalTime.now().minusHours(9),
                LocalDateTime.now().minusMonths(2), OffsetDateTime.now().minusHours(7), false));
        nbElements = 3;

        /*
         * Initialize the DataSourceAttributeMapping
         */
        this.buildModelAttributes();

        /*
         * Instantiate the data source plugin
         */
        List<PluginParameter> parameters;
        try {
            parameters = PluginParametersFactory.build()
                    .addParameterPluginConfiguration(PostgreDataSourcePlugin.CONNECTION_PARAM,
                                                     getPostGreSqlConnectionConfiguration())
                    .addParameter(PostgreDataSourcePlugin.MODEL_PARAM, adapter.toJson(modelMapping))
                    .addParameter(PostgreDataSourcePlugin.FROM_CLAUSE, "from T_TEST_PLUGIN_DATA_SOURCE")
                    .getParameters();
        } catch (PluginUtilsException e) {
            throw new DataSourcesPluginException(e.getMessage());
        }

        try {
            plgDataSource = PluginUtils.getPlugin(parameters, PostgreDataSourcePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        } catch (PluginUtilsException e) {
            throw new DataSourcesPluginException(e.getMessage());
        }

    }

    @Test
    @Requirement("REGARDS_DSL_DAM_PLG_200")
    @Purpose("The system has a plugin that enables to define a datasource to a PostreSql database by setting a SQL request")
    public void getDataSourceIntrospection() {
        Assert.assertEquals(nbElements, repository.count());

        Page<DataObject> ll = plgDataSource.findAll(TENANT, new PageRequest(0, 10));
        Assert.assertNotNull(ll);
        Assert.assertEquals(nbElements, ll.getContent().size());

        ll.getContent().get(0).getAttributes().forEach(attr -> {
            if (attr.getName().equals("name")) {
                Assert.assertTrue(attr.getValue().toString().contains(HELLO));
            }
        });
    }

    @After
    public void erase() {
        // repository.deleteAll();
    }

    /**
     * Define the {@link PluginConfiguration} for a {@link DefaultPostgreConnectionPlugin} to connect to the PostgreSql
     * database
     *
     * @return the {@link PluginConfiguration}
     * @throws PluginUtilsException
     */
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
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private void buildModelAttributes() {
        List<DataSourceAttributeMapping> attributes = new ArrayList<DataSourceAttributeMapping>();

        attributes.add(new DataSourceAttributeMapping("id", AttributeType.LONG, "id", true));
        attributes
                .add(new DataSourceAttributeMapping("name", AttributeType.STRING, "'" + HELLO + "- '||label as label"));
        attributes
                .add(new DataSourceAttributeMapping("alt", "geometry", AttributeType.INTEGER, "altitude AS altitude"));
        attributes.add(new DataSourceAttributeMapping("lat", "geometry", AttributeType.DOUBLE, "latitude"));
        attributes.add(new DataSourceAttributeMapping("long", "geometry", AttributeType.DOUBLE, "longitude"));
        attributes.add(new DataSourceAttributeMapping("creationDate1", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithoutTimeZone", Types.TIMESTAMP));
        attributes.add(new DataSourceAttributeMapping("creationDate2", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithoutTimeZone"));
        attributes.add(new DataSourceAttributeMapping("date", "hello", AttributeType.DATE_ISO8601, "date", Types.DATE));
        attributes.add(new DataSourceAttributeMapping("timeStampWithTimeZone", "hello", AttributeType.DATE_ISO8601,
                "timeStampWithTimeZone", Types.TIMESTAMP));
        attributes.add(new DataSourceAttributeMapping("isUpdate", "hello", AttributeType.BOOLEAN, "update"));

        modelMapping = new DataSourceModelMapping(123L, attributes);
    }

}