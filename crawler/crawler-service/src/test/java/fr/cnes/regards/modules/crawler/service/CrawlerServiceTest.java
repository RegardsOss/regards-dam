package fr.cnes.regards.modules.crawler.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.modules.datasources.domain.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.domain.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.OracleDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.utils.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.utils.exceptions.DataSourcesPluginException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.service.adapters.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class CrawlerServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceTest.class);

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "T_DATA_OBJECTS";

    private static final String TENANT = "default";

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
    private ICrawlerService service;

    @Autowired
    private IIndexerService indexerService;

    private IDataSourceFromSingleTablePlugin dsPlugin;

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    @Before
    public void tearUp() throws DataSourcesPluginException, PluginUtilsException {
        /*
         * Initialize the DataSourceAttributeMapping
         */
        this.buildModelAttributes();

        // PlujginConf de connexion
        /*        PluginConfiguration pluginConf = getOracleConnectionConfiguration();
        PluginService pluginService;
        pluginService.savePluginConfiguration(pluginConf);
        
        // PluginConf de datasource
        // Cette datasource doit être positionnée sur la dataset
        PluginConfiguration dataSourcePluginConf = getOracleDataSource(pluginConf);
        pluginService.savePluginConfiguration(dataSourcePluginConf);
        
        // Find all des objets de la datasource
        IDataSourcePlugin dsPlugin = pluginService.getPlugin(dataSourcePluginConf.getId());
        dsPlugin.findAll(TENANT, null)*/

        /*
         * Instantiate the SQL DataSource plugin
         */
        List<PluginParameter> parameters;
        try {
            parameters = PluginParametersFactory.build()
                    .addParameterPluginConfiguration(OracleDataSourceFromSingleTablePlugin.CONNECTION_PARAM,
                                                     getOracleConnectionConfiguration())
                    .addParameter(OracleDataSourceFromSingleTablePlugin.TABLE_PARAM, TABLE_NAME_TEST)
                    .addParameter(OracleDataSourceFromSingleTablePlugin.MODEL_PARAM,
                                  adapter.toJson(dataSourceModelMapping))
                    .getParameters();
            dsPlugin = PluginUtils.getPlugin(parameters, OracleDataSourceFromSingleTablePlugin.class,
                                             Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        } catch (PluginUtilsException e) {
            throw new DataSourcesPluginException(e.getMessage());
        }

    }

    @After
    public void tearDown() throws Exception {
    }

    private PluginConfiguration getOracleDataSource(PluginConfiguration pluginConf) throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(OracleDataSourceFromSingleTablePlugin.CONNECTION_PARAM, pluginConf.getId().toString())
                .addParameter(OracleDataSourceFromSingleTablePlugin.TABLE_PARAM, "TABLE_MACHIN...")
                .addParameter(OracleDataSourceFromSingleTablePlugin.MODEL_PARAM, adapter.toJson(dataSourceModelMapping))
                .getParameters();

        return PluginUtils.getPluginConfiguration(parameters, OracleDataSourceFromSingleTablePlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    @Ignore
    @Test
    public void testSuckUp() {
        // register JSon data types (for ES)
        registerJSonModelAttributes();

        // Creating index if it doesn't already exist
        indexerService.deleteIndex(TENANT);
        indexerService.createIndex(TENANT);

        // Retrieve first 1000 objects

        Page<DataObject> page = dsPlugin.findAll(TENANT, new PageRequest(0, 1000));

        LOGGER.info(String.format("saving %d/%d entities...", page.getNumberOfElements(), page.getTotalElements()));
        Set<DataObject> set = Sets.newHashSet(page.getContent());
        Assert.assertEquals(page.getContent().size(), set.size());
        Map<String, Throwable> errorMap = indexerService.saveBulkEntities(TENANT, page.getContent());
        LOGGER.info(String.format("...%d entities saved",
                                  page.getNumberOfElements() - ((errorMap == null) ? 0 : errorMap.size())));
        while (page.hasNext()) {
            page = dsPlugin.findAll(TENANT, page.nextPageable());
            set = Sets.newHashSet(page.getContent());
            Assert.assertEquals(page.getContent().size(), set.size());
            LOGGER.info(String.format("saving %d/%d entities...", page.getNumberOfElements(), page.getTotalElements()));
            errorMap = indexerService.saveBulkEntities(TENANT, page.getContent());
            LOGGER.info(String.format("...%d entities saved",
                                      page.getNumberOfElements() - ((errorMap == null) ? 0 : errorMap.size())));
        }
        Assert.assertTrue(true);
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

        dataSourceModelMapping = new DataSourceModelMapping(123L, attributes);
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
