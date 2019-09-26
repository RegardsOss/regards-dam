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
package fr.cnes.regards.modules.dam.service.entities;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.model.dao.IModelRepository;
import fr.cnes.regards.modules.model.domain.Model;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=cdgroups",
        "regards.dam.post.aip.entities.to.storage=false" }, locations = "classpath:es.properties")
@MultitenantTransactional
public class CollectionDatasetGroupsIT extends AbstractMultitenantServiceTest {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(CollectionDatasetGroupsIT.class);

    private Model modelColl;

    private Model modelDataset;

    private Dataset dataset1;

    private Dataset dataset2;

    private Dataset dataset3;

    private Collection coll1;

    private Collection coll2;

    private Collection coll3;

    private Collection coll4;

    @Autowired
    private ICollectionService collService;

    @Autowired
    private ICollectionRepository collRepository;

    @Autowired
    private IDatasetService dataSetService;

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity<?>> entityRepos;

    @Autowired
    private IModelRepository modelRepository;

    @Before
    public void setUp() throws Exception {
        collRepository.deleteAll();
        datasetRepository.deleteAll();
        entityRepos.deleteAll();
        modelRepository.deleteAll();
    }

    @After
    public void tearDown() throws Exception {
    }

    // (G1, G2, G3) (G3)
    // C2 C3
    // /\ |
    // / \ |
    // / \ |
    // v \ |
    // (G1, G2) C1 \ |
    // /\ \ |
    // / \ \ |
    // / \ \ |
    // / G1 \ G2 \|G3
    // v v v
    // DS1 DS2 DS3
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

        modelDataset = Model.build("modelDataset", "model desc", EntityType.DATASET);
        modelDataset = modelRepository.save(modelDataset);

        dataset1 = new Dataset(modelDataset, "PROJECT", "ProviderId1", "labelDs1");
        dataset1.setLicence("licence");
        // DS1 -> (G1) (group 1)
        dataset1.setGroups(Sets.newHashSet("G1"));
        dataset2 = new Dataset(modelDataset, "PROJECT", "ProviderId2", "labelDs2");
        dataset2.setLicence("licence");
        // DS2 -> (G2)
        dataset2.setGroups(Sets.newHashSet("G2"));
        dataset3 = new Dataset(modelDataset, "PROJECT", "ProviderId3", "labelDs3");
        dataset3.setLicence("licence");
        // DS3 -> (G3)
        dataset3.setGroups(Sets.newHashSet("G3"));
        // No tags on Datasets, it doesn't matter

        coll1 = new Collection(modelColl, "PROJECT", "ProviderId4", "coll1");

        // DS1 -> C1
        dataset1.setTags(Sets.newHashSet(coll1.getIpId().toString()));

        coll2 = new Collection(modelColl, "PROJECT", "ProviderId5", "coll2");
        // DS2 -> C1
        dataset2.setTags(Sets.newHashSet(coll1.getIpId().toString()));
        // C1 -> C2
        coll1.addTags(coll2.getIpId().toString());

