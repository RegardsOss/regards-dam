/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.collections.service.CollectionsRequestService;
import fr.cnes.regards.modules.collections.service.ICollectionsRequestService;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.storage.service.IStorageService;

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

    private IStorageService storageServiceMocked;

    private IAbstractEntityRepository entitiesRepositoryMocked;

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
        storageServiceMocked = Mockito.mock(IStorageService.class);
        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        Mockito.when(storageServiceMocked.persist(collection1)).thenReturn(collection1);
        Mockito.when(storageServiceMocked.persist(collection2)).thenReturn(collection2);
        collectionsRequestServiceMocked = new CollectionsRequestService(collectionRepositoryMocked,
                entitiesRepositoryMocked, storageServiceMocked);

    }

    @Test
    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections.")
    public void retrieveAllCollectionList() {
        final List<Collection> answer = new ArrayList<>(2);
        answer.add(collection1);
        answer.add(collection2);
        Mockito.when(collectionRepositoryMocked.findAll()).thenReturn(answer);
        final List<Collection> collections = collectionsRequestServiceMocked.retrieveCollectionList();
        Assert.assertEquals(2, collections.size());
    }

    @Requirement("REGARDS_DSL_DAM_COL_310")
    @Test
    public void retrieveCollectionById() {
        Mockito.when(collectionRepositoryMocked.findOne(collection2.getId())).thenReturn(collection2);
        final Collection collection = collectionsRequestServiceMocked.retrieveCollectionById(collection2.getId());

        Assert.assertEquals(collection.getId(), collection2.getId());
        Assert.assertEquals(collection.getModel().getId(), pModel2.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_210")
    @Purpose("Le système doit permettre de mettre à jour les valeurs d’une collection via son IP_ID et d’archiver ces modifications dans son AIP au niveau du composant « Archival storage » si ce composant est déployé.")
    @Test
    public void updateCollection() {
        final Collection updatedCollection1 = collection1;
        updatedCollection1.setDescription("Updated Description");

        Mockito.when(collectionRepositoryMocked.findOne(collection1.getId())).thenReturn(collection1);
        Mockito.when(collectionRepositoryMocked.save(updatedCollection1)).thenReturn(updatedCollection1);
        try {
            final Collection result = collectionsRequestServiceMocked.updateCollection(updatedCollection1,
                                                                                       collection1.getId());
            Assert.assertEquals(updatedCollection1, result);
        } catch (final EntityInconsistentIdentifierException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Requirement("REGARDS_DSL_DAM_COL_220")
    @Purpose("Le système doit permettre d’associer/dissocier des collections à la collection courante lors de la mise à jour.")
    @Ignore
    public void testFullUpdate() {

    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void updateCollectionWithWrongURL() throws EntityInconsistentIdentifierException {
        Mockito.when(collectionRepositoryMocked.findOne(collection2.getId())).thenReturn(collection2);
        collectionsRequestServiceMocked.updateCollection(collection1, collection2.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_120")
    @Purpose("Si la suppression d’une collection est demandée, le système doit au préalable supprimer le tag correspondant de tout autre AIP (dissociation complète).")
    @Test
    public void deleteCollection() {
        collectionsRequestServiceMocked.deleteCollection(collection2.getId());
        // TODO: check that others got affected and tag is gone
        Mockito.verify(collectionRepositoryMocked).delete(collection2.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_010")
    @Purpose("Le système doit permettre de créer une collection à partir d’un modèle préalablement défini et d’archiver cette collection sous forme d’AIP dans le composant « Archival storage ».")
    @Test
    public void createCollection() {
        Mockito.when(collectionRepositoryMocked.save(collection2)).thenReturn(collection2);
        final Collection collection = collectionsRequestServiceMocked.createCollection(collection2);
        Mockito.verify(collectionRepositoryMocked).save(collection2);
        Assert.assertEquals(collection, collection2);
    }

    @Requirement("REGARDS_DSL_DAM_COL_230")
    @Purpose("Si la collection courante est dissociée d’une collection alors cette dernière doit aussi être dissociée de la collection courante (suppression de la navigation bidirectionnelle).")
    public void testDissociate() {

    }

    @Requirement("REGARDS_DSL_DAM_COL_420")
    @Purpose("Le système doit permettre de manière synchrone de rechercher des collections à partir de mots-clés (recherche full text).")
    public void testRetrieveByKeyWord() {
    }

    @Requirement("REGARDS_DSL_DAM_COL_040")
    @Purpose("Le système doit permettre d’associer une collection à d’autres collections.")
    public void testAssociateToList() {

    }

    @Requirement("REGARDS_DSL_DAM_COL_050")
    @Purpose("Si une collection cible est associée à une collection source alors la collection source doit aussi être associée à la collection cible (navigation bidirectionnelle).")
    public void testAssociateSourceToTarget() {
    }

}
