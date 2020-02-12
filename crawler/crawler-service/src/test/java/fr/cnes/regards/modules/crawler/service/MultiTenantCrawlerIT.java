package fr.cnes.regards.modules.crawler.service;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.crawler.test.MultitenantConfiguration;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.service.entities.ICollectionService;
import fr.cnes.regards.modules.indexer.dao.EsRepository;
import fr.cnes.regards.modules.model.dao.IModelRepository;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactoryEventHandler;
import fr.cnes.regards.modules.model.service.IModelService;

/**
 * Multitenant crawler test
 *
 * @author oroussel
 */
@Ignore("not testable nor in local nor on jenkins")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { MultitenantConfiguration.class })
public class MultiTenantCrawlerIT {

    private static final String TENANT1 = "MICKEY";

    private static final String TENANT2 = "DONALD";

    @Value("${spring.application.name:}")
    private String toTestPropertiesExists;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactoryEventHandler gsonAttributeFactoryHandler;

    @Autowired
    private IModelService modelService;

    @Autowired
    private ICollectionService collService;

    @Autowired
    private IModelRepository modelRepos;

    @Autowired
    private ICollectionRepository collRepos;

    @Autowired
    private IDatasetRepository datasetRepos;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private ICrawlerAndIngesterService crawlerService;

    @Autowired
    private EsRepository esRepos;

    @BeforeClass
    public static void toBeOrNotToBe() throws URISyntaxException {
        Assume.assumeTrue(ClassLoader
                .getSystemResource("multitenant_" + System.getProperty("user.name") + ".properties") != null);
    }

    @Before
    public void init() {
        // Simulate spring boot ApplicationStarted event to start mapping for each tenants.
        gsonAttributeFactoryHandler.onApplicationEvent(null);

        if (esRepos.indexExists(TENANT1)) {
            esRepos.deleteAll(TENANT1);
        }
        if (esRepos.indexExists(TENANT2)) {
            esRepos.deleteAll(TENANT2);
        }

        tenantResolver.forceTenant(TENANT1);
        datasetRepos.deleteAll();
        collRepos.deleteAll();
        datasetRepos.deleteAll();
        modelRepos.deleteAll();

        crawlerService.setConsumeOnlyMode(false);

        tenantResolver.forceTenant(TENANT2);
        collRepos.deleteAll();
        modelRepos.deleteAll();
    }

    @Test
    public void test() throws ModuleException, IOException, InterruptedException {
        tenantResolver.forceTenant(TENANT1);
        Model model1 = Model.build("model1_" + TENANT1, "modele pour tenant " + TENANT1, EntityType.COLLECTION);
        model1 = modelService.createModel(model1);

        final Collection coll11 = new Collection(model1, TENANT1, "COL11", "collection 1 pour tenant " + TENANT1);
        coll11.setGroups(Sets.newHashSet("Michou", "Jojo"));
        collService.create(coll11);
        final Collection coll12 = new Collection(model1, TENANT1, "COL12", "collection 2 pour tenant " + TENANT1);
        collService.create(coll12);

        coll11.setTags(Sets.newHashSet(coll12.getIpId().toString()));
        collService.update(coll11);

        tenantResolver.forceTenant(TENANT2);
        Model model2 = Model.build("model2_" + TENANT2, "modele pour tenant " + TENANT2, EntityType.COLLECTION);
        model2 = modelService.createModel(model2);

        final Collection coll21 = new Collection(model2, TENANT2, "COL21", "collection 1 pour tenant " + TENANT2);
        coll21.setGroups(Sets.newHashSet("Riri", "Fifi", "Loulou"));
        collService.create(coll21);

        final Collection coll22 = new Collection(model2, TENANT2, "COL22", "collection 2 pour tenant " + TENANT2);
        collService.create(coll22);

        coll21.setTags(Sets.newHashSet(coll22.getIpId().toString()));
        collService.update(coll21);

        Thread.sleep(10_000);
        esRepos.refresh(TENANT1);
        esRepos.refresh(TENANT2);

        final Collection coll12FromEs = esRepos.get(TENANT1, coll12);
        // coll11 tags coll12 so coll11 groups must have been copied to coll12
        Assert.assertArrayEquals(Sets.newTreeSet(coll11.getGroups()).toArray(),
                                 Sets.newTreeSet(coll12FromEs.getGroups()).toArray());

        final Collection coll22FromEs = esRepos.get(TENANT2, coll22);
        // coll21 tags coll22 so coll21 groups must have been copied to coll22
        Assert.assertArrayEquals(Sets.newTreeSet(coll21.getGroups()).toArray(),
                                 Sets.newTreeSet(coll22FromEs.getGroups()).toArray());
    }
}
