/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fr.cnes.regards.microservices.core.test.report.annotation.Purpose;
import fr.cnes.regards.microservices.core.test.report.annotation.Requirement;
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

    private ICollectionsRequestService collectionsRequestService_;

    private Model pModel1;

    private Model pModel2_;

    private Collection collection1_;

    private Collection collection2_;

    @Before
    public void init() throws AlreadyExistingException {
        // use a stub repository, to be able to only test the service
        ICollectionRepository collectionRepository = new CollectionRepositoryStub();
        collectionsRequestService_ = new CollectionsRequestService(collectionRepository);
        // populate the repository
        pModel1 = new Model();
        pModel2_ = new Model();
        collection1_ = new Collection("pSid_id", pModel1, "pDescription", "pName");
        collection2_ = new Collection("pSid_id2", pModel2_, "pDescription2", "pName2");
        collectionRepository.save(collection1_);
        collectionRepository.save(collection2_);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections.")
    public void retrieveCollectionList() {
        List<Collection> collections = collectionsRequestService_.retrieveCollectionList();
        assertEquals(collections.size(), 2);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve collections by model id.")
    public void retrieveCollectionListByModelId() {
        List<Collection> collections = collectionsRequestService_.retrieveCollectionListByModelId(pModel1.getId());
        assertEquals(collections.size(), 1);
        assertEquals(collections.get(0).getId(), collection1_.getId());
        assertEquals(collections.get(0).getModel().getId(), pModel1.getId());
    }
}
