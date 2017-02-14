package fr.cnes.regards.modules.crawler.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
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
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.jpa.multitenant.resolver.CurrentTenantIdentifierResolverImpl;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.datasources.plugins.OracleDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.DataSourceAttributeMapping;
import fr.cnes.regards.modules.datasources.utils.DataSourceModelMapping;
import fr.cnes.regards.modules.datasources.utils.ModelMappingAdapter;
import fr.cnes.regards.modules.datasources.utils.exceptions.DataSourcesPluginException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.service.adapters.gson.FlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@Ignore
public class CrawlerServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceTest.class);

    @Autowired
    private FlattenedAttributeAdapterFactory gsonAttributeFactory;

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    private static final String TABLE_NAME_TEST = "T_DATA_OBJECTS";

    @Value("${oracle.datasource.url}")
    private String url;

    @Value("${oracle.datasource.username}")
    private String user;

    @Value("${oracle.datasource.password}")
    private String password;

    @Value("${oracle.datasource.driver}")
    private String driver;

    // private ICrawlerService service;

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolverImpl();
    }

    @Autowired
    private IIndexerService indexerService;

    private IDBDataSourcePlugin dsPlugin;

    private DataSourceModelMapping dataSourceModelMapping;

    private final ModelMappingAdapter adapter = new ModelMappingAdapter();

    @Before
    public void tearUp() throws DataSourcesPluginException {
        /*
         * Initialize the DataSourceAttributeMapping
         */
        this.buildModelAttributes();

        /*
         * Instantiate the SQL DataSource plugin
         */
        List<PluginParameter> parameters;
        try {
            parameters = PluginParametersFactory.build()
                    .addParameterPluginConfiguration(OracleDBDataSourcePlugin.CONNECTION_PARAM,
                                                     getOracleConnectionConfiguration())
                    .addParameter(PostgreDataSourcePlugin.MODEL_PARAM, adapter.toJson(dataSourceModelMapping))
                    .getParameters();
            dsPlugin = PluginUtils.getPlugin(parameters, OracleDBDataSourcePlugin.class,
                                             Arrays.asList(PLUGIN_CURRENT_PACKAGE));
        } catch (PluginUtilsException e) {
            throw new DataSourcesPluginException(e.getMessage());
        }

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSuckUp() {
        // register JSon data types (for ES)
        registerJSonModelAttributes();
        String tenant = currentTenantIdentifierResolver().resolveCurrentTenantIdentifier();

        // Creating index if it doesn't already exist
        indexerService.deleteIndex(tenant);
        indexerService.createIndex(tenant);

        // Retrieve first 1000 objects
        dsPlugin.setMapping(TABLE_NAME_TEST, dataSourceModelMapping);

        Page<DataObject> page = dsPlugin.findAll(new PageRequest(0, 1000));

        LOGGER.info(String.format("saving %d/%d entities...", page.getNumberOfElements(), page.getTotalElements()));
        Set<DataObject> set = Sets.newHashSet(page.getContent());
        Assert.assertEquals(page.getContent().size(), set.size());
        Map<String, Throwable> errorMap = indexerService.saveBulkEntities(tenant, page.getContent());
        LOGGER.info(String.format("...%d entities saved",
                                  page.getNumberOfElements() - ((errorMap == null) ? 0 : errorMap.size())));
        while (page.hasNext()) {
            page = dsPlugin.findAll(page.nextPageable());
            set = Sets.newHashSet(page.getContent());
            Assert.assertEquals(page.getContent().size(), set.size());
            LOGGER.info(String.format("saving %d/%d entities...", page.getNumberOfElements(), page.getTotalElements()));
            errorMap = indexerService.saveBulkEntities(tenant, page.getContent());
            LOGGER.info(String.format("...%d entities saved",
                                      page.getNumberOfElements() - ((errorMap == null) ? 0 : errorMap.size())));
        }
        Assert.assertTrue(true);
    }

    private PluginConfiguration getOracleConnectionConfiguration() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, user)
                .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, password)
                .addParameter(DefaultOracleConnectionPlugin.URL_PARAM, url)
                .addParameter(DefaultOracleConnectionPlugin.DRIVER_PARAM, driver)
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

        dataSourceModelMapping = new DataSourceModelMapping("ModelDeTest", attributes);
    }

    private void registerJSonModelAttributes() {
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "DATA_OBJECTS_ID");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "FILE_SIZE");
        gsonAttributeFactory.registerSubtype(StringAttribute.class, "FILE_TYPE");
        gsonAttributeFactory.registerSubtype(StringAttribute.class, "FILE_NAME_ORIGINE");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "DATA_SET_ID");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "DATA_TITLE");
        gsonAttributeFactory.registerSubtype(StringAttribute.class, "DATA_AUTHOR");
        gsonAttributeFactory.registerSubtype(StringAttribute.class, "DATA_AUTHOR_COMPANY");
        gsonAttributeFactory.registerSubtype(DateAttribute.class, "START_DATE");
        gsonAttributeFactory.registerSubtype(DateAttribute.class, "STOP_DATE");
        gsonAttributeFactory.registerSubtype(DateAttribute.class, "DATA_CREATION_DATE");

        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "MIN_LONGITUDE");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "MAX_LONGITUDE");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "MIN_LATITUDE");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "MAX_LATITUDE");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "MIN_ALTITUDE");
        gsonAttributeFactory.registerSubtype(IntegerAttribute.class, "MAX_ALTITUDE");
    }
}
