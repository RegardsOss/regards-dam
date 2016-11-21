/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.collections.service.CollectionsRequestService;
import fr.cnes.regards.modules.collections.service.ICollectionsRequestService;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
public class CollectionRequestServiceTest {

    private Model pModel1;

    private Model pModel2;

    private Collection collection1;

    private Collection collection2;

    private ICollectionRepository collectionRepositoryMocked;

    private ICollectionsRequestService collectionsRequestServiceMocked;

    /**
     * initialize the repo before each test
     *
     */
    @Before
    public void init() {
        // populate the repository
        pModel1 = new Model();
        pModel1.setId(1L);
        pModel2 = new Model();
        pModel2.setId(2L);
        collection1 = new Collection(1L, pModel1, "pDescription", "pName");
        collection2 = new Collection(2L, pModel2, "pDescription2", "pName2");
        // create a mock repository
        collectionRepositoryMocked = Mockito.mock(ICollectionRepository.class);
        collectionsRequestServiceMocked = new CollectionsRequestService(collectionRepositoryMocked);

    }

    @Test
    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections.")
    public void retrieveCollectionList() {
        final List<Collection> answer = new ArrayList<>(2);
        answer.add(collection1);
        answer.add(collection2);
        Mockito.when(collectionRepositoryMocked.findAll()).thenReturn(answer);
        final List<Collection> collections = collectionsRequestServiceMocked.retrieveCollectionList(null);
        Assert.assertEquals(2, collections.size());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve collections by model id.")
    public void retrieveCollectionListByModelId() {
        final List<Collection> answer = new ArrayList<>(1);
        answer.add(collection1);
        Mockito.when(collectionRepositoryMocked.findAllByModelId(pModel1.getId())).thenReturn(answer);
        final List<Collection> collections = collectionsRequestServiceMocked.retrieveCollectionList(pModel1.getId());
        Assert.assertEquals(1, collections.size());
        Assert.assertEquals(collection1.getId(), collections.get(0).getId());
        Assert.assertEquals(pModel1.getId(), collections.get(0).getModel().getId());
    }

    @Test
    public void retrieveCollectionById() {
        Mockito.when(collectionRepositoryMocked.findOne(collection2.getId())).thenReturn(collection2);
        final Collection collection = collectionsRequestServiceMocked.retrieveCollectionById(collection2.getId());

        Assert.assertEquals(collection.getId(), collection2.getId());
        Assert.assertEquals(collection.getModel().getId(), pModel2.getId());
    }

    @Test
    public void updateCollection() {
        Mockito.when(collectionRepositoryMocked.findOne(collection1.getId())).thenReturn(collection1);
        Mockito.when(collectionRepositoryMocked.save(collection1)).thenReturn(collection1);
        try {
            collectionsRequestServiceMocked.updateCollection(collection1, collection1.getId());
        } catch (final EntityInconsistentIdentifierException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void updateCollectionWithWrongURL() throws EntityInconsistentIdentifierException {
        Mockito.when(collectionRepositoryMocked.findOne(collection2.getId())).thenReturn(collection2);
        collectionsRequestServiceMocked.updateCollection(collection1, collection2.getId());
    }

    @Test
    public void deleteCollection() {
        collectionsRequestServiceMocked.deleteCollection(collection2.getId());
        Mockito.verify(collectionRepositoryMocked).delete(collection2.getId());
    }

    @Test
    public void createCollection() {
        Mockito.when(collectionRepositoryMocked.save(collection2)).thenReturn(collection2);
        final Collection collection = collectionsRequestServiceMocked.createCollection(collection2);
        Mockito.verify(collectionRepositoryMocked).save(collection2);
        Assert.assertEquals(collection, collection2);
    }

}
