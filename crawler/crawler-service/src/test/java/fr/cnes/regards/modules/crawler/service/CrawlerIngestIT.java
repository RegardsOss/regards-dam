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

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;
import fr.cnes.regards.framework.amqp.configuration.IRabbitVirtualHostAdmin;
import fr.cnes.regards.framework.amqp.configuration.RegardsAmqpAdmin;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.crawler.service.ds.ExternalData;
import fr.cnes.regards.modules.crawler.service.ds.ExternalDataRepository;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.domain.StaticAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.event.DatasetEvent;
import fr.cnes.regards.modules.entities.domain.event.NotDatasetEntityEvent;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.modules.indexer.dao.EsRepository;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.service.IIndexerService;
import fr.cnes.regards.modules.indexer.service.ISearchService;
import fr.cnes.regards.modules.indexer.service.Searches;
import fr.cnes.regards.modules.models.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.plugins.utils.PluginUtils;

/**
 * Crawler ingestion tests
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
@Ignore("Don't reactivate this test, it is nearly impossible de manage a multi-thread tests with all this mess")
public class CrawlerIngestIT {

    private static Logger LOGGER = LoggerFactory.getLogger(CrawlerIngestIT.class);

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "t_data";

    private static final String TENANT = "INGEST";

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
    private IModelService modelService;

    @Autowired
    private IModelRepository modelRepository;

    @Autowired
    private IDatasetService dsService;

    @Autowired
    private IIndexerService indexerService;

    @Autowired
    private ISearchService searchService;

    @Autowired
    private ICrawlerAndIngesterService crawlerService;

    @Autowired
    private IIngesterService ingesterService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepos;

    @Autowired
    private IDatasetRepository datasetRepos;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepos;

    @Autowired
    private IRabbitVirtualHostAdmin rabbitVhostAdmin;

    @Autowired
    private RegardsAmqpAdmin amqpAdmin;

    @Autowired
    private EsRepository esRepos;

    private Model dataModel;

    private Model datasetModel;

    private PluginConfiguration dataSourcePluginConf;

    private Dataset dataset;

    private PluginConfiguration dBConnectionConf;

    @Autowired
    private ExternalDataRepository extDataRepos;

    @Autowired
    private IModelAttrAssocRepository attrAssocRepos;

    @Before
    public void setUp() throws Exception {
        LOGGER.info("********************* setUp CrawlerIngestIT ***********************************");
        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        if (esRepos.indexExists(TENANT)) {
            esRepos.deleteAll(TENANT);
        } else {
            esRepos.createIndex(TENANT);
        }
        tenantResolver.forceTenant(TENANT);

        crawlerService.setConsumeOnlyMode(false);
        ingesterService.setConsumeOnlyMode(true);

        rabbitVhostAdmin.bind(tenantResolver.getTenant());
        amqpAdmin.purgeQueue(DatasetEvent.class, false);
        amqpAdmin.purgeQueue(NotDatasetEntityEvent.class, false);
        rabbitVhostAdmin.unbind();

        attrAssocRepos.deleteAll();
        datasetRepos.deleteAll();
        entityRepos.deleteAll();
        pluginConfRepos.deleteAll();
        modelRepository.deleteAll();
        extDataRepos.deleteAll();

        pluginService.addPluginPackage("fr.cnes.regards.modules.datasources.plugins");

        // Register model attributes
        dataModel = new Model();
        dataModel.setName("model_1" + System.currentTimeMillis());
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
        dataSourcePluginConf = getPostgresDataSource(dBConnectionConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf);
        LOGGER.info("***************************************************************************");
    }

    @After
    public void clean() {
        LOGGER.info("********************* clean CrawlerIngestIT ***********************************");
        attrAssocRepos.deleteAll();
        datasetRepos.deleteAll();
        entityRepos.deleteAll();
        pluginConfRepos.deleteAll();
        modelRepository.deleteAll();
        extDataRepos.deleteAll();
        LOGGER.info("***************************************************************************");
    }

    private PluginConfiguration getPostgresDataSource(final PluginConfiguration pluginConf) {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameterPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.REFRESH_RATE, "1800")
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_PARAM,
                              adapter.toJson(dataSourceModelMapping)).getParameters();

        return PluginUtils.getPluginConfiguration(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getPostgresConnectionConfiguration() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, dbPpassword)
                .addParameter(DefaultOracleConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultOracleConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultOracleConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultOracleConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultOracleConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultPostgreConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private void buildModelAttributes() {
        final List<AbstractAttributeMapping> attributes = new ArrayList<AbstractAttributeMapping>();

        attributes.add(new StaticAttributeMapping(AbstractAttributeMapping.PRIMARY_KEY, "id"));

        attributes.add(new StaticAttributeMapping(AbstractAttributeMapping.LAST_UPDATE, AttributeType.DATE_ISO8601,
                                                  "date"));

        dataSourceModelMapping = new DataSourceModelMapping(dataModel.getId(), attributes);
    }

    @Test
    public void test()
            throws ModuleException, IOException, InterruptedException, ExecutionException, DataSourceException {
        LOGGER.info("********************* test CrawlerIngestIT ***********************************");
        final String tenant = tenantResolver.getTenant();
        // First delete index if it already exists
        //        indexerService.deleteIndex(tenant);

        // Fill the DB with an object from 2000/01/01
        extDataRepos.saveAndFlush(new ExternalData(LocalDate.of(2000, Month.JANUARY, 1)));

        // Ingest from scratch
        IngestionResult summary = crawlerService.ingest(dataSourcePluginConf);
        Assert.assertEquals(1, summary.getSavedObjectsCount());

        crawlerService.startWork();
        // Dataset on all objects
        dataset = new Dataset(datasetModel, tenant, "dataset label 1");
        dataset.setDataModel(dataModel.getId());
        dataset.setSubsettingClause(ICriterion.all());
        dataset.setLicence("licence");
        dataset.setDataSource(dataSourcePluginConf);
        dataset.setTags(Sets.newHashSet("BULLSHIT"));
        dataset.setGroups(Sets.newHashSet("group0", "group11"));
        LOGGER.info("Creating dataset....");
        dsService.create(dataset);
        LOGGER.info("Dataset created in DB....");

        LOGGER.info("Waiting for end of crawler work");
        crawlerService.waitForEndOfWork();
        LOGGER.info("Sleeping 10 s....");
        Thread.sleep(10_000);
        LOGGER.info("...Waking");

        // Retrieve dataset1 from ES
        final UniformResourceName ipId = dataset.getIpId();
        dataset = searchService.get(ipId);
        if (dataset == null) {
            Thread.sleep(10_000L);
            esRepos.refresh(tenant);
            dataset = searchService.get(ipId);
        }

        final SimpleSearchKey<DataObject> objectSearchKey = Searches.onSingleEntity(tenant, EntityType.DATA);
        // Search for DataObjects tagging dataset1
        LOGGER.info("searchService : " + searchService);
        LOGGER.info("dataset : " + dataset);
        LOGGER.info("dataset.getIpId() : " + dataset.getIpId());
        Page<DataObject> objectsPage = searchService
                .search(objectSearchKey, IEsRepository.BULK_SIZE, ICriterion.eq("tags", dataset.getIpId().toString()));
        Assert.assertEquals(1L, objectsPage.getTotalElements());

        // Fill the Db with an object dated 2001/01/01
        extDataRepos.save(new ExternalData(LocalDate.of(2001, Month.JANUARY, 1)));

        // Ingest from 2000/01/01 (strictly after)
        summary = crawlerService
                .ingest(dataSourcePluginConf, OffsetDateTime.of(2000, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC));
        Assert.assertEquals(1, summary.getSavedObjectsCount());

        // Search for DataObjects tagging dataset1
        objectsPage = searchService
                .search(objectSearchKey, IEsRepository.BULK_SIZE, ICriterion.eq("tags", dataset.getIpId().toString()));
        Assert.assertEquals(2L, objectsPage.getTotalElements());
        Assert.assertEquals(1, objectsPage.getContent().stream()
                .filter(data -> data.getLastUpdate().equals(data.getCreationDate())).count());
        Assert.assertEquals(1, objectsPage.getContent().stream()
                .filter(data -> data.getLastUpdate().isAfter(data.getCreationDate())).count());
        LOGGER.info("***************************************************************************");
    }
}
