/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import fr.cnes.regards.modules.datasources.plugins.domain.AttributeMappingAdapter;
import fr.cnes.regards.modules.datasources.plugins.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.plugintypes.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.DataSourceEntity;
import fr.cnes.regards.modules.datasources.utils.DataSourceUtilsException;
import fr.cnes.regards.modules.datasources.utils.IDomainDataSourceRepository;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
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

    /**
     * JPA Repository
     */
    @Autowired
    IDomainDataSourceRepository repository;

    @Value("${datasource.url}")
    private String url;

    @Value("${datasource.username}")
    private String user;

    @Value("${datasource.password}")
    private String password;

    @Value("${datasource.driver}")
    private String driver;

    private List<DataSourceAttributeMapping> attributes = new ArrayList<DataSourceAttributeMapping>();

    private IDataSourcePlugin plgDataSource;

    private final AttributeMappingAdapter adapter = new AttributeMappingAdapter();

    /**
     * Populate the datasource as a legacy catalog
     * 
     * @throws DataSourceUtilsException
     * 
     * @throws JwtException
     * @throws PluginUtilsException
     */
    @Before
    public void setUp() throws DataSourceUtilsException {
        /*
         * Add data to the data source
         */
        repository.deleteAll();
        repository.save(new DataSourceEntity("azertyuiop", 12345, 1.10203045607080901234568790123456789, 45.5444544454,
                LocalDateTime.now(), true));
        repository.save(new DataSourceEntity("Toulouse", 110, 3.141592653589793238462643383279, -15.2323654654564654,
                LocalDateTime.now().minusDays(5), false));
        repository.save(new DataSourceEntity("Paris", 110, -3.141592653589793238462643383279502884197169399375105,
                25.565465465454564654654654, LocalDateTime.now().plusHours(10), false));

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
                    .addParameterPluginConfiguration(PostgreDataSourcePlugin.CONNECTION,
                                                     getPostGreSqlConnectionConfiguration())
                    .addParameter(PostgreDataSourcePlugin.MODEL, adapter.toJson(attributes))
                    .addParameter(PostgreDataSourcePlugin.REQUEST, "select * from T_TEST_PLUGIN_DATA_SOURCE")
                    .getParameters();
        } catch (PluginUtilsException e) {
            throw new DataSourceUtilsException(e.getMessage());
        }

        try {
            plgDataSource = PluginUtils.getPlugin(parameters, PostgreDataSourcePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        } catch (PluginUtilsException e) {
            throw new DataSourceUtilsException(e.getMessage());
        }

    }

    @Test
    public void firstTest() {
        Assert.assertEquals(3, repository.count());

        Page<AbstractEntity> ll = plgDataSource.findAll(new PageRequest(0, 10));

        Assert.assertNotNull(ll);
    }

    @After
    public void erase() {
        // repository.deleteAll();
    }

    /**
     * Define the {@link PluginConfiguration} for a {@link DefaultPostgreSQLConnectionPlugin} to connect to the
     * PostgreSql database
     * 
     * @return the {@link PluginConfiguration}
     * @throws PluginUtilsException
     */
    private PluginConfiguration getPostGreSqlConnectionConfiguration() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER, user)
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD, password)
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL, url)
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER, driver)
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE, "3")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE, "1")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_STATEMENTS, "150").getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultPostgreSQLConnectionPlugin.class,
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
