/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.max.InternalMax;
import org.elasticsearch.search.aggregations.metrics.min.InternalMin;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
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
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.configuration.IRabbitVirtualHostAdmin;
import fr.cnes.regards.framework.amqp.configuration.RegardsAmqpAdmin;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.crawler.domain.IngestionResult;
import fr.cnes.regards.modules.crawler.test.CrawlerConfiguration;
import fr.cnes.regards.modules.datasources.domain.AbstractAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.domain.DynamicAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.domain.StaticAttributeMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.OracleDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.domain.event.EntityEvent;
import fr.cnes.regards.modules.entities.plugin.CountElementAttribute;
import fr.cnes.regards.modules.entities.plugin.MaxDateAttribute;
import fr.cnes.regards.modules.entities.plugin.MinDateAttribute;
import fr.cnes.regards.modules.entities.plugin.SumIntegerAttribute;
import fr.cnes.regards.modules.entities.service.ICollectionService;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.modules.entities.service.adapters.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.builder.QueryBuilderCriterionVisitor;
import fr.cnes.regards.modules.indexer.domain.JoinEntitySearchKey;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.service.IIndexerService;
import fr.cnes.regards.modules.indexer.service.ISearchService;
import fr.cnes.regards.modules.indexer.service.Searches;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.plugins.utils.PluginUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class IndexerServiceDataSourceIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(IndexerServiceDataSourceIT.class);

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "T_DATA_OBJECTS";

    private static final String DATA_MODEL_FILE_NAME = "dataModel.xml";

    private static final String DATASET_MODEL_FILE_NAME = "datasetModel.xml";

    @Value("${regards.tenant}")
    private String tenant;

    @Value("${oracle.datasource.host}")
    private String dbHost;

    @Value("${oracle.datasource.port}")
    private String dbPort;

    @Value("${oracle.datasource.name}")
    private String dbName;

    @Value("${oracle.datasource.username}")
    private String dbUser;

    @Value("${oracle.datasource.password}")
    private String dbPpassword;

    @Value("${oracle.datasource.driver}")
    private String driver;

    @Value("${regards.elasticsearch.host:}")
    private String esHost;

    @Value("${regards.elasticsearch.address:}")
    private String esAddress;

    @Value("${regards.elasticsearch.tcp.port}")
    private int esPort;

    @Value("${regards.elasticsearch.cluster.name}")
    private String esClusterName;

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
    private ICollectionService collService;

    @Autowired
    private IIndexerService indexerService;

    @Autowired
    private ISearchService searchService;

    @Autowired
    private ICrawlerService crawlerService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepos;

    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepo;

    @Autowired
    private IRabbitVirtualHostAdmin rabbitVhostAdmin;

    @Autowired
    private RegardsAmqpAdmin amqpAdmin;

    private Model dataModel;

    private Model datasetModel;

    private PluginConfiguration dataSourcePluginConf;

    private Dataset dataset1;

    private Dataset dataset2;

    private Dataset dataset3;

    private PluginConfiguration dBConnectionConf;

    @Before
    public void setUp() throws Exception {
        tenantResolver.forceTenant(tenant);
        if (esRepos.indexExists(tenant)) {
            esRepos.deleteIndex(tenant);
        }
        esRepos.createIndex(tenant);

        crawlerService.setConsumeOnlyMode(false);

        rabbitVhostAdmin.bind(tenantResolver.getTenant());
        amqpAdmin.purgeQueue(EntityEvent.class, false);
        rabbitVhostAdmin.unbind();

        entityRepos.deleteAll();
        modelAttrAssocRepo.deleteAll();
        pluginConfRepo.deleteAll();
        attrModelRepo.deleteAll();
        modelRepository.deleteAll();
        fragRepo.deleteAll();
        pluginService.addPluginPackage("fr.cnes.regards.modules.datasources.plugins");

        // registerJSonModelAttributes();

        // get the plugin configuration for computed attributes
        initPluginConfForComputedAttributes();
        // get a model for DataObject, by importing them it also register them for (de)serialization
        importModel(DATA_MODEL_FILE_NAME);
        dataModel = modelService.getModelByName("model_1");

        // get a model for Dataset
        importModel(DATASET_MODEL_FILE_NAME);
        datasetModel = modelService.getModelByName("model_ds_1");

        // Initialize the AbstractAttributeMapping
        buildModelAttributes();

        // Connection PluginConf
        dBConnectionConf = getOracleConnectionConfiguration();
        pluginService.savePluginConfiguration(dBConnectionConf);

        DefaultOracleConnectionPlugin dbCtx = pluginService.getPlugin(dBConnectionConf);
        Assume.assumeTrue(dbCtx.testConnection());

        // DataSource PluginConf
        dataSourcePluginConf = getOracleDataSource(dBConnectionConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf);
    }

    private void initPluginConfForComputedAttributes() throws ModuleException {
        pluginService.addPluginPackage(IComputedAttribute.class.getPackage().getName());
        pluginService.addPluginPackage(CountElementAttribute.class.getPackage().getName());
        // conf for "count"
        List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter("attributeToComputeName", "count").getParameters();
        PluginMetaData metadata = new PluginMetaData();
        metadata.setPluginId("CountElementAttribute");
        metadata.setAuthor("toto");
        metadata.setDescription("titi");
        metadata.setVersion("tutu");
        metadata.getInterfaceNames().add(IComputedAttribute.class.getName());
        metadata.setPluginClassName(CountElementAttribute.class.getName());
        PluginConfiguration conf = new PluginConfiguration(metadata, "CountElementTestConf");
        conf.setParameters(parameters);
        conf = pluginService.savePluginConfiguration(conf);
        // create a pluginConfiguration with a label for START_DATE
        List<PluginParameter> parametersMin = PluginParametersFactory.build()
                .addParameter("attributeToComputeName", "START_DATE").getParameters();
        PluginMetaData metadataMin = new PluginMetaData();
        metadataMin.setPluginId("MinDateAttribute");
        metadataMin.setAuthor("toto");
        metadataMin.setDescription("titi");
        metadataMin.setVersion("tutu");
        metadataMin.getInterfaceNames().add(IComputedAttribute.class.getName());
        metadataMin.setPluginClassName(MinDateAttribute.class.getName());
        PluginConfiguration confMin = new PluginConfiguration(metadataMin, "MinDateTestConf");
        confMin.setParameters(parametersMin);
        confMin = pluginService.savePluginConfiguration(confMin);
        // create a pluginConfiguration with a label for STOP_DATE
        List<PluginParameter> parametersMax = PluginParametersFactory.build()
                .addParameter("attributeToComputeName", "STOP_DATE").getParameters();
        PluginMetaData metadataMax = new PluginMetaData();
        metadataMax.setPluginId("MaxDateAttribute");
        metadataMax.setAuthor("toto");
        metadataMax.setDescription("titi");
        metadataMax.setVersion("tutu");
        metadataMax.getInterfaceNames().add(IComputedAttribute.class.getName());
        metadataMax.setPluginClassName(MaxDateAttribute.class.getName());
        PluginConfiguration confMax = new PluginConfiguration(metadataMax, "MaxDateTestConf");
        confMax.setParameters(parametersMax);
        confMax = pluginService.savePluginConfiguration(confMax);
        // create a pluginConfiguration with a label for FILE_SIZE
        List<PluginParameter> parametersInteger = PluginParametersFactory.build()
                .addParameter("attributeToComputeName", "FILE_SIZE").getParameters();
        PluginMetaData metadataInteger = new PluginMetaData();
        metadataInteger.setPluginId("SumIntegerAttribute");
        metadataInteger.setAuthor("toto");
        metadataInteger.setDescription("titi");
        metadataInteger.setVersion("tutu");
        metadataInteger.getInterfaceNames().add(IComputedAttribute.class.getName());
        metadataInteger.setPluginClassName(SumIntegerAttribute.class.getName());
        PluginConfiguration confInteger = new PluginConfiguration(metadataInteger, "SumIntegerTestConf");
        confInteger.setParameters(parametersInteger);
        confInteger = pluginService.savePluginConfiguration(confInteger);
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

    private PluginConfiguration getOracleDataSource(PluginConfiguration pluginConf) {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameterPluginConfiguration(OracleDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(OracleDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.REFRESH_RATE, "1800")
                .addParameter(OracleDataSourceFromSingleTablePlugin.MODEL_PARAM, adapter.toJson(dataSourceModelMapping))
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, OracleDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getOracleConnectionConfiguration() {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, dbUser)
                .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, dbPpassword)
                .addParameter(DefaultOracleConnectionPlugin.DB_HOST_PARAM, dbHost)
                .addParameter(DefaultOracleConnectionPlugin.DB_PORT_PARAM, dbPort)
                .addParameter(DefaultOracleConnectionPlugin.DB_NAME_PARAM, dbName)
                .addParameter(DefaultOracleConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultOracleConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultOracleConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private void buildModelAttributes() {
        List<AbstractAttributeMapping> attributes = new ArrayList<AbstractAttributeMapping>();

        attributes.add(new StaticAttributeMapping(AttributeType.INTEGER, "DATA_OBJECTS_ID",
                AbstractAttributeMapping.PRIMARY_KEY));

        attributes.add(new DynamicAttributeMapping("FILE_SIZE", AttributeType.INTEGER, "FILE_SIZE"));
        attributes.add(new DynamicAttributeMapping("FILE_TYPE", AttributeType.STRING, "FILE_TYPE"));
        attributes.add(new DynamicAttributeMapping("FILE_NAME_ORIGINE", AttributeType.STRING, "FILE_NAME_ORIGINE"));

        attributes.add(new DynamicAttributeMapping("DATA_SET_ID", AttributeType.INTEGER, "DATA_SET_ID"));
        attributes.add(new DynamicAttributeMapping("DATA_TITLE", AttributeType.STRING, "DATA_TITLE"));
        attributes.add(new DynamicAttributeMapping("DATA_AUTHOR", AttributeType.STRING, "DATA_AUTHOR"));
        attributes.add(new DynamicAttributeMapping("DATA_AUTHOR_COMPANY", AttributeType.STRING, "DATA_AUTHOR_COMPANY"));

        attributes.add(new DynamicAttributeMapping("START_DATE", AttributeType.DATE_ISO8601, "START_DATE",
                Types.DECIMAL));
        attributes
                .add(new DynamicAttributeMapping("STOP_DATE", AttributeType.DATE_ISO8601, "STOP_DATE", Types.DECIMAL));
        attributes.add(new DynamicAttributeMapping("DATA_CREATION_DATE", AttributeType.DATE_ISO8601,
                "DATA_CREATION_DATE", Types.DECIMAL));

        attributes.add(new DynamicAttributeMapping("MIN", "LONGITUDE", AttributeType.INTEGER, "MIN_LONGITUDE"));
        attributes.add(new DynamicAttributeMapping("MAX", "LONGITUDE", AttributeType.INTEGER, "MAX_LONGITUDE"));
        attributes.add(new DynamicAttributeMapping("MIN", "LATITUDE", AttributeType.INTEGER, "MIN_LATITUDE"));
        attributes.add(new DynamicAttributeMapping("MAX", "LATITUDE", AttributeType.INTEGER, "MAX_LATITUDE"));
        attributes.add(new DynamicAttributeMapping("MIN", "ALTITUDE", AttributeType.INTEGER, "MIN_ALTITUDE"));
        attributes.add(new DynamicAttributeMapping("MAX", "ALTITUDE", AttributeType.INTEGER, "MAX_ALTITUDE"));
        attributes.add(new DynamicAttributeMapping("ANSA5_REAL", AttributeType.DOUBLE, "ANSA5_REAL"));
        attributes.add(new DynamicAttributeMapping("ANSR5_REAL", AttributeType.DOUBLE, "ANSR5_REAL"));
        attributes.add(new DynamicAttributeMapping("ANSE5_REAL", AttributeType.DOUBLE, "ANSE5_REAL"));
        attributes.add(new DynamicAttributeMapping("ANSE6_STRING", AttributeType.STRING, "ANSE6_STRING"));
        attributes.add(new DynamicAttributeMapping("ANSL6_2_STRING", AttributeType.STRING, "ANSL6_2_STRING"));
        attributes.add(new DynamicAttributeMapping("ANSR3_INT", "frag3", AttributeType.INTEGER, "ANSR3_INT"));
        attributes.add(new DynamicAttributeMapping("ANSL3_1_INT", "frag3", AttributeType.INTEGER, "ANSL3_1_INT"));
        attributes.add(new DynamicAttributeMapping("ANSL3_2_INT", "frag3", AttributeType.INTEGER, "ANSL3_2_INT"));

        attributes
                .add(new StaticAttributeMapping(AttributeType.STRING, "ANSA7_URL", AbstractAttributeMapping.THUMBNAIL));
        attributes
                .add(new StaticAttributeMapping(AttributeType.STRING, "ANSE7_URL", AbstractAttributeMapping.RAW_DATA));

        dataSourceModelMapping = new DataSourceModelMapping(dataModel.getId(), attributes);
    }

    private void registerJSonModelAttributes() {
        String tenant = tenantResolver.getTenant();
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "FILE_SIZE");
        gsonAttributeFactory.registerSubtype(tenant, StringAttribute.class, "FILE_TYPE");
        gsonAttributeFactory.registerSubtype(tenant, StringAttribute.class, "FILE_NAME_ORIGINE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "DATA_SET_ID");
        gsonAttributeFactory.registerSubtype(tenant, StringAttribute.class, "DATA_TITLE");
        gsonAttributeFactory.registerSubtype(tenant, StringAttribute.class, "DATA_AUTHOR");
        gsonAttributeFactory.registerSubtype(tenant, StringAttribute.class, "DATA_AUTHOR_COMPANY");
        gsonAttributeFactory.registerSubtype(tenant, DateAttribute.class, "START_DATE");
        gsonAttributeFactory.registerSubtype(tenant, DateAttribute.class, "STOP_DATE");
        gsonAttributeFactory.registerSubtype(tenant, DateAttribute.class, "DATA_CREATION_DATE");

        gsonAttributeFactory.registerSubtype(tenant, ObjectAttribute.class, "LONGITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MIN", "LONGITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MAX", "LONGITUDE");
        gsonAttributeFactory.registerSubtype(tenant, ObjectAttribute.class, "LATITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MIN", "LATITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MAX", "LATITUDE");
        gsonAttributeFactory.registerSubtype(tenant, ObjectAttribute.class, "ALTITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MIN", "ALTITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MAX", "ALTITUDE");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "ANSA5_REAL");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "ANSR5_REAL");
        gsonAttributeFactory.registerSubtype(tenant, DoubleAttribute.class, "ANSE5_REAL");
        gsonAttributeFactory.registerSubtype(tenant, StringAttribute.class, "ANSE6_STRING");
        gsonAttributeFactory.registerSubtype(tenant, StringAttribute.class, "ANSL6_2_STRING");
        gsonAttributeFactory.registerSubtype(tenant, ObjectAttribute.class, "frag3");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "ANSR3_INT", "frag3");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "ANSL3_1_INT", "frag3");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "ANSL3_2_INT", "frag3");
    }

    @Test
    public void test() throws ModuleException, IOException, InterruptedException {
        String tenant = tenantResolver.getTenant();

        // Creation
        IngestionResult summary1 = crawlerService.ingest(dataSourcePluginConf);

        // Update
        IngestionResult summary2 = crawlerService.ingest(dataSourcePluginConf);
        Assert.assertEquals(summary1.getSavedObjectsCount(), summary2.getSavedObjectsCount());

        crawlerService.startWork();
        // Create 3 Datasets on all objects
        dataset1 = new Dataset(datasetModel, tenant, "dataset label 1");
        dataset1.setDataModel(dataModel.getId());
        dataset1.setSubsettingClause(ICriterion.all());
        dataset1.setLicence("licence");
        dataset1.setDataSource(dataSourcePluginConf);
        dataset1.setTags(Sets.newHashSet("BULLSHIT"));
        dataset1.setGroups(Sets.newHashSet("group0", "group11"));
        dsService.create(dataset1);

        dataset2 = new Dataset(datasetModel, tenant, "dataset label 2");
        dataset2.setDataModel(dataModel.getId());
        dataset2.setSubsettingClause(ICriterion.all());
        dataset2.setTags(Sets.newHashSet("BULLSHIT"));
        dataset2.setLicence("licence");
        dataset2.setDataSource(dataSourcePluginConf);
        dataset2.setGroups(Sets.newHashSet("group12", "group11"));
        dsService.create(dataset2);

        dataset3 = new Dataset(datasetModel, tenant, "dataset label 3");
        dataset3.setDataModel(dataModel.getId());
        dataset3.setSubsettingClause(ICriterion.all());
        dataset3.setLicence("licence");
        dataset3.setDataSource(dataSourcePluginConf);
        dataset3.setGroups(Sets.newHashSet("group2"));
        dsService.create(dataset3);

        crawlerService.waitForEndOfWork();
        Thread.sleep(10_000);
        // indexerService.refresh(tenant);

        // Retrieve dataset1 from ES
        dataset1 = (Dataset) searchService.get(dataset1.getIpId());
        Assert.assertNotNull(dataset1);

        // SearchKey<DataObject> objectSearchKey = new SearchKey<>(tenant, EntityType.DATA.toString(),
        // DataObject.class);
        SimpleSearchKey<DataObject> objectSearchKey = Searches.onSingleEntity(tenant, EntityType.DATA);
        // check that computed attribute were correclty done
        checkDatasetComputedAttribute(dataset1, objectSearchKey, summary1.getSavedObjectsCount());
        // Search for DataObjects tagging dataset1
        Page<DataObject> objectsPage = searchService.search(objectSearchKey, IEsRepository.BULK_SIZE,
                                                            ICriterion.eq("tags", dataset1.getIpId().toString()));
        Assert.assertTrue(objectsPage.getContent().size() > 0);
        Assert.assertEquals(summary1.getSavedObjectsCount(), objectsPage.getContent().size());
        // All data are associated with the 3 datasets so they must all have the 4 groups
        for (DataObject object : objectsPage.getContent()) {
            Assert.assertTrue(object.getGroups().contains("group0"));
            Assert.assertTrue(object.getGroups().contains("group11"));
            Assert.assertTrue(object.getGroups().contains("group12"));
            Assert.assertTrue(object.getGroups().contains("group2"));
            Assert.assertEquals(object.getDatasetModelIds().iterator().next(), datasetModel.getId());
        }

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
        objectsPage.getContent().forEach(object -> object.getTags().add("TOTO"));
        esRepos.saveBulk(tenant, objectsPage.getContent());

        esRepos.refresh(tenant);

        // Search for DataObjects tagging dataset2
        objectsPage = searchService.search(objectSearchKey, IEsRepository.BULK_SIZE,
                                           ICriterion.eq("tags", dataset2.getIpId().toString()));
        Assert.assertTrue(objectsPage.getContent().size() > 0);
        Assert.assertEquals(summary1.getSavedObjectsCount(), objectsPage.getContent().size());
        // dataset1 has bee removed so objects must have "group11", "group12" (from dataset2), "group2" (from dataset3)
        // but not "group0" (only on dataset1)
        for (DataObject object : objectsPage.getContent()) {
            Assert.assertFalse(object.getGroups().contains("group0"));
            Assert.assertTrue(object.getGroups().contains("group11"));
            Assert.assertTrue(object.getGroups().contains("group12"));
            Assert.assertTrue(object.getGroups().contains("group2"));
        }

        // Search for Dataset but with criterion on DataObjects
        // SearchKey<Dataset> dsSearchKey = new SearchKey<>(tenant, EntityType.DATA.toString(), Dataset.class);
        JoinEntitySearchKey<DataObject, Dataset> dsSearchKey = Searches
                .onSingleEntityReturningJoinEntity(tenant, EntityType.DATA, EntityType.DATASET);
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
        JoinEntitySearchKey<AbstractEntity, Dataset> dsSearchKey2 = Searches
                .onAllEntitiesReturningJoinEntity(tenant, EntityType.DATASET);
        dsPage = searchService.search(dsSearchKey, 1, ICriterion.all());
        Assert.assertNotNull(dsPage);
        Assert.assertFalse(dsPage.getContent().isEmpty());
        Assert.assertEquals(1, dsPage.getContent().size());

        dsPage = searchService.search(dsSearchKey2, dsPage.nextPageable(), ICriterion.all());
        Assert.assertNotNull(dsPage);
        Assert.assertFalse(dsPage.getContent().isEmpty());
        Assert.assertEquals(1, dsPage.getContent().size());

        objectsPage = searchService.multiFieldsSearch(objectSearchKey, IEsRepository.BULK_SIZE, 13,
                                                      "properties.DATA_OBJECTS_ID", "properties.FILE_SIZE",
                                                      "properties.LONGITUDE.MAX", "properties.LATITUDE.MAX");
        Assert.assertFalse(objectsPage.getContent().isEmpty());
        Assert.assertEquals(3092, objectsPage.getContent().size());
    }

    private void checkDatasetComputedAttribute(Dataset pDataset, SimpleSearchKey<DataObject> pObjectSearchKey,
            long objectsCreationCount) {
        TransportClient client = new PreBuiltTransportClient(
                Settings.builder().put("cluster.name", esClusterName).build());
        try {
            client.addTransportAddress(new InetSocketTransportAddress(
                    InetAddress.getByName((!Strings.isNullOrEmpty(esHost)) ? esHost : esAddress), esPort));
        } catch (UnknownHostException e) {
            LOGGER.error("could not get a connection to ES in the middle of the test where we know ES is available", e);
            Assert.fail();
        }
        // lets build the request so elasticsearch can calculate the few attribute we are using in test(min(START_DATE),
        // max(STOP_DATE), sum(FILE_SIZE) via aggregation, the count of element in this context is already known:
        // objectsCreationCount
        QueryBuilderCriterionVisitor critVisitor = new QueryBuilderCriterionVisitor();
        ICriterion crit = ICriterion.eq("tags", pDataset.getIpId().toString());
        QueryBuilder qb = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
                .filter(crit.accept(critVisitor));
        // now we have a request on the right data
        SearchRequestBuilder searchRequest = client.prepareSearch(pObjectSearchKey.getSearchIndex().toLowerCase())
                .setTypes(pObjectSearchKey.getSearchTypes()).setQuery(qb).setSize(0);
        // lets build the aggregations
        // aggregation for the min
        searchRequest.addAggregation(AggregationBuilders.min("min_start_date").field("properties.START_DATE"));
        // aggregation for the max
        searchRequest.addAggregation(AggregationBuilders.max("max_stop_date").field("properties.STOP_DATE"));
        // aggregation for the sum
        searchRequest.addAggregation(AggregationBuilders.sum("sum_file_size").field("properties.FILE_SIZE"));

        // get the results computed by ElasticSearch
        SearchResponse response = searchRequest.get();
        Map<String, Aggregation> aggregations = response.getAggregations().asMap();

        // now lets actually test things
        Assert.assertEquals(objectsCreationCount, getDatasetProperty(pDataset, "count").getValue());
        Assert.assertEquals((int) ((InternalSum) aggregations.get("sum_file_size")).getValue(),
                            getDatasetProperty(pDataset, "FILE_SIZE").getValue());
        // lets convert both dates to instant, it is the simpliest way to compare them
        Assert.assertEquals(Instant.parse(((InternalMin) aggregations.get("min_start_date")).getValueAsString()),
                            ((LocalDateTime) getDatasetProperty(pDataset, "START_DATE").getValue())
                                    .toInstant(ZoneOffset.UTC));
        Assert.assertEquals(Instant.parse(((InternalMax) aggregations.get("max_stop_date")).getValueAsString()),
                            ((LocalDateTime) getDatasetProperty(pDataset, "STOP_DATE").getValue())
                                    .toInstant(ZoneOffset.UTC));
        client.close();
    }

    private AbstractAttribute<?> getDatasetProperty(Dataset pDataset, String pPropertyName) {
        return pDataset.getProperties().stream().filter(p -> p.getName().equals(pPropertyName)).findAny().orElse(null);
    }

    /**
     * Import model definition file from resources directory
     *
     * @param pFilename filename
     * @return list of created model attributes
     * @throws ModuleException if error occurs
     */
    private void importModel(String pFilename) throws ModuleException {
        try {
            final InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));
            modelService.importModel(input);
            gsonAttributeFactory.refresh(tenant);
        } catch (IOException e) {
            String errorMessage = "Cannot import " + pFilename;
            throw new AssertionError(errorMessage);
        }
    }
}
