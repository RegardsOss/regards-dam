/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
public class CollectionServiceTest {

    private Model pModel1;

    private Model pModel2;

    private Collection collection1;

    private Collection collection2;

    private Collection collection3;

    private Collection collection4;

    private UniformResourceName collection2URN;

    private ICollectionRepository collectionRepositoryMocked;

    private ICollectionService collectionServiceMocked;

    private IAbstractEntityRepository<AbstractEntity> entitiesRepositoryMocked;

    /**
     * initialize the repo before each test
     *
     */
    @SuppressWarnings("unchecked")
    @Before
    public void init() {

        // populate the repository
        pModel1 = new Model();
        pModel1.setId(1L);
        pModel2 = new Model();
        pModel2.setId(2L);

        collection1 = new Collection(pModel1, "PROJECT", "collection1");
        collection1.setId(1L);
        collection2 = new Collection(pModel2, "PROJECT", "collection2");
        collection2.setId(2L);
        collection3 = new Collection(pModel2, "PROJECT", "collection3");
        collection3.setId(3L);
        collection4 = new Collection(pModel2, "PROJECT", "collection4");
        collection4.setId(4L);
        collection2URN = collection2.getIpId();
        Set<String> collection1Tags = collection1.getTags();
        collection1Tags.add(collection2URN.toString());
        Set<String> collection2Tags = collection2.getTags();
        collection2Tags.add(collection1.getIpId().toString());
        collection2.setTags(collection2Tags);

        // create a mock repository
        collectionRepositoryMocked = Mockito.mock(ICollectionRepository.class);
        Mockito.when(collectionRepositoryMocked.findOne(collection1.getId())).thenReturn(collection1);
        Mockito.when(collectionRepositoryMocked.findOne(collection2.getId())).thenReturn(collection2);
        Mockito.when(collectionRepositoryMocked.findOne(collection3.getId())).thenReturn(collection3);

        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        final List<AbstractEntity> findByTagsValueCol2IpId = new ArrayList<>();
        findByTagsValueCol2IpId.add(collection1);
        Mockito.when(entitiesRepositoryMocked.findByTags(collection2.getIpId().toString()))
                .thenReturn(findByTagsValueCol2IpId);
        Mockito.when(entitiesRepositoryMocked.findOne(collection1.getId())).thenReturn(collection1);
        Mockito.when(entitiesRepositoryMocked.findOne(collection2.getId())).thenReturn(collection2);
        Mockito.when(entitiesRepositoryMocked.findOne(collection3.getId())).thenReturn(collection3);

        IModelAttributeService pModelAttributeService = Mockito.mock(IModelAttributeService.class);
        IModelService pModelService = Mockito.mock(IModelService.class);
        IDeletedEntityRepository deletedEntityRepositoryMocked = Mockito.mock(IDeletedEntityRepository.class);
        collectionServiceMocked = new CollectionService(collectionRepositoryMocked, entitiesRepositoryMocked,
                storageServiceMocked, pModelAttributeService, pModelService, deletedEntityRepositoryMocked, null, null);

    }

    @Requirement("REGARDS_DSL_DAM_COL_010")
    @Purpose("Le système doit permettre de créer une collection à partir d’un modèle préalablement défini et d’archiver cette collection sous forme d’AIP dans le composant « Archival storage ».")
    @Test
    public void createCollection() throws ModuleException, IOException, PluginUtilsException {
        Mockito.when(entitiesRepositoryMocked.save(collection2)).thenReturn(collection2);
        final Collection collection = collectionServiceMocked.create(collection2);
        Assert.assertEquals(collection2, collection);
    }

}