        coll3 = new Collection(modelColl, "PROJECT", "ProviderId6", "coll3");
        // DS3 -> (C2, C3)
        dataset3.setTags(Sets.newHashSet(coll2.getIpId().toString(), coll3.getIpId().toString()));
    }

    @Requirement("REGARDS_DSL_DAM_COL_310")
    @Test
    public void testCollectionsFirst() throws ModuleException, IOException {
        buildData1();

        // First create collections
        coll1 = collService.create(coll1);
        coll2 = collService.create(coll2);
        coll3 = collService.create(coll3);

        // then datasets => groups must have been updated on collections
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        coll1 = collService.load(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll1.getGroups());
        coll2 = collService.load(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll2.getGroups());
        coll3 = collService.load(coll3.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());

        // Delete DS3 => C3 (), C2 (G1, G2)
        dataSetService.delete(dataset3.getId());

        coll1 = collService.load(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll1.getGroups());
        coll2 = collService.load(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll2.getGroups());
        coll3 = collService.load(coll3.getId());
        Assert.assertTrue(coll3.getGroups().isEmpty());

    }

    @Test
    public void testDatasetsFirst() throws ModuleException, IOException {
        buildData1();
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

        // Add C4: C2 -> C4 -> C1
        // C4 => (G1, G2, G3)
        // C1 => (G1, G2, G3)
        coll4 = new Collection(modelColl, "PROJECT", "ProviderId7", "coll4");
        coll4.setTags(Sets.newHashSet(coll1.getIpId().toString()));
        coll2.addTags(coll4.getIpId().toString());

        coll4 = collService.create(coll4);
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll4.getGroups());
        coll1 = collService.load(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll1.getGroups());

        // Delete C1 => C2 (G3), C4 (G3)
        collService.delete(coll1.getId());

        coll4 = collService.load(coll4.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll4.getGroups());
        coll2 = collService.load(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll2.getGroups());
        coll3 = collService.load(coll3.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());
    }

    @Test
    public void testLoop() throws ModuleException, IOException {
        buildData1();
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

        // Add C4: C2 -> C4 -> C1
        // C4 => (G1, G2, G3)
        // C1 => (G1, G2, G3)
        coll4 = new Collection(modelColl, "PROJECT", "ProviderId7", "coll4");
        coll4.setTags(Sets.newHashSet(coll1.getIpId().toString()));
        coll2.addTags(coll4.getIpId().toString());

        coll4 = collService.create(coll4);
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll4.getGroups());
        coll1 = collService.load(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll1.getGroups());

        // Delete DS2 => C1 (G1), C2 (G1, G3)
        dataSetService.delete(dataset2.getId());

        coll4 = collService.load(coll4.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G3"), coll4.getGroups());
        coll2 = collService.load(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G3"), coll2.getGroups());
        coll3 = collService.load(coll3.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());
        coll1 = collService.load(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G3"), coll1.getGroups());
    }

    @Requirement("REGARDS_DSL_DAM_CAT_050")
    @Purpose("Le système doit permettre d’associer un document à une ou plusieurs collections.")
    @Test
    public void testAssociateDissociate() throws ModuleException, IOException {
        buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1); // C1 tags DS1 and DS2 => (G1, G2)
        coll2 = collService.create(coll2); // C2 tags DS3 and C1 => (G1, G2, G3)
        coll3 = collService.create(coll3); // C3 tags DS3 => (G3)

        // Dissociate all datasets and their tags to collections
        dataSetService.dissociate(dataset1.getId(), Sets.newHashSet(coll1.getIpId().toString()));
        dataSetService.dissociate(dataset2.getId(), Sets.newHashSet(coll1.getIpId().toString()));
        dataSetService.dissociate(dataset3.getId(),
                                  Sets.newHashSet(coll2.getIpId().toString(), coll3.getIpId().toString()));

        coll1 = collService.load(coll1.getId());
        Assert.assertTrue(coll1.getGroups().isEmpty());
        coll2 = collService.load(coll2.getId());
        Assert.assertTrue(coll2.getGroups().isEmpty());
        coll3 = collService.load(coll3.getId());
        Assert.assertTrue(coll3.getGroups().isEmpty());

        // Re-associate all datasets and their tags to collections
        dataSetService.associate(dataset1.getId(), Sets.newHashSet(coll1.getIpId().toString()));
        dataSetService.associate(dataset2.getId(), Sets.newHashSet(coll1.getIpId().toString()));
        dataSetService.associate(dataset3.getId(),
                                 Sets.newHashSet(coll2.getIpId().toString(), coll3.getIpId().toString()));

        coll1 = collService.load(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2"), coll1.getGroups());
        coll2 = collService.load(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll2.getGroups());
        coll3 = collService.load(coll3.getId());
        Assert.assertEquals(Sets.newHashSet("G3"), coll3.getGroups());

        // Add C4: C2 -> C4
        // C4 (G1, G2, G3)
        coll4 = new Collection(modelColl, "PROJECT", "ProviderId7", "coll4");
        coll4 = collService.create(coll4);

        collService.associate(coll2.getId(), Sets.newHashSet(coll4.getIpId().toString()));

        coll4 = collService.load(coll4.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3"), coll4.getGroups());

        // lets test when we add group G4 to DS1 so we can check it has been propagated to C1(direct tag), C2(indirect
        // tag through DS1->C1->C2), C4 (indirect tag through DS1->C1->C2->C4)
        // because the tests are transactional we need to create a new object so hibernate doesn't see the changes and
        // the logic is respected
        final Dataset dataset1Updated = new Dataset(modelDataset, "PROJECT", "DS1", "labelDs1");
        dataset1Updated.setGroups(Sets.newHashSet("G1"));
        dataset1Updated.setDataModel(dataset1.getDataModel());
        dataset1Updated.setCreationDate(dataset1.getCreationDate());
        dataset1Updated.setDataSource(dataset1.getDataSource());
        dataset1Updated.setLicence(dataset1.getLicence());
        dataset1Updated.setMetadata(dataset1.getMetadata());
        dataset1Updated.setOpenSearchSubsettingClause(dataset1.getOpenSearchSubsettingClause());
        dataset1Updated.setNormalizedGeometry(dataset1.getNormalizedGeometry());
        dataset1Updated.setId(dataset1.getId());
        dataset1Updated.setIpId(dataset1.getIpId());
        dataset1Updated.setLabel(dataset1.getLabel());
        dataset1Updated.setLastUpdate(dataset1.getLastUpdate());
        dataset1Updated.setModel(dataset1.getModel());
        dataset1Updated.setProperties(dataset1.getProperties());
        dataset1Updated.setProviderId(dataset1.getProviderId());
        dataset1Updated.setTags(Sets.newHashSet(coll1.getIpId().toString()));
        dataset1Updated.getGroups().add("G4");
        dataset1 = dataSetService.update(dataset1Updated);
        // we now should have:
        // DS1 => (G1, G4)
        // C1 => DS1 & DS2 => (G1, G2, G4)
        // C2 => DS3 & C1 => (G1, G2, G3, G4)
        // C4 => C2 => (G1, G2, G3, G4)
        Assert.assertTrue(dataset1.getGroups().contains("G4"));
        coll1 = collService.load(coll1.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G4"), coll1.getGroups());
        coll2 = collService.load(coll2.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3", "G4"), coll2.getGroups());
        coll4 = collService.load(coll4.getId());
        Assert.assertEquals(Sets.newHashSet("G1", "G2", "G3", "G4"), coll4.getGroups());
    }

    @Requirement("REGARDS_DSL_DAM_COL_220")
    @Requirement("REGARDS_DSL_DAM_COL_040")
    @Purpose("Le système doit permettre d’associer/dissocier des collections à la collection courante lors de la mise à jour."
            + "Le système doit permettre de mettre à jour les valeurs d’une collection via son IP_ID et d’archiver ces "
            + "modifications dans son AIP au niveau du composant « Archival storage » si ce composant est déployé.")
    @Requirement("REGARDS_DSL_DAM_COL_210")
    @Test
    public void testUpdate() throws ModuleException, IOException {
        buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1); // C1 tags DS1 and DS2 => (G1, G2)
        coll2 = collService.create(coll2); // C2 tags DS3 and C1 => (G1, G2, G3)
        coll3 = collService.create(coll3); // C3 tags DS3 => (G3)

        // Dissociate "by hand"
        coll1.clearTags();
        coll2.clearTags();
        coll3.clearTags();

        dataset1.clearTags();
        dataset2.clearTags();
        dataset3.clearTags();

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

        // Associate "by hand" C1 -> (C3, DS1)
        coll1.addTags(coll3.getIpId().toString());
        coll1.addTags(dataset1.getIpId().toString());
        coll1 = collService.update(coll1.getId(), coll1);
        Assert.assertTrue(coll1.getTags().contains(coll3.getIpId().toString()));
        Assert.assertTrue(coll1.getTags().contains(dataset1.getIpId().toString()));

        // Associate "by hands" DS1 -> (C1, DS2)
        dataset1.addTags(coll1.getIpId().toString());
        dataset1.addTags(dataset2.getIpId().toString());
        Assert.assertTrue(dataset1.getTags().contains(coll1.getIpId().toString()));
        Assert.assertTrue(dataset1.getTags().contains(dataset2.getIpId().toString()));
    }

    @Requirement("REGARDS_DSL_DAM_COL_120")
    @Purpose("Si la suppression d’une collection est demandée, le système doit au préalable supprimer le tag correspondant de tout autre AIP (dissociation complète).")
    @Test
    public void testDelete() throws ModuleException, IOException {
        buildData1();
        // First create datasets
        dataset1 = dataSetService.create(dataset1);
        dataset2 = dataSetService.create(dataset2);
        dataset3 = dataSetService.create(dataset3);

        // Then collections => groups must have been updated on collections
        coll2 = collService.create(coll2); // DS3 tags C2 => C2 (G1, G2, G3)
        coll3 = collService.create(coll3); // DS3 tags C3 => C3 (G3)
        coll1.addTags(coll3.getIpId().toString()); // Add C1 -> C3
        coll1 = collService.create(coll1); // DS1 and DS2 tag C1 => C1 (G1, G2)

        // C1 -> C2 and C1 -> C3
        collService.delete(coll2.getId());
        collService.delete(coll3.getId());

        coll1 = collService.load(coll1.getId());
        Assert.assertFalse(coll1.getTags().contains(coll2.getIpId().toString()));
        Assert.assertFalse(coll1.getTags().contains(coll3.getIpId().toString()));

    }

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections.")
    @Test
    public void testFindAll() throws ModuleException, IOException {
        buildData1();

        // Then collections => groups must have been updated on collections
        coll1 = collService.create(coll1);
        coll2 = collService.create(coll2);
        coll3 = collService.create(coll3);

        final List<Collection> collections = collService.findAll();
        Assert.assertEquals(3, collections.size());
        Assert.assertTrue(collections.contains(coll1));
        Assert.assertTrue(collections.contains(coll2));
        Assert.assertTrue(collections.contains(coll3));
    }

    //    @Test(expected = EntityInconsistentIdentifierException.class)
    //    public void updateEntityWithWrongId() throws ModuleException, IOException {
    //        buildData1();
    //        // First create datasets
    //        dataset1 = dataSetService.create(dataset1);
    //        dataset2 = dataSetService.create(dataset2);
    //
    //        dataSetService.update(dataset1.getId(), dataset2);
    //    }

}
