/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.datasources.plugins.connection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.utils.PostgreDataSourcePluginTestConfiguration;

/**
 * @author Christophe Mertz
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PostgreDataSourcePluginTestConfiguration.class })
@TestPropertySource("classpath:datasource-test.properties")
public class PostgreConnectionPluginIntrospectionTest extends AbstractRegardsServiceIT {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreConnectionPluginIntrospectionTest.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

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

    private DefaultPostgreConnectionPlugin postgreDBConn;

    @Before
    public void setUp() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName).getParameters();

        postgreDBConn = PluginUtils.getPlugin(parameters, DefaultPostgreConnectionPlugin.class,
                                              Arrays.asList(PLUGIN_PACKAGE), new HashMap<>());

        // Do not launch tests is Database is not available
        Assume.assumeTrue(postgreDBConn.testConnection());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_PLG_100")
    @Purpose("The system has a plugin that enables to connect to a PostreSql database")
    public void postgreSqlConnection() {
        Assert.assertTrue(postgreDBConn.testConnection());

        Map<String, Table> tables = postgreDBConn.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_155")
    @Purpose("The system has a plugin that enables for a SGBD to get the list of tables and for a table, the list of columns and their types")
    public void getTablesAndColumns() {
        Assert.assertTrue(postgreDBConn.testConnection());

        Map<String, Table> tables = postgreDBConn.getTables();
        Assert.assertNotNull(tables);
        Assert.assertTrue(!tables.isEmpty());

        tables.forEach((k, t) -> {
            Assert.assertNotNull(t.getName());
            LOG.info("table={}-{}-{}-{}-{}-{}", t.toString(), t.getPKey(), t.getName(), t.getTableDefinition(),
                     t.getCatalog(), t.getSchema());

        });

        Map<String, Column> columns = postgreDBConn.getColumns(TABLE_NAME_TEST);
        Assert.assertNotNull(columns);

        columns.forEach((k, c) -> {
            Assert.assertNotNull(c.getName());
            LOG.info("column={}-{}", c.getName(), c.getJavaSqlType());
        });
    }

}
