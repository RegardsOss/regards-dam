package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

@TestPropertySource(locations = { "classpath:test.properties" })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ServiceConfiguration.class)
@Transactional
public class CollectionDataSetGroupsIT {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionDataSetGroupsIT.class);

    private Model modelColl;

    private Model modelDataSet;

    private DataSet dataset1;

    private DataSet dataset2;

    private DataSet dataset3;

    private Collection coll1;

    private Collection coll2;

    private Collection coll3;

    private Collection coll4;

    private Document doc1;

    private DataObject dataObj1;

    @Autowired
    private ICollectionService collService;

    @Autowired
    private IDatasetService dataSetService;

    @Autowired
    private IModelRepository modelRepository;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

    //      (G1, G2, G3)    (G3)
    //             C2        C3
    //             /\        |
    //            /  \       |
    //           /    \      |
    //           v     \     |
    // (G1, G2) C1      \    |
    //          /\       \   |
    //         /  \       \  |
    //        /    \       \ |
    //       / G1   \ G2    \|G3
    //       v       v       v
    //      DS1    DS2     DS3
    //
    // DS1 (G1)
    // DS2 (G2)
    // DS3 (G3)
    // C1 -> DS1, C1 -> DS2 => C1 (G1, G2)
    // C3 -> DS3 => C3 (G3)
    // C2 -> C1, C2 -> DS3 => C2 (G1, G2, G3)
    public void buildData1() {
        modelColl = Model.build("modelColl", "model desc", EntityType.COLLECTION);
        modelColl = modelRepository.save(modelColl);

        modelDataSet = Model.build("modelDataSet", "model desc", EntityType.DATASET);
        modelDataSet = modelRepository.save(modelDataSet);

        dataset1 = new DataSet(modelDataSet, "PROJECT", "labelDs1");
        dataset1.setSipId("SipId1");
        // DS1 -> (G1) (group 1)
        dataset1.setGroups(Sets.newHashSet("G1"));
        dataset2 = new DataSet(modelDataSet, "PROJECT", "labelDs2");
        dataset2.setSipId("SipId2");
        // DS2 -> (G2)
        dataset2.setGroups(Sets.newHashSet("G2"));
        dataset3 = new DataSet(modelDataSet, "PROJECT", "labelDs3");
        dataset3.setSipId("SipId3");
        // DS3 -> (G3)
        dataset3.setGroups(Sets.newHashSet("G3"));
        // No tags on DataSets, it doesn't matter

        coll1 = new Collection(modelColl, "PROJECT", "coll1");
        coll1.setSipId("SipId4");
        // C1 -> (DS1, DS2)
        coll1.setTags(Sets.newHashSet(dataset1.getIpId().toString(), dataset2.getIpId().toString()));
        coll2 = new Collection(modelColl, "PROJECT", "coll2");
        coll2.setSipId("SipId5");
        // C2 -> (C1, DS3)
        coll2.setTags(Sets.newHashSet(coll1.getIpId().toString(), dataset3.getIpId().toString()));
        coll3 = new Collection(modelColl, "PROJECT", "coll3");
        coll3.setSipId("SipId6");
        // C3 -> (DS3)
        coll3.setTags(Sets.newHashSet(dataset3.getIpId().toString()));
    }

    @Requirement("REGARDS_DSL_DAM_COL_310")
    @Test
    public void testCollectionsFirst() throws ModuleException, IOException {
        this.buildData1();
        // First create collections
        coll1 = collService.create(coll1);
        coll2 = collService.create(coll2);
        coll3 = collService.create(coll3);

        // then datasets => groups must have been updated on collections
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        coll1 = collService.retrieveCollectionById(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll1.getGroups());
        coll2 = collService.retrieveCollectionById(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll2.getGroups());
        coll3 = collService.retrieveCollectionById(coll3.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());

        // Delete DS3 => C3 (), C2 (G1, G2)
        dataSetService.delete(dataset3.getId());

        coll1 = collService.retrieveCollectionById(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll1.getGroups());
        coll2 = collService.retrieveCollectionById(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll2.getGroups());
        coll3 = collService.retrieveCollectionById(coll3.getId());
        Assert.assertTrue(coll3.getGroups().isEmpty());

    }

    @Test
    public void testDatasetsFirst() throws ModuleException, IOException {
        this.buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1); // C1 tags DS1 and DS2 => (G1, G2)
        coll2 = collService.create(coll2); // C2 tags DS3 and C1 => (G1, G2, G3)
        coll3 = collService.create(coll3); // C3 tags DS3 => (G3)

        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll1.getGroups());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll2.getGroups());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());

        // Add C4: C1 -> C4 -> C2
        // C4 => (G1, G2, G3)
        // C1 => (G1, G2, G3)
        coll4 = new Collection(modelColl, "PROJECT", "coll4");
        coll4.setSipId("SipId7");
        coll4.setTags(Sets.newHashSet(coll2.getIpId().toString()));
        coll1.getTags().add(coll4.getIpId().toString());

        coll4 = collService.create(coll4);
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll4.getGroups());
        coll1 = collService.retrieveCollectionById(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll1.getGroups());

        // Delete C1 => C2 (G3), C4 (G3)
        collService.delete(coll1.getId());

        coll4 = collService.retrieveCollectionById(coll4.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll4.getGroups());
        coll2 = collService.retrieveCollectionById(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll2.getGroups());
        coll3 = collService.retrieveCollectionById(coll3.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());
    }

    @Test
    public void testLoop() throws ModuleException, IOException {
        this.buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1); // C1 tags DS1 and DS2 => (G1, G2)
        coll2 = collService.create(coll2); // C2 tags DS3 and C1 => (G1, G2, G3)
        coll3 = collService.create(coll3); // C3 tags DS3 => (G3)

        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll1.getGroups());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll2.getGroups());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());

        // Add C4: C1 -> C4 -> C2
        // C4 => (G1, G2, G3)
        // C1 => (G1, G2, G3)
        coll4 = new Collection(modelColl, "PROJECT", "coll4");
        coll4.setSipId("SipId7");
        coll4.setTags(Sets.newHashSet(coll2.getIpId().toString()));
        coll1.getTags().add(coll4.getIpId().toString());

        coll4 = collService.create(coll4);
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll4.getGroups());
        coll1 = collService.retrieveCollectionById(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll1.getGroups());

        // Delete DS2 => C1 (G1), C2 (G1, G3)
        dataSetService.delete(dataset2.getId());

        coll4 = collService.retrieveCollectionById(coll4.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G3"), coll4.getGroups());
        coll2 = collService.retrieveCollectionById(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G3"), coll2.getGroups());
        coll3 = collService.retrieveCollectionById(coll3.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());
        coll1 = collService.retrieveCollectionById(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G3"), coll1.getGroups());
    }

    @Test
    public void testAssociateDissociate() throws ModuleException, IOException {
        this.buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1); // C1 tags DS1 and DS2 => (G1, G2)
        coll2 = collService.create(coll2); // C2 tags DS3 and C1 => (G1, G2, G3)
        coll3 = collService.create(coll3); // C3 tags DS3 => (G3)

        // Dissociate all collections and their tags to datasets
        collService.dissociate(coll1.getId(), Sets.newHashSet(dataset1.getIpId(), dataset2.getIpId()));
        collService.dissociate(coll2.getId(), Sets.newHashSet(dataset3.getIpId()));
        collService.dissociate(coll3.getId(), Sets.newHashSet(dataset3.getIpId()));

        coll1 = collService.retrieveCollectionById(coll1.getId());
        Assert.assertTrue(coll1.getGroups().isEmpty());
        coll2 = collService.retrieveCollectionById(coll2.getId());
        Assert.assertTrue(coll2.getGroups().isEmpty());
        coll3 = collService.retrieveCollectionById(coll3.getId());
        Assert.assertTrue(coll3.getGroups().isEmpty());

        // Re-Associate all collections and their tags to datasets
        collService.associate(coll1.getId(), Sets.newHashSet(dataset1.getIpId(), dataset2.getIpId()));
        collService.associate(coll2.getId(), Sets.newHashSet(dataset3.getIpId()));
        collService.associate(coll3.getId(), Sets.newHashSet(dataset3.getIpId()));

        coll1 = collService.retrieveCollectionById(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll1.getGroups());
        coll2 = collService.retrieveCollectionById(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll2.getGroups());
        coll3 = collService.retrieveCollectionById(coll3.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());

        coll4 = new Collection(modelColl, "PROJECT", "coll4");
        coll4.setSipId("SipId7");
        coll4 = collService.create(coll4);

        collService.associate(coll4.getId(), Sets.newHashSet(coll2.getIpId()));

        coll4 = collService.retrieveCollectionById(coll4.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll4.getGroups());
    }

    @Requirement("REGARDS_DSL_DAM_COL_220")
    @Requirement("REGARDS_DSL_DAM_COL_040")
    @Purpose("Le système doit permettre d’associer/dissocier des collections à la collection courante lors de la mise à jour."
            + "Le système doit permettre de mettre à jour les valeurs d’une collection via son IP_ID et d’archiver ces "
            + "modifications dans son AIP au niveau du composant « Archival storage » si ce composant est déployé.")
    @Requirement("REGARDS_DSL_DAM_COL_210")
    @Test
    public void testUpdate() throws ModuleException, IOException {
        this.buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1); // C1 tags DS1 and DS2 => (G1, G2)
        coll2 = collService.create(coll2); // C2 tags DS3 and C1 => (G1, G2, G3)
        coll3 = collService.create(coll3); // C3 tags DS3 => (G3)

        // Dissociate "by hand"
        coll1.getTags().clear();
        coll2.getTags().clear();
        coll3.getTags().clear();

        dataset1.getTags().clear();
        dataset2.getTags().clear();
        dataset3.getTags().clear();

        coll1 = collService.update(coll1);
        Assert.assertTrue(coll1.getTags().isEmpty());
        coll2 = collService.update(coll2);
        Assert.assertTrue(coll2.getTags().isEmpty());
        coll3 = collService.update(coll3.getIpId(), coll3);
        Assert.assertTrue(coll3.getTags().isEmpty());

        dataset1 = dataSetService.update(dataset1);
        Assert.assertTrue(dataset1.getTags().isEmpty());
        dataset2 = dataSetService.update(dataset2);
        Assert.assertTrue(dataset2.getTags().isEmpty());
        dataset3 = dataSetService.update(dataset3);
        Assert.assertTrue(dataset3.getTags().isEmpty());

        // Associate "by hand"
        coll1.getTags().add(coll3.getIpId().toString());
        coll1.getTags().add(dataset1.getIpId().toString());
        coll1 = collService.update(coll1.getId(), coll1);
        Assert.assertTrue(coll1.getTags().contains(coll3.getIpId().toString()));
        Assert.assertTrue(coll1.getTags().contains(dataset1.getIpId().toString()));

        dataset1.getTags().add(coll1.getIpId().toString());
        dataset1.getTags().add(dataset2.getIpId().toString());
        Assert.assertTrue(dataset1.getTags().contains(coll1.getIpId().toString()));
        Assert.assertTrue(dataset1.getTags().contains(dataset2.getIpId().toString()));
    }

    @Requirement("REGARDS_DSL_DAM_COL_120")
    @Purpose("Si la suppression d’une collection est demandée, le système doit au préalable supprimer le tag correspondant de tout autre AIP (dissociation complète).")
    @Test
    public void testDelete() throws ModuleException, IOException {
        this.buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1); // C1 tags DS1 and DS2 => (G1, G2)
        coll2 = collService.create(coll2); // C2 tags DS3 and C1 => (G1, G2, G3)
        coll3.getTags().add(coll1.getIpId().toString()); // Add C3 -> C1
        coll3 = collService.create(coll3); // C3 tags DS3 => (G3)

        // C2 -> C1 and C3 -> C1
        collService.delete(coll1.getId());

        coll2 = collService.retrieveCollectionById(coll2.getId());
        Assert.assertFalse(coll2.getTags().contains(coll1.getIpId().toString()));
        coll3 = collService.retrieveCollectionById(coll3.getId());
        Assert.assertFalse(coll3.getTags().contains(coll1.getIpId().toString()));

    }

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections.")
    @Test
    public void testFindAll() throws ModuleException, IOException {
        this.buildData1();

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1); // C1 tags DS1 and DS2 => (G1, G2)
        coll2 = collService.create(coll2); // C2 tags DS3 and C1 => (G1, G2, G3)
        coll3 = collService.create(coll3); // C3 tags DS3 => (G3)

        List<Collection> collections = collService.retrieveCollectionList();
        Assert.assertEquals(3, collections.size());
        Assert.assertTrue(collections.contains(coll1));
        Assert.assertTrue(collections.contains(coll2));
        Assert.assertTrue(collections.contains(coll3));
    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void updateEntityWithWrongId() throws ModuleException, IOException {
        this.buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);

        dataSetService.update(dataset1.getId(), dataset2);
    }
}
