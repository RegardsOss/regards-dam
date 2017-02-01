/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.plugins.datasource;

import java.time.LocalDateTime;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreSQLConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.SqlDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.domain.Column;
import fr.cnes.regards.modules.datasources.plugins.domain.Index;
import fr.cnes.regards.modules.datasources.plugins.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.DataSourceEntity;
import fr.cnes.regards.modules.datasources.utils.DataSourceUtilsException;
import fr.cnes.regards.modules.datasources.utils.IDomainDataSourceRepository;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Christophe Mertz
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PostgreDataSourcePluginTestConfiguration.class })
@ComponentScan(basePackages = { "fr.cnes.regards.modules.datasources.utils" })
public class PostgresDBDataSourcePluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresDBDataSourcePluginTest.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "t_test_plugin_data_source";

    @Value("${postgresql.datasource.url}")
    private String url;

    @Value("${postgresql.datasource.username}")
    private String user;

    @Value("${postgresql.datasource.password}")
    private String password;

    @Value("${postgresql.datasource.driver}")
    private String driver;

    private IDBDataSourcePlugin plgDBDataSource;

    /**
     * JPA Repository
     */
    @Autowired
    IDomainDataSourceRepository repository;

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
         * Instantiate the SQL DataSource plugin
         */
        List<PluginParameter> parameters;
        try {
            parameters = PluginParametersFactory.build()
                    .addParameterPluginConfiguration(SqlDBDataSourcePlugin.CONNECTION_PARAM,
                                                     getPostGreSqlConnectionConfiguration())
                    .getParameters();
        } catch (PluginUtilsException e) {
            throw new DataSourceUtilsException(e.getMessage());
        }

        try {
            plgDBDataSource = PluginUtils.getPlugin(parameters, SqlDBDataSourcePlugin.class,
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
    public void getColumnsIndexes() {
        Assert.assertEquals(3, repository.count());

        Map<String, Table> tables = plgDBDataSource.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());

        Map<String, Column> columns = plgDBDataSource.getColumns(tables.get(TABLE_NAME_TEST));
        Assert.assertNotNull(columns);
        Assert.assertEquals(7, columns.size());

        Map<String, Index> indexs = plgDBDataSource.getIndexes(tables.get(TABLE_NAME_TEST));
        Assert.assertNotNull(indexs);
        Assert.assertEquals(3, indexs.size());
    }

    @After
    public void erase() {
        // repository.deleteAll();
    }

    /**
     * Define the {@link PluginConfiguration} for a {@link DefaultPostgreSQLConnectionPlugin} to connect to the
     * PostgreSql database.
     * 
     * @return the {@link PluginConfiguration}
     * @throws PluginUtilsException
     */
    private PluginConfiguration getPostGreSqlConnectionConfiguration() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreSQLConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultPostgreSQLConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(DefaultPostgreSQLConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultPostgreSQLConnectionPlugin.DRIVER_PARAM, driver)
                .addParameter(DefaultPostgreSQLConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultPostgreSQLConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultPostgreSQLConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

}
