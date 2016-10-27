/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service.test;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.dao.stubs.CollectionRepositoryStub;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.collections.service.CollectionsRequestService;
import fr.cnes.regards.modules.collections.service.ICollectionsRequestService;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 *
 */
public class CollectionRequestServiceTest {

    private ICollectionsRequestService collectionsRequestService;

    private Model pModel1;

    private Model pModel2;

    private Collection collection1;

    private Collection collection2;

    private ICollectionRepository collectionRepositoryMocked;

    private ICollectionsRequestService collectionsRequestServiceMocked;

    @Before
    public void init() throws AlreadyExistingException {
        // use a stub repository, to be able to only test the service
        final ICollectionRepository collectionRepository = new CollectionRepositoryStub();
        collectionsRequestService = new CollectionsRequestService(collectionRepository);
        // populate the repository
        pModel1 = new Model();
        pModel2 = new Model();
        collection1 = new Collection(pModel1, "pDescription", "pName");
        collection2 = new Collection(pModel2, "pDescription2", "pName2");
        collectionRepository.save(collection1);
        collectionRepository.save(collection2);

        // create a mock repository
        collectionRepositoryMocked = Mockito.mock(ICollectionRepository.class);
        collectionsRequestServiceMocked = new CollectionsRequestService(collectionRepositoryMocked);

    }

    @Test
    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections.")
    public void retrieveCollectionList() {
        final List<Collection> collections = collectionsRequestService.retrieveCollectionList();
        Assert.assertEquals(collections.size(), 2);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve collections by model id.")
    public void retrieveCollectionListByModelId() {
        final List<Collection> collections = collectionsRequestService.retrieveCollectionListByModelId(pModel1.getId());
        Assert.assertEquals(collections.size(), 1);
        Assert.assertEquals(collections.get(0).getId(), collection1.getId());
        Assert.assertEquals(collections.get(0).getModel().getId(), pModel1.getId());
    }

    @Test
    public void retrieveCollectionById() {
        final Collection collection = collectionsRequestService.retrieveCollectionById(collection2.getId());

        Assert.assertEquals(collection.getId(), collection2.getId());
        Assert.assertEquals(collection.getModel().getId(), pModel2.getId());
    }

    @Test
    public void updateCollection() {
        Mockito.when(collectionRepositoryMocked.findOne(collection1.getId())).thenReturn(collection1);
        Mockito.when(collectionRepositoryMocked.save(collection1)).thenReturn(collection1);
        try {
            collectionsRequestServiceMocked.updateCollection(collection1, collection1.getId());
        } catch (final OperationNotSupportedException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = OperationNotSupportedException.class)
    public void updateCollectionWithWrongURL() throws OperationNotSupportedException {
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
