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
package fr.cnes.regards.modules.crawler.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceIT;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.crawler.dao.IDatasourceIngestionRepository;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionStatus;
import fr.cnes.regards.modules.crawler.service.ds.ExternalData;
import fr.cnes.regards.modules.crawler.service.ds.ExternalData2;
import fr.cnes.regards.modules.crawler.service.ds.ExternalData2Repository;
import fr.cnes.regards.modules.crawler.service.ds.ExternalData3;
import fr.cnes.regards.modules.crawler.service.ds.ExternalData3Repository;
import fr.cnes.regards.modules.crawler.service.ds.ExternalDataRepository;
import fr.cnes.regards.modules.crawler.test.IngesterConfiguration;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.StaticAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.AipDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.models.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.storage.client.IAipClient;

@ContextConfiguration(classes = { IngesterConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
public class IngesterServiceIT extends AbstractRegardsServiceIT {

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    // private static final String TENANT = "INGEST";
    private static final String TENANT = DEFAULT_TENANT;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    private static final String T_DATA_1 = "projectdb.t_data";

    private static final String T_DATA_2 = "projectdb.t_data_2";

    private static final String T_DATA_3 = "projectdb.t_data_3";

    @Value("${postgresql.datasource.host}")
    private String dbHost;

    @Value("${postgresql.datasource.port}")
    private String dbPort;

    @Value("${postgresql.datasource.name}")
    private String dbName;

    @Value("${postgresql.datasource.username}")
    private String dbUser;

    @Value("${postgresql.datasource.password}")
    private String dbPpassword;

    @Value("${postgresql.datasource.driver}")
    private String driver;

    @Autowired
    private IIngesterService ingesterService;

    private List<AbstractAttributeMapping> modelAttrMapping;

    private Model dataModel;

    private Model datasetModel;

    private PluginConfiguration dataSourcePluginConf1;

    private PluginConfiguration dataSourcePluginConf2;

    private PluginConfiguration dataSourcePluginConf3;

    private PluginConfiguration dataSourcePluginConf4;

    private PluginConfiguration dBConnectionConf;

    @Autowired
    private ExternalDataRepository extData1Repos;

    @Autowired
    private ExternalData2Repository extData2Repos;

    @Autowired
    private ExternalData3Repository extData3Repos;

    @Autowired
    private ICrawlerAndIngesterService crawlerService;

    @Autowired
    private IDatasetCrawlerService datasetCrawlerService;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IModelRepository modelRepository;

    @Autowired
    private IModelAttrAssocRepository modelAttrAssocRepos;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepos;

    @Autowired
    private IDatasetRepository datasetRepos;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepos;

    @Autowired
    private IDatasourceIngestionRepository dsIngestionRepos;

    @Autowired
    private IEsRepository esRepository;

    @Autowired
    private IAipClient aipClient;

    private PluginConfiguration getPostgresDataSource1(final PluginConfiguration pluginConf) {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.TABLE_PARAM, T_DATA_1)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.REFRESH_RATE, 1)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_NAME_PARAM, dataModel.getName())
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_MAPPING_PARAM, modelAttrMapping)
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getPostgresDataSource2(final PluginConfiguration pluginConf) {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.TABLE_PARAM, T_DATA_2)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.REFRESH_RATE, 1)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_NAME_PARAM, dataModel.getName())
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_MAPPING_PARAM, modelAttrMapping)
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getPostgresDataSource3(final PluginConfiguration pluginConf) {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.TABLE_PARAM, T_DATA_3)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.REFRESH_RATE, 10)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_NAME_PARAM, dataModel.getName())
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_MAPPING_PARAM, modelAttrMapping)
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getAipDataSource() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(AipDataSourcePlugin.MODEL_NAME_PARAM, "model_1")
                .addParameter(AipDataSourcePlugin.REFRESH_RATE, 10)
                .addParameter(AipDataSourcePlugin.BINDING_MAP, createBindingMap()).getParameters();

        return PluginUtils.getPluginConfiguration(parameters, AipDataSourcePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getPostgresConnectionConfiguration() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultPostgreConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultPostgreConnectionPlugin.PASSWORD_PARAM, dbPpassword)
                .addParameter(DefaultPostgreConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultPostgreConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultPostgreConnectionPlugin.DB_NAME_PARAM, dbName).getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultPostgreConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private void buildModelAttributes() {
        modelAttrMapping = new ArrayList<>();
        modelAttrMapping.add(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY, AttributeType.LONG, "id"));
        modelAttrMapping.add(new StaticAttributeMapping(AbstractAttributeMapping.LAST_UPDATE, AttributeType.DATE_ISO8601,
                "date"));
    }

    /**
     * No binding with dynamic values, only mandatory ones
     */
    private Map<String, String> createBindingMap() {
        Map<String, String> map = new HashMap<>();
        return map;
    }

    @Before
    public void setUp() throws Exception {

        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        tenantResolver.forceTenant(TENANT);

        if (esRepository.indexExists(TENANT)) {
            esRepository.deleteAll(TENANT);
        } else {
            esRepository.createIndex(TENANT);
        }

        crawlerService.setConsumeOnlyMode(true);
        datasetCrawlerService.setConsumeOnlyMode(true);
        ingesterService.setConsumeOnlyMode(true);

        dsIngestionRepos.deleteAll();
        extData1Repos.deleteAll();
        extData2Repos.deleteAll();
        extData3Repos.deleteAll();

        datasetRepos.deleteAll();
        entityRepos.deleteAll();
        modelAttrAssocRepos.deleteAll();
        modelRepository.deleteAll();

        pluginConfRepos.deleteAll();

        pluginService.addPluginPackage("fr.cnes.regards.modules.datasources.plugins");

        dataModel = new Model();
        dataModel.setName("model_1");
        dataModel.setType(EntityType.DATA);
        dataModel.setVersion("1");
        dataModel.setDescription("Test data object model");
        modelService.createModel(dataModel);

        datasetModel = new Model();
        datasetModel.setName("model_ds_1" + System.currentTimeMillis());
        datasetModel.setType(EntityType.DATASET);
        datasetModel.setVersion("1");
        datasetModel.setDescription("Test dataset model");
        modelService.createModel(datasetModel);

        // Initialize the AbstractAttributeMapping
        buildModelAttributes();

        // Connection PluginConf
        dBConnectionConf = getPostgresConnectionConfiguration();
        pluginService.savePluginConfiguration(dBConnectionConf);

        final DefaultPostgreConnectionPlugin dbCtx = pluginService.getPlugin(dBConnectionConf);
        Assume.assumeTrue(dbCtx.testConnection());

        // DataSource PluginConf
        dataSourcePluginConf1 = getPostgresDataSource1(dBConnectionConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf1);

        dataSourcePluginConf2 = getPostgresDataSource2(dBConnectionConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf2);

        dataSourcePluginConf3 = getPostgresDataSource3(dBConnectionConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf3);

        dataSourcePluginConf4 = getAipDataSource();
        pluginService.savePluginConfiguration(dataSourcePluginConf4);

    }

    @After
    public void clean() {
        if (dataSourcePluginConf1 != null) {
            Utils.execute(pluginService::deletePluginConfiguration, dataSourcePluginConf1.getId());
        }
        if (dataSourcePluginConf2 != null) {
            Utils.execute(pluginService::deletePluginConfiguration, dataSourcePluginConf2.getId());
        }
        if (dataSourcePluginConf3 != null) {
            Utils.execute(pluginService::deletePluginConfiguration, dataSourcePluginConf3.getId());
        }
        if (dBConnectionConf != null) {
            Utils.execute(pluginService::deletePluginConfiguration, dBConnectionConf.getId());
        }
        if (dataSourcePluginConf4 != null) {
            Utils.execute(pluginService::deletePluginConfiguration, dataSourcePluginConf4.getId());
        }

        if (datasetModel != null) {
            Utils.execute(modelService::deleteModel, datasetModel.getName());
        }
        if (dataModel != null) {
            Utils.execute(modelService::deleteModel, dataModel.getName());
        }

    }

    @Test
    public void test() throws InterruptedException {
        Mockito.when(aipClient.retrieveAipDataFiles(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(),
                                                    Mockito.anyInt()))
                .thenReturn(ResponseEntity.ok(new PagedResources<>(Collections.emptyList(),
                        new PagedResources.PageMetadata(0, 0, 0, 1))));
        // Initial Ingestion with no value from datasources
        ingesterService.manage();

        List<DatasourceIngestion> dsIngestions = dsIngestionRepos.findAll();
        Assert.assertTrue(dsIngestions.stream().allMatch(dsIngest -> dsIngest.getStatus() == IngestionStatus.FINISHED));
        Assert.assertTrue(dsIngestions.stream().allMatch(dsIngest -> dsIngest.getSavedObjectsCount() == 0));
        Assert.assertTrue(dsIngestions.stream().allMatch(dsIngest -> dsIngest.getLastIngestDate() != null));

        // Add a ExternalData
        final LocalDate today = LocalDate.now();
        final ExternalData data1_0 = new ExternalData(today);
        extData1Repos.save(data1_0);

        // ExternalData is from a datasource that has a refresh rate of 1 s
        Thread.sleep(1_000);

        ingesterService.manage();
        dsIngestions = dsIngestionRepos.findAll();
        // ExternalData has a Date not a DateTime so its creation date will be available tomorrow, not today
        Assert.assertTrue(dsIngestions.stream().allMatch(dsIngest -> dsIngest.getSavedObjectsCount() == 0));

        final OffsetDateTime now = OffsetDateTime.now();
        final ExternalData2 data2_0 = new ExternalData2(now);
        extData2Repos.save(data2_0);
        final ExternalData3 data3_0 = new ExternalData3(now);
        extData3Repos.save(data3_0);

        // ExternalData2 is from a datasource that has a refresh rate of 1 s
        // ExternalData3 is from a datasource that has a refresh rate of 10 s (so does AipDataSourcePlugin)
        Thread.sleep(1_000);
        ingesterService.manage();
        dsIngestions = dsIngestionRepos.findAll();
        // because of refresh rates, only ExternalData2 datasource must be ingested, we should wait 9 more
        // seconds for ExternalData3 one
        Assert.assertEquals(1, dsIngestions.stream().filter(dsIngest -> dsIngest.getSavedObjectsCount() == 1).count());

        Thread.sleep(9_000);
        ingesterService.manage();
        dsIngestions = dsIngestionRepos.findAll();
        // because of refresh rates, only ExternalData2 datasource must be ingested, we should wait at least 9 more
        // seconds for ExternalData3 one
        Assert.assertEquals(1, dsIngestions.stream().filter(dsIngest -> dsIngest.getSavedObjectsCount() == 1).count());

        Thread.sleep(10_000);
        ingesterService.manage();
        dsIngestions = dsIngestionRepos.findAll();
        Assert.assertTrue(dsIngestions.stream().allMatch(dsIngest -> dsIngest.getStatus() == IngestionStatus.FINISHED));
        Assert.assertTrue(dsIngestions.stream().allMatch(dsIngest -> dsIngest.getSavedObjectsCount() == 0));
        Assert.assertTrue(dsIngestions.stream().allMatch(dsIngest -> dsIngest.getLastIngestDate() != null));
    }
}
