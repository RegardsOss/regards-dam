/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.crawler.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.dam.domain.datasources.AbstractAttributeMapping;
import fr.cnes.regards.modules.dam.domain.datasources.DynamicAttributeMapping;
import fr.cnes.regards.modules.dam.domain.datasources.StaticAttributeMapping;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DataSourceException;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.attribute.DateAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.IntegerAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.StringAttribute;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.gson.entities.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.indexer.service.IIndexerService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
@Ignore
public class CrawlerServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceTest.class);

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "T_DATA_OBJECTS";

    private static final String TENANT = "default";

    @Autowired
    private IIndexerService indexerService;

    private IDBDataSourceFromSingleTablePlugin dsPlugin;

    private List<AbstractAttributeMapping> modelAttrMapping;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Before
    public void tearUp() throws SQLException {
        // This Tenant (default) isn't in "regards.tenants" so crawlerService will never poll associated events
        tenantResolver.forceTenant(TENANT);
        /*
         * Initialize the AbstractAttributeMapping
         */
        buildModelAttributes();

        /*
         * Instantiate the SQL DataSource plugin
         */
        List<PluginParameter> parameters;
        // parameters = PluginParametersFactory.build()
        // .addParameterPluginConfiguration(OracleDataSourceFromSingleTablePlugin.CONNECTION_PARAM,
        // getOracleConnectionConfiguration())
        // .addParameter(OracleDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST)
        // .addParameter(OracleDataSourceFromSingleTablePlugin.MODEL_PARAM, dataSourceModelMapping)
        // .addParameter(OracleDataSourceFromSingleTablePlugin.REFRESH_RATE, "1800").getParameters();
        // dsPlugin = PluginUtils.getPlugin(parameters, OracleDataSourceFromSingleTablePlugin.class,
        // Arrays.asList(PLUGIN_CURRENT_PACKAGE), new HashMap<>());
        // TODO use a Postgres plugin

        // Do not launch tests is Database is not available
        Assume.assumeTrue(dsPlugin.getDBConnection().testConnection());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSuckUp() throws DataSourceException {
        // register JSon data types (for ES)
        registerJSonModelAttributes();

        // Creating index if it doesn't already exist
        indexerService.deleteIndex(TENANT);
        indexerService.createIndex(TENANT);

        // Retrieve first 1000 objects

        Page<DataObject> page = dsPlugin.findAll(TENANT, new PageRequest(0, 1000));

        LOGGER.info("saving {}/{} entities...", page.getNumberOfElements(), page.getTotalElements());
        Set<DataObject> set = Sets.newHashSet(page.getContent());
        Assert.assertEquals(page.getContent().size(), set.size());
        int savedItemsCount = indexerService.saveBulkEntities(TENANT, page.getContent());
        LOGGER.info("...{} entities saved", savedItemsCount);
        while (page.hasNext()) {
            page = dsPlugin.findAll(TENANT, page.nextPageable());
            set = Sets.newHashSet(page.getContent());
            Assert.assertEquals(page.getContent().size(), set.size());
            LOGGER.info("saving {}/{} entities...", page.getNumberOfElements(), page.getTotalElements());
            savedItemsCount = indexerService.saveBulkEntities(TENANT, page.getContent());
            LOGGER.info("...{} entities saved", savedItemsCount);
        }
        Assert.assertTrue(true);
    }

    // private PluginConfiguration getOracleConnectionConfiguration() {
    // final List<PluginParameter> parameters = PluginParametersFactory.build()
    // .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, dbUser)
    // .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, dbPpassword)
    // .addParameter(DefaultOracleConnectionPlugin.DB_HOST_PARAM, dbHost)
    // .addParameter(DefaultOracleConnectionPlugin.DB_PORT_PARAM, dbPort)
    // .addParameter(DefaultOracleConnectionPlugin.DB_NAME_PARAM, dbName)
    // .addParameter(DefaultOracleConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
    // .addParameter(DefaultOracleConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();
    //
    // return PluginUtils.getPluginConfiguration(parameters, DefaultOracleConnectionPlugin.class,
    // Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    // }

    private void buildModelAttributes() {
        modelAttrMapping = new ArrayList<>();

        modelAttrMapping.add(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY, AttributeType.INTEGER,
                "DATA_OBJECTS_ID"));

        modelAttrMapping.add(new DynamicAttributeMapping("FILE_SIZE", AttributeType.INTEGER, "FILE_SIZE"));
        modelAttrMapping.add(new DynamicAttributeMapping("FILE_TYPE", AttributeType.STRING, "FILE_TYPE"));
        modelAttrMapping
                .add(new DynamicAttributeMapping("FILE_NAME_ORIGINE", AttributeType.STRING, "FILE_NAME_ORIGINE"));

        modelAttrMapping.add(new DynamicAttributeMapping("DATA_SET_ID", AttributeType.INTEGER, "DATA_SET_ID"));
        modelAttrMapping.add(new DynamicAttributeMapping("DATA_TITLE", AttributeType.STRING, "DATA_TITLE"));
        modelAttrMapping.add(new DynamicAttributeMapping("DATA_AUTHOR", AttributeType.STRING, "DATA_AUTHOR"));
        modelAttrMapping
                .add(new DynamicAttributeMapping("DATA_AUTHOR_COMPANY", AttributeType.STRING, "DATA_AUTHOR_COMPANY"));

        modelAttrMapping.add(new DynamicAttributeMapping("START_DATE", AttributeType.DATE_ISO8601, "START_DATE"));
        modelAttrMapping.add(new DynamicAttributeMapping("STOP_DATE", AttributeType.DATE_ISO8601, "STOP_DATE"));
        modelAttrMapping.add(new DynamicAttributeMapping("DATA_CREATION_DATE", AttributeType.DATE_ISO8601,
                "DATA_CREATION_DATE"));

        modelAttrMapping.add(new DynamicAttributeMapping("MIN_LONGITUDE", AttributeType.INTEGER, "MIN_LONGITUDE"));
        modelAttrMapping.add(new DynamicAttributeMapping("MAX_LONGITUDE", AttributeType.INTEGER, "MAX_LONGITUDE"));
        modelAttrMapping.add(new DynamicAttributeMapping("MIN_LATITUDE", AttributeType.INTEGER, "MIN_LATITUDE"));
        modelAttrMapping.add(new DynamicAttributeMapping("MAX_LATITUDE", AttributeType.INTEGER, "MAX_LATITUDE"));
        modelAttrMapping.add(new DynamicAttributeMapping("MIN_ALTITUDE", AttributeType.INTEGER, "MIN_ALTITUDE"));
        modelAttrMapping.add(new DynamicAttributeMapping("MAX_ALTITUDE", AttributeType.INTEGER, "MAX_ALTITUDE"));
    }

    private void registerJSonModelAttributes() {
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "DATA_OBJECTS_ID");
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "FILE_SIZE");
        gsonAttributeFactory.registerSubtype(TENANT, StringAttribute.class, "FILE_TYPE");
        gsonAttributeFactory.registerSubtype(TENANT, StringAttribute.class, "FILE_NAME_ORIGINE");
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "DATA_SET_ID");
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "DATA_TITLE");
        gsonAttributeFactory.registerSubtype(TENANT, StringAttribute.class, "DATA_AUTHOR");
        gsonAttributeFactory.registerSubtype(TENANT, StringAttribute.class, "DATA_AUTHOR_COMPANY");
        gsonAttributeFactory.registerSubtype(TENANT, DateAttribute.class, "START_DATE");
        gsonAttributeFactory.registerSubtype(TENANT, DateAttribute.class, "STOP_DATE");
        gsonAttributeFactory.registerSubtype(TENANT, DateAttribute.class, "DATA_CREATION_DATE");

        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "MIN_LONGITUDE");
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "MAX_LONGITUDE");
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "MIN_LATITUDE");
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "MAX_LATITUDE");
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "MIN_ALTITUDE");
        gsonAttributeFactory.registerSubtype(TENANT, IntegerAttribute.class, "MAX_ALTITUDE");
    }
}
