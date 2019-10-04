/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.max.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.min.ParsedMin;
import org.elasticsearch.search.aggregations.metrics.sum.ParsedSum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.framework.utils.plugins.PluginParameterTransformer;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.crawler.dao.IDatasourceIngestionRepository;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.crawler.plugins.TestDataSourcePlugin;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.dao.models.IAttributeModelRepository;
import fr.cnes.regards.modules.dam.dao.models.IFragmentRepository;
import fr.cnes.regards.modules.dam.dao.models.IModelAttrAssocRepository;
import fr.cnes.regards.modules.dam.dao.models.IModelRepository;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DataSourcePluginConstants;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.entities.event.DatasetEvent;
import fr.cnes.regards.modules.dam.domain.entities.event.NotDatasetEntityEvent;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.gson.entities.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.dam.gson.entities.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.builder.QueryBuilderCriterionVisitor;
import fr.cnes.regards.modules.indexer.dao.spatial.ProjectGeoSettings;
import fr.cnes.regards.modules.indexer.domain.JoinEntitySearchKey;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.indexer.service.ISearchService;
import fr.cnes.regards.modules.indexer.service.Searches;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@ActiveProfiles("noschedule") // Disable scheduling, this will activate IngesterService during all tests
@TestPropertySource(locations = { "classpath:test.properties" })
@DirtiesContext(hierarchyMode = HierarchyMode.EXHAUSTIVE, classMode = ClassMode.BEFORE_CLASS)
public class IndexerServiceDataSourceIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(IndexerServiceDataSourceIT.class);

    @SuppressWarnings("unused")
    private static final String TABLE_NAME_TEST = "t_validation_1";

    private static final String DATA_MODEL_FILE_NAME = "validationDataModel1.xml";

    private static final String DATASET_MODEL_FILE_NAME = "validationDatasetModel1.xml";

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    @Value("${regards.tenant}")
    private String tenant;

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

    @Value("${postgresql.datasource.schema}")
    private String dbSchema;

    @Value("${regards.elasticsearch.host:}")
    private String esHost;

    @Value("${regards.elasticsearch.address:}")
    private String esAddress;

    @Value("${regards.elasticsearch.http.port}")
    private int esPort;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IModelRepository modelRepository;

    @Autowired
    private IAttributeModelRepository attrModelRepo;

    @Autowired
    private IModelAttrAssocRepository modelAttrAssocRepo;

    @Autowired
    private IFragmentRepository fragRepo;

    @Autowired
    private IDatasetService dsService;

    @Autowired
    private IAttributeModelService attributeModelService;

    @Autowired
    private ISearchService searchService;

    @Autowired
    private IIngesterService ingesterService;

    @Autowired
    private ICrawlerAndIngesterService crawlerService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity<?>> entityRepos;

    @Autowired
    private IDatasetRepository datasetRepos;

    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepo;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private IDatasourceIngestionRepository dsIngestionRepos;

    private Model dataModel;

    private Model datasetModel;

    private PluginConfiguration dataSourcePluginConf;

    private Dataset dataset1;

    private Dataset dataset2;

    private Dataset dataset3;

    @Autowired
    private ProjectGeoSettings settings;

    @Before
    public void setUp() throws Exception {
        Mockito.when(settings.getCrs()).thenReturn(Crs.WGS_84);

        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        runtimeTenantResolver.forceTenant(tenant);
        if (esRepos.indexExists(tenant)) {
            esRepos.deleteIndex(tenant);
        }
        esRepos.createIndex(tenant);

        crawlerService.setConsumeOnlyMode(false);
        ingesterService.setConsumeOnlyMode(true);

        publisher.purgeQueue(DatasetEvent.class);
        publisher.purgeQueue(NotDatasetEntityEvent.class);

        datasetRepos.deleteAll();
        entityRepos.deleteAll();
        modelAttrAssocRepo.deleteAll();
        pluginConfRepo.deleteAll();
        attrModelRepo.deleteAll();
        modelRepository.deleteAll();
        fragRepo.deleteAll();

        // get a model for DataObject, by importing them it also register them for (de)serialization
        importModel(DATA_MODEL_FILE_NAME);
        dataModel = modelService.getModelByName("VALIDATION_DATA_MODEL_1");

        // get a model for Dataset
        importModel(DATASET_MODEL_FILE_NAME);
        datasetModel = modelService.getModelByName("VALIDATION_DATASET_MODEL_1");

        // DataSource PluginConf
        dataSourcePluginConf = getPostgresDataSource();
        pluginService.savePluginConfiguration(dataSourcePluginConf);
    }

    @After
    public void clean() {
        entityRepos.deleteAll();
        modelAttrAssocRepo.deleteAll();
        pluginConfRepo.deleteAll();
        attrModelRepo.deleteAll();
        modelRepository.deleteAll();
        fragRepo.deleteAll();
    }

    private PluginConfiguration getPostgresDataSource() {
        Set<IPluginParam> param = IPluginParam
                .set(IPluginParam.build(TestDataSourcePlugin.MODEL, PluginParameterTransformer.toJson(dataModel)),
                     IPluginParam.build(DataSourcePluginConstants.MODEL_NAME_PARAM, dataModel.getName()));
        return PluginUtils.getPluginConfiguration(param, TestDataSourcePlugin.class);
    }

    @Requirement("REGARDS_DSL_DAM_COL_420")
    @Purpose("Requirement is for collection. Multi search field is used here on data objects but the code is the same")
    @Test
    public void test() throws Exception {
        String tenant = runtimeTenantResolver.getTenant();

        // Creation
        long start = System.currentTimeMillis();
        DatasourceIngestion dsi = new DatasourceIngestion(dataSourcePluginConf.getBusinessId());
        dsi.setLabel("Label");
        dsIngestionRepos.save(dsi);

        IngestionResult summary1 = crawlerService.ingest(dataSourcePluginConf, dsi);
        System.out.println("Insertion : " + (System.currentTimeMillis() - start) + " ms");

        // Update
        start = System.currentTimeMillis();
        IngestionResult summary2 = crawlerService.ingest(dataSourcePluginConf, dsi);
        System.out.println("Update : " + (System.currentTimeMillis() - start) + " ms");
        Assert.assertEquals(summary1.getSavedObjectsCount(), summary2.getSavedObjectsCount());

        crawlerService.startWork();
        // Create 3 Datasets on all objects
        dataset1 = new Dataset(datasetModel, tenant, "DS1", "dataset label 1");
        dataset1.setDataModel(dataModel.getName());
        dataset1.setSubsettingClause(ICriterion.all());
        dataset1.setOpenSearchSubsettingClause("");
        dataset1.setLicence("licence");
        dataset1.setDataSource(dataSourcePluginConf);
        dataset1.setTags(Sets.newHashSet("BULLSHIT"));
        dataset1.setGroups(Sets.newHashSet("group0", "group11"));
        dsService.create(dataset1);

        dataset2 = new Dataset(datasetModel, tenant, "DS2", "dataset label 2");
        dataset2.setDataModel(dataModel.getName());
        dataset2.setSubsettingClause(ICriterion.all());
        dataset2.setOpenSearchSubsettingClause("");
        dataset2.setTags(Sets.newHashSet("BULLSHIT"));
        dataset2.setLicence("licence");
        dataset2.setDataSource(dataSourcePluginConf);
        dataset2.setGroups(Sets.newHashSet("group12", "group11"));
        dsService.create(dataset2);

        dataset3 = new Dataset(datasetModel, tenant, "DS3", "dataset label 3");
        dataset3.setDataModel(dataModel.getName());
        dataset3.setSubsettingClause(ICriterion.all());
        dataset3.setOpenSearchSubsettingClause("");
        dataset3.setLicence("licence");
        dataset3.setDataSource(dataSourcePluginConf);
        dataset3.setGroups(Sets.newHashSet("group2"));
        dsService.create(dataset3);

        crawlerService.waitForEndOfWork();
        Thread.sleep(20_000);
        // indexerService.refresh(tenant);

        // Retrieve dataset1 from ES
        dataset1 = searchService.get(dataset1.getIpId());
        Assert.assertNotNull(dataset1);

        // SearchKey<DataObject> objectSearchKey = new SearchKey<>(tenant, EntityType.DATA.toString(),
        // DataObject.class);
        SimpleSearchKey<DataObject> objectSearchKey = Searches.onSingleEntity(EntityType.DATA);
        objectSearchKey.setSearchIndex(tenant);
        // check that computed attribute were correclty done
        checkDatasetComputedAttribute(dataset1, objectSearchKey, summary1.getSavedObjectsCount());
        // Search for DataObjects tagging dataset1
        Page<DataObject> objectsPage = searchService.search(objectSearchKey, IEsRepository.BULK_SIZE,
                                                            ICriterion.eq("tags", dataset1.getIpId().toString()));
        Assert.assertTrue(objectsPage.getContent().size() > 0);
        Assert.assertEquals(summary1.getSavedObjectsCount(), objectsPage.getContent().size());

        crawlerService.startWork();
        // Delete dataset1
        dsService.delete(dataset1.getId());

        // Wait a while to permit RabbitMq sending a message to crawler service which update ES
        crawlerService.waitForEndOfWork();

        // Search again for DataObjects tagging this dataset
        objectsPage = searchService.search(objectSearchKey, IEsRepository.BULK_SIZE,
                                           ICriterion.eq("tags", dataset1.getIpId().toString()));
        Assert.assertTrue(objectsPage.getContent().isEmpty());
        // Adding some free tag
        objectsPage.getContent().forEach(object -> object.addTags("TOTO"));
        esRepos.saveBulk(tenant, objectsPage.getContent());

        esRepos.refresh(tenant);

        // Search for DataObjects tagging dataset2
        objectsPage = searchService.search(objectSearchKey, IEsRepository.BULK_SIZE,
                                           ICriterion.eq("tags", dataset2.getIpId().toString()));
        Assert.assertTrue(objectsPage.getContent().size() > 0);
        Assert.assertEquals(summary1.getSavedObjectsCount(), objectsPage.getContent().size());

        // Search for Dataset but with criterion on DataObjects
        // SearchKey<Dataset> dsSearchKey = new SearchKey<>(tenant, EntityType.DATA.toString(), Dataset.class);
        JoinEntitySearchKey<DataObject, Dataset> dsSearchKey = Searches
                .onSingleEntityReturningJoinEntity(EntityType.DATA, EntityType.DATASET);
        dsSearchKey.setSearchIndex(tenant);
        // Page<Dataset> dsPage = searchService.searchAndReturnJoinedEntities(dsSearchKey, 1, ICriterion.all());
        Page<Dataset> dsPage = searchService.search(dsSearchKey, 1, ICriterion.all());
        Assert.assertNotNull(dsPage);
        Assert.assertFalse(dsPage.getContent().isEmpty());
        Assert.assertEquals(1, dsPage.getContent().size());

        dsPage = searchService.search(dsSearchKey, dsPage.nextPageable(), ICriterion.all());
        Assert.assertNotNull(dsPage);
        Assert.assertFalse(dsPage.getContent().isEmpty());
        Assert.assertEquals(1, dsPage.getContent().size());

        // Search for Dataset but with criterion on everything
        // SearchKey<Dataset> dsSearchKey2 = new SearchKey<>(tenant, EntityType.DATA.toString(), Dataset.class);
        @SuppressWarnings("rawtypes")
        final JoinEntitySearchKey<AbstractEntity, Dataset> dsSearchKey2 = Searches
                .onAllEntitiesReturningJoinEntity(EntityType.DATASET);
        dsSearchKey2.setSearchIndex(tenant);
        dsPage = searchService.search(dsSearchKey, 1, ICriterion.all());
        Assert.assertNotNull(dsPage);
        Assert.assertFalse(dsPage.getContent().isEmpty());
        Assert.assertEquals(1, dsPage.getContent().size());

        dsPage = searchService.search(dsSearchKey2, dsPage.nextPageable(), ICriterion.all());
        Assert.assertNotNull(dsPage);
        Assert.assertFalse(dsPage.getContent().isEmpty());
        Assert.assertEquals(1, dsPage.getContent().size());
    }

    /**
     * Add document type (Elasticsearch prior to version 6 type) into criterion
     * (Copied from EsRepository)
     */
    private static ICriterion addTypes(ICriterion criterion, String... types) {
        // Beware if crit is null
        criterion = criterion == null ? ICriterion.all() : criterion;
        // Then add type
        switch (types.length) {
            case 0:
                return criterion;
            case 1:
                return ICriterion.and(ICriterion.eq("type", types[0]), criterion);
            default:
                ICriterion orCrit = ICriterion.or(Arrays.stream(types).map(type -> ICriterion.eq("type", type))
                        .toArray(n -> new ICriterion[n]));
                return ICriterion.and(orCrit, criterion);
        }

    }

    private void checkDatasetComputedAttribute(Dataset dataset, SimpleSearchKey<DataObject> objectSearchKey,
            long objectsCreationCount) throws IOException {
        RestClientBuilder restClientBuilder;
        RestHighLevelClient client;
        try {
            restClientBuilder = RestClient.builder(new HttpHost(
                    InetAddress.getByName(!Strings.isNullOrEmpty(esHost) ? esHost : esAddress), esPort));
            client = new RestHighLevelClient(restClientBuilder);

        } catch (final UnknownHostException e) {
            LOGGER.error("could not get a connection to ES in the middle of the test where we know ES is available", e);
            Assert.fail();
            return;
        }
        // lets build the request so elasticsearch can calculate the few attribute we are using in test(min(START_DATE),
        // max(STOP_DATE), sum(FILE_SIZE) via aggregation, the count of element in this context is already known:
        // objectsCreationCount
        QueryBuilderCriterionVisitor critVisitor = new QueryBuilderCriterionVisitor();
        ICriterion crit = ICriterion.eq("tags", dataset.getIpId().toString());
        crit = addTypes(crit, objectSearchKey.getSearchTypes());
        QueryBuilder qb = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                .filter(crit.accept(critVisitor));
        // now we have a request on the right data
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(qb).size(0);
        // lets build the aggregations
        // aggregation for the min
        builder.aggregation(AggregationBuilders.min("min_start_date")
                .field(StaticProperties.FEATURE_PROPERTIES_PATH + ".vdate"));
        // aggregation for the max
        builder.aggregation(AggregationBuilders.max("max_stop_date")
                .field(StaticProperties.FEATURE_PROPERTIES_PATH + ".vdate"));
        // aggregation for the sum
        builder.aggregation(AggregationBuilders.sum("sum_values_l1")
                .field(StaticProperties.FEATURE_PROPERTIES_PATH + ".value_l1"));
        SearchRequest request = new SearchRequest(objectSearchKey.getSearchIndex().toLowerCase()).source(builder);

        // get the results computed by ElasticSearch
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        Map<String, Aggregation> aggregations = response.getAggregations().asMap();

        // now lets actually test things
        Assert.assertEquals(objectsCreationCount, getDatasetProperty(dataset, "vcount").getValue());
        Assert.assertEquals((long) ((ParsedSum) aggregations.get("sum_values_l1")).getValue(),
                            getDatasetProperty(dataset, "values_l1_sum").getValue());
        // lets convert both dates to instant, it is the simpliest way to compare them
        Assert.assertEquals(Instant.parse(((ParsedMin) aggregations.get("min_start_date")).getValueAsString()),
                            ((OffsetDateTime) getDatasetProperty(dataset, "start_date").getValue()).toInstant());
        Assert.assertEquals(Instant.parse(((ParsedMax) aggregations.get("max_stop_date")).getValueAsString()),
                            ((OffsetDateTime) getDatasetProperty(dataset, "end_date").getValue()).toInstant());
        client.close();
    }

    private AbstractAttribute<?> getDatasetProperty(final Dataset pDataset, final String pPropertyName) {
        return pDataset.getProperties().stream().filter(p -> p.getName().equals(pPropertyName)).findAny().orElse(null);
    }

    /**
     * Import model definition file from resources directory
     * @param pFilename filename
     * @throws ModuleException if error occurs
     */
    private void importModel(final String pFilename) throws ModuleException {
        try {
            InputStream input = Files
                    .newInputStream(Paths.get("src", "test", "resources", "validation", "models", pFilename));
            modelService.importModel(input);

            List<AttributeModel> attributes = attributeModelService.getAttributes(null, null, null, null);
            gsonAttributeFactory.refresh(tenant, attributes);
        } catch (final IOException e) {
            String errorMessage = "Cannot import " + pFilename;
            throw new AssertionError(errorMessage);
        }
    }
}