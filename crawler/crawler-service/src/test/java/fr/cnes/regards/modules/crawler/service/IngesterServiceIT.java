package fr.cnes.regards.modules.crawler.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.crawler.service.ds.ExternalData2Repository;
import fr.cnes.regards.modules.crawler.service.ds.ExternalData3Repository;
import fr.cnes.regards.modules.crawler.service.ds.ExternalDataRepository;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.DefaultPostgreConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.ModelMappingAdapter;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.LongAttribute;
import fr.cnes.regards.modules.entities.service.adapters.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class IngesterServiceIT {

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TENANT = "INGEST";

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    private static final String T_DATA_1 = "T_DATA";

    private static final String T_DATA_2 = "T_DATA_2";

    private static final String T_DATA_3 = "T_DATA_3";

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

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    private Model dataModel;

    private Model datasetModel;

    private PluginConfiguration dataSourcePluginConf1;

    private PluginConfiguration dataSourcePluginConf2;

    private PluginConfiguration dataSourcePluginConf3;

    private Dataset dataset;

    private PluginConfiguration dBConnectionConf;

    @Autowired
    private ExternalDataRepository extData1Repos;

    @Autowired
    private ExternalData2Repository extData2Repos;

    @Autowired
    private ExternalData3Repository extData3Repos;

    @Autowired
    private ICrawlerService crawlerService;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IPluginService pluginService;

    private PluginConfiguration getPostgresDataSource1(PluginConfiguration pluginConf) throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameterPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.TABLE_PARAM, T_DATA_1)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_PARAM,
                              adapter.toJson(dataSourceModelMapping))
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getPostgresDataSource2(PluginConfiguration pluginConf) throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameterPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.TABLE_PARAM, T_DATA_2)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_PARAM,
                              adapter.toJson(dataSourceModelMapping))
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getPostgresDataSource3(PluginConfiguration pluginConf) throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameterPluginConfiguration(PostgreDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.TABLE_PARAM, T_DATA_3)
                .addParameter(PostgreDataSourceFromSingleTablePlugin.MODEL_PARAM,
                              adapter.toJson(dataSourceModelMapping))
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, PostgreDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    private PluginConfiguration getPostgresConnectionConfiguration() throws PluginUtilsException {
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
        List<DataSourceAttributeMapping> attributes = new ArrayList<DataSourceAttributeMapping>();

        attributes.add(new DataSourceAttributeMapping("ext_data_id", AttributeType.LONG, "id",
                DataSourceAttributeMapping.PRIMARY_KEY));

        attributes.add(new DataSourceAttributeMapping("date", AttributeType.DATE_ISO8601, "date",
                DataSourceAttributeMapping.LAST_UPDATE));

        dataSourceModelMapping = new DataSourceModelMapping(dataModel.getId(), attributes);
    }

    private void registerJSonModelAttributes() {
        String tenant = tenantResolver.getTenant();
        gsonAttributeFactory.registerSubtype(tenant, LongAttribute.class, "ext_data_id");
        gsonAttributeFactory.registerSubtype(tenant, DateAttribute.class, "date");
    }

    @Before
    public void setUp() throws Exception {
        tenantResolver.forceTenant(TENANT);

        crawlerService.setConsumeOnlyMode(false);

        //        entityRepos.deleteAll();
        //        modelRepository.deleteAll();
        extData1Repos.deleteAll();
        extData2Repos.deleteAll();
        extData3Repos.deleteAll();

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
        buildModelAttributes();

        // Connection PluginConf
        dBConnectionConf = getPostgresConnectionConfiguration();
        pluginService.savePluginConfiguration(dBConnectionConf);

        DefaultPostgreConnectionPlugin dbCtx = pluginService.getPlugin(dBConnectionConf);
        Assume.assumeTrue(dbCtx.testConnection());

        // DataSource PluginConf
        dataSourcePluginConf1 = getPostgresDataSource1(dBConnectionConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf1);

        dataSourcePluginConf2 = getPostgresDataSource2(dBConnectionConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf2);

        dataSourcePluginConf3 = getPostgresDataSource3(dBConnectionConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf3);
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

        if (datasetModel != null) {
            Utils.execute(modelService::deleteModel, datasetModel.getId());
        }
        if (dataModel != null) {
            Utils.execute(modelService::deleteModel, dataModel.getId());
        }

    }

    @Test
    public void test() {
        ingesterService.manage();
    }
}
