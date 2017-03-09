package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
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

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.test.util.Beans;
import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.datasources.plugins.DefaultOracleConnectionPlugin;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.IEntityService;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * Crawler service integration tests
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
@DirtiesContext // because there are 2 Configuration classes in package
public class CrawlerServiceIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceIT.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    @Value("${regards.tenant}")
    private String tenant;

    private Model modelColl;

    private Model modelDataset;

    private Dataset dataset1;

    private Dataset dataset2;

    private Dataset dataset3;

    private Collection coll1;

    private Collection coll2;

    private Collection coll3;

    private PluginConfiguration pluginConf;

    @Autowired
    private IModelService modelService;

    @Autowired
    @Qualifier(value = "entityService")
    private IEntityService entityService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepos;

    @Autowired
    private IEsRepository esRepos;

    @Autowired
    private IPluginService pluginService;

    @After
    public void clean() {
        // Don't use entity service to clean because events are published on RabbitMQ
        Utils.execute(entityRepos::delete, dataset1.getId());
        Utils.execute(entityRepos::delete, dataset2.getId());
        Utils.execute(entityRepos::delete, dataset3.getId());
        Utils.execute(entityRepos::delete, coll1.getId());
        Utils.execute(entityRepos::delete, coll2.getId());
        Utils.execute(entityRepos::delete, coll3.getId());

        Utils.execute(modelService::deleteModel, modelColl.getId());
        Utils.execute(modelService::deleteModel, modelDataset.getId());
        Utils.execute(pluginService::deletePluginConfiguration, pluginConf.getId());

        esRepos.deleteIndex(tenant);
    }

    private PluginConfiguration getOracleConnectionConfiguration() throws PluginUtilsException {
        final List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(DefaultOracleConnectionPlugin.USER_PARAM, "toto")
                .addParameter(DefaultOracleConnectionPlugin.PASSWORD_PARAM, "toto")
                .addParameter(DefaultOracleConnectionPlugin.DB_HOST_PARAM, "toto")
                .addParameter(DefaultOracleConnectionPlugin.DB_PORT_PARAM, "toto")
                .addParameter(DefaultOracleConnectionPlugin.DB_NAME_PARAM, "toto")
                .addParameter(DefaultOracleConnectionPlugin.MAX_POOLSIZE_PARAM, "3")
                .addParameter(DefaultOracleConnectionPlugin.MIN_POOLSIZE_PARAM, "1").getParameters();

        return PluginUtils.getPluginConfiguration(parameters, DefaultOracleConnectionPlugin.class,
                                                  Arrays.asList(PLUGIN_CURRENT_PACKAGE));
    }

    public void buildData1() throws ModuleException, PluginUtilsException {
        modelColl = Model.build("modelColl", "model desc", EntityType.COLLECTION);
        modelColl = modelService.createModel(modelColl);

        modelDataset = Model.build("modelDataset", "model desc", EntityType.DATASET);
        modelDataset = modelService.createModel(modelDataset);

        pluginConf = getOracleConnectionConfiguration();
        pluginService.savePluginConfiguration(pluginConf);

        dataset1 = new Dataset(modelDataset, tenant, "labelDs1");
        dataset1.setLicence("licence");
        dataset1.setSipId("SipId1");
        dataset1.setDataSource(pluginConf);
        // DS1 -> (G1) (group 1)
        dataset1.setGroups(Sets.newHashSet("G1"));
        dataset2 = new Dataset(modelDataset, tenant, "labelDs2");
        dataset2.setLicence("licence");
        dataset2.setSipId("SipId2");
        dataset2.setDataSource(pluginConf);
        // DS2 -> (G2)
        dataset2.setGroups(Sets.newHashSet("G2"));
        dataset3 = new Dataset(modelDataset, tenant, "labelDs3");
        dataset3.setLicence("licence");
        dataset3.setSipId("SipId3");
        dataset3.setDataSource(pluginConf);
        // DS3 -> (G3)
        dataset3.setGroups(Sets.newHashSet("G3"));
        // No tags on Datasets, it doesn't matter

        coll1 = new Collection(modelColl, tenant, "coll1");
        coll1.setSipId("SipId4");
        // C1 -> (DS1, DS2)
        coll1.setTags(Sets.newHashSet(dataset1.getIpId().toString(), dataset2.getIpId().toString()));
        coll2 = new Collection(modelColl, tenant, "coll2");
        coll2.setSipId("SipId5");
        // C2 -> (C1, DS3)
        coll2.setTags(Sets.newHashSet(coll1.getIpId().toString(), dataset3.getIpId().toString()));
        coll3 = new Collection(modelColl, tenant, "coll3");
        coll3.setSipId("SipId6");
        // C3 -> (DS3)
        coll3.setTags(Sets.newHashSet(dataset3.getIpId().toString()));
    }

    @Test
    public void testCrawl() throws InterruptedException, ModuleException, IOException, PluginUtilsException {
        buildData1();
        coll1 = entityService.create(coll1);
        LOGGER.info("create coll1 (" + coll1.getIpId() + ")");
        coll2 = entityService.create(coll2);
        LOGGER.info("create coll2 (" + coll2.getIpId() + ")");
        coll3 = entityService.create(coll3);
        LOGGER.info("create coll3 (" + coll3.getIpId() + ")");

        dataset1 = entityService.create(dataset1);
        LOGGER.info("create dataset1 (" + dataset1.getIpId() + ")");
        dataset2 = entityService.create(dataset2);
        LOGGER.info("create dataset2 (" + dataset2.getIpId() + ")");
        dataset3 = entityService.create(dataset3);
        LOGGER.info("create dataset3 (" + dataset3.getIpId() + ")");

        // To be sure that the crawlerService daemon has time to do its job
        Thread.sleep(30_000);

        // Don't forget managing groups update others entities
        coll1 = (Collection) entityService.loadWithRelations(coll1.getIpId());
        coll2 = (Collection) entityService.loadWithRelations(coll2.getIpId());
        coll3 = (Collection) entityService.loadWithRelations(coll3.getIpId());
        dataset1 = (Dataset) entityService.loadWithRelations(dataset1.getIpId());
        dataset2 = (Dataset) entityService.loadWithRelations(dataset2.getIpId());
        dataset3 = (Dataset) entityService.loadWithRelations(dataset3.getIpId());

        Collection coll1Bis = esRepos.get(tenant, coll1);
        Assert.assertNotNull(coll1Bis);
        Assert.assertTrue(Beans.equals(coll1, coll1Bis));
        Collection coll2Bis = esRepos.get(tenant, coll2);
        Assert.assertNotNull(coll2Bis);
        Assert.assertTrue(Beans.equals(coll2, coll2Bis));
        Collection coll3Bis = esRepos.get(tenant, coll3);
        Assert.assertNotNull(coll3Bis);
        Assert.assertTrue(Beans.equals(coll3, coll3Bis));

        Dataset ds1Bis = esRepos.get(tenant, dataset1);
        Assert.assertNotNull(ds1Bis);
        Assert.assertTrue(Beans.equals(dataset1, ds1Bis));
        Dataset ds2Bis = esRepos.get(tenant, dataset2);
        Assert.assertNotNull(ds2Bis);
        Assert.assertTrue(Beans.equals(dataset2, ds2Bis));
        Dataset ds3Bis = esRepos.get(tenant, dataset3);
        Assert.assertNotNull(ds3Bis);
        Assert.assertTrue(Beans.equals(dataset3, ds3Bis));

        entityService.delete(coll1.getId());
        entityService.delete(dataset1.getId());

        // To be sure that the crawlerService daemon has time to do its job
        Thread.sleep(5000);

        esRepos.refresh(tenant);
        coll1Bis = esRepos.get(tenant, coll1);
        Assert.assertNull(coll1Bis);
        ds1Bis = esRepos.get(tenant, dataset1);
        Assert.assertNull(ds1Bis);
    }
}
