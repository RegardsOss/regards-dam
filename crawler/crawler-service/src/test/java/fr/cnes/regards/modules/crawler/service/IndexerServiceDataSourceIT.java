package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.sql.Types;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.OracleDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.ModelMappingAdapter;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.service.IEntityService;
import fr.cnes.regards.modules.entities.service.adapters.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@DirtiesContext // because there are 2 Configuration classes in package
public class IndexerServiceDataSourceIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(IndexerServiceDataSourceIT.class);

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "T_DATA_OBJECTS";

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

    @Autowired
    private IModelService modelService;

    @Autowired
    @Qualifier("entityService")
    private IEntityService entityService;

    @Autowired
    private IIndexerService indexerService;

    @Autowired
    private ICrawlerService crawlerService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepos;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    @Autowired
    private IPluginService pluginService;

    private Model dataModel;

    private Model datasetModel;

    private PluginConfiguration dataSourcePluginConf;

    private Dataset dataset1;

    @Before
    public void setUp() throws Exception {
        pluginService.addPluginPackage("fr.cnes.regards.modules.datasources.plugins");

        // Register model attributes
        registerJSonModelAttributes();
        dataModel = new Model();
        dataModel.setName("model_1");
        dataModel.setType(EntityType.DATA);
        dataModel.setVersion("1");
        dataModel.setDescription("Test data object model");
        modelService.createModel(dataModel);

        datasetModel = new Model();
        datasetModel.setName("model_ds_1");
        datasetModel.setType(EntityType.DATASET);
        datasetModel.setVersion("1");
        datasetModel.setDescription("Test dataset model");
        modelService.createModel(datasetModel);

        // Initialize the DataSourceAttributeMapping
        this.buildModelAttributes();

        // Connection PluginConf
        PluginConfiguration pluginConf = getOracleConnectionConfiguration();
        pluginService.savePluginConfiguration(pluginConf);

        // DataSource PluginConf
        dataSourcePluginConf = getOracleDataSource(pluginConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf);

    }

    @After
    public void clean() {
        // Don't use entity service to clean because events are published on RabbitMQ
        Utils.execute(entityRepos::delete, dataset1.getId());

        Utils.execute(modelService::deleteModel, datasetModel.getId());
        Utils.execute(modelService::deleteModel, dataModel.getId());
    }

    private PluginConfiguration getOracleDataSource(PluginConfiguration pluginConf) throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameterPluginConfiguration(OracleDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(OracleDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST)
                .addParameter(OracleDataSourceFromSingleTablePlugin.MODEL_PARAM, adapter.toJson(dataSourceModelMapping))
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, OracleDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getOracleConnectionConfiguration() throws PluginUtilsException {
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
        List<DataSourceAttributeMapping> attributes = new ArrayList<DataSourceAttributeMapping>();

        attributes
                .add(new DataSourceAttributeMapping("DATA_OBJECTS_ID", AttributeType.INTEGER, "DATA_OBJECTS_ID", true));

        attributes.add(new DataSourceAttributeMapping("FILE_SIZE", AttributeType.INTEGER, "FILE_SIZE"));
        attributes.add(new DataSourceAttributeMapping("FILE_TYPE", AttributeType.STRING, "FILE_TYPE"));
        attributes.add(new DataSourceAttributeMapping("FILE_NAME_ORIGINE", AttributeType.STRING, "FILE_NAME_ORIGINE"));

        attributes.add(new DataSourceAttributeMapping("DATA_SET_ID", AttributeType.INTEGER, "DATA_SET_ID"));
        attributes.add(new DataSourceAttributeMapping("DATA_TITLE", AttributeType.STRING, "DATA_TITLE"));
        attributes.add(new DataSourceAttributeMapping("DATA_AUTHOR", AttributeType.STRING, "DATA_AUTHOR"));
        attributes.add(new DataSourceAttributeMapping("DATA_AUTHOR_COMPANY", AttributeType.STRING,
                "DATA_AUTHOR_COMPANY"));

        attributes.add(new DataSourceAttributeMapping("START_DATE", AttributeType.DATE_ISO8601, "START_DATE",
                Types.DECIMAL));
        attributes.add(new DataSourceAttributeMapping("STOP_DATE", AttributeType.DATE_ISO8601, "STOP_DATE",
                Types.DECIMAL));
        attributes.add(new DataSourceAttributeMapping("DATA_CREATION_DATE", AttributeType.DATE_ISO8601,
                "DATA_CREATION_DATE", Types.DECIMAL));

        attributes.add(new DataSourceAttributeMapping("MIN_LONGITUDE", AttributeType.INTEGER, "MIN_LONGITUDE"));
        attributes.add(new DataSourceAttributeMapping("MAX_LONGITUDE", AttributeType.INTEGER, "MAX_LONGITUDE"));
        attributes.add(new DataSourceAttributeMapping("MIN_LATITUDE", AttributeType.INTEGER, "MIN_LATITUDE"));
        attributes.add(new DataSourceAttributeMapping("MAX_LATITUDE", AttributeType.INTEGER, "MAX_LATITUDE"));
        attributes.add(new DataSourceAttributeMapping("MIN_ALTITUDE", AttributeType.INTEGER, "MIN_ALTITUDE"));
        attributes.add(new DataSourceAttributeMapping("MAX_ALTITUDE", AttributeType.INTEGER, "MAX_ALTITUDE"));

        dataSourceModelMapping = new DataSourceModelMapping(dataModel.getId(), attributes);
    }

    private void registerJSonModelAttributes() {
        String tenant = tenantResolver.getTenant();
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "DATA_OBJECTS_ID");
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

        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MIN_LONGITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MAX_LONGITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MIN_LATITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MAX_LATITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MIN_ALTITUDE");
        gsonAttributeFactory.registerSubtype(tenant, IntegerAttribute.class, "MAX_ALTITUDE");
    }

    @Test
    public void test() throws ModuleException, IOException, InterruptedException {
        String tenant = tenantResolver.getTenant();
        // First delete index if it already exists
        indexerService.deleteIndex(tenant);

        // Creation
        crawlerService.ingest(dataSourcePluginConf);

        // Update
        crawlerService.ingest(dataSourcePluginConf);

        dataset1 = new Dataset(datasetModel, tenant, "dataset label");
        dataset1.setDataModel(dataModel);
        dataset1.setSubsettingClause(ICriterion.all());
        dataset1.setLicence("licence");
        dataset1.setDataSource(dataSourcePluginConf);
        entityService.create(dataset1);

        Thread.sleep(10_000);

        dataset1 = (Dataset) indexerService.get(dataset1.getIpId());
        int i = 0;
        while (dataset1 == null) {
            Thread.sleep(1000);
            i++;
            if (i == 3) {
                break;
            }
        }
        Assert.assertNotNull(dataset1);
    }

}
