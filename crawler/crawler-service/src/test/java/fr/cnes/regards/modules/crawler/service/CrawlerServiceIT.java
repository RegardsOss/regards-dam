package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.util.Beans;
import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.IEntityService;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class CrawlerServiceIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceIT.class);

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

    @Autowired
    private IModelService modelService;

    @Autowired
    @Qualifier(value = "entityService")
    private IEntityService entityService;

    @Autowired
    private IEsRepository esRepos;

    private static interface ConsumerWithException<T> {

        void accept(T t) throws Exception;
    }

    private <T> void execute(ConsumerWithException<T> consumer, T arg) {
        try {
            consumer.accept(arg);
        } catch (Throwable t) {
            //            t.printStackTrace();
        }
    }

    @After
    public void clean() {
        execute(entityService::delete, dataset1.getId());
        execute(entityService::delete, dataset2.getId());
        execute(entityService::delete, dataset3.getId());
        execute(entityService::delete, coll1.getId());
        execute(entityService::delete, coll2.getId());
        execute(entityService::delete, coll3.getId());

        execute(modelService::deleteModel, modelColl.getId());
        execute(modelService::deleteModel, modelDataset.getId());

        esRepos.deleteIndex(tenant);
    }

    public void buildData1() throws ModuleException {
        modelColl = Model.build("modelColl", "model desc", EntityType.COLLECTION);
        modelColl = modelService.createModel(modelColl);

        modelDataset = Model.build("modelDataset", "model desc", EntityType.DATASET);
        modelDataset = modelService.createModel(modelDataset);

        dataset1 = new Dataset(modelDataset, tenant, "labelDs1");
        dataset1.setLicence("licence");
        dataset1.setSipId("SipId1");
        // DS1 -> (G1) (group 1)
        dataset1.setGroups(Sets.newHashSet("G1"));
        dataset2 = new Dataset(modelDataset, tenant, "labelDs2");
        dataset2.setLicence("licence");
        dataset2.setSipId("SipId2");
        // DS2 -> (G2)
        dataset2.setGroups(Sets.newHashSet("G2"));
        dataset3 = new Dataset(modelDataset, tenant, "labelDs3");
        dataset3.setLicence("licence");
        dataset3.setSipId("SipId3");
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
    public void testCrawl() throws InterruptedException, ModuleException, IOException {
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
        Thread.sleep(30000);

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
        Thread.sleep(10000);

        esRepos.refresh(tenant);
        coll1Bis = esRepos.get(tenant, coll1);
        Assert.assertNull(coll1Bis);
        ds1Bis = esRepos.get(tenant, dataset1);
        Assert.assertNull(ds1Bis);
    }
}
