/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AbstractEntityServiceTest {

    private AbstractEntityService entityServiceMocked;

    private IAbstractEntityRepository<AbstractEntity> entitiesRepositoryMocked;

    private Collection collection2;

    private Collection collection3;

    private Collection collection4;

    private DataObject data;

    private Document doc;

    private DataSet dataset;

    private DataSet dataset2;

    @SuppressWarnings("unchecked")
    @Before
    public void init() {

        // populate the repository
        Model pModel2 = new Model();
        pModel2.setId(2L);

        collection2 = new Collection(pModel2, "PROJECT", "collection2");
        collection2.setId(2L);
        collection2.setDescription("pDescription2");
        collection3 = new Collection(pModel2, "PROJECT", "collection3");
        collection3.setId(3L);
        collection3.setDescription("pDescription3");
        collection3.setLabel("pName3");
        collection4 = new Collection(pModel2, "PROJECT", "collection4");
        collection4.setId(4L);
        collection4.setDescription("pDescription4");
        Set<String> collection2Tags = collection2.getTags();
        collection2Tags.add(collection4.getIpId().toString());
        collection2.setTags(collection2Tags);

        data = new DataObject(null, "PROJECT", "objectc");
        data.setId(1L);
        doc = new Document(pModel2, "PROJECT", "doc");
        doc.setId(2L);
        dataset = new DataSet(pModel2, "PROJECT", "dataset");
        dataset.setId(3L);
        dataset.setDescription("datasetDesc");
        dataset.setLabel("dataset");
        dataset2 = new DataSet(pModel2, "PROJECT", "dataset2");
        dataset2.setDescription("datasetDesc2");

        IModelAttributeService pModelAttributeService = Mockito.mock(IModelAttributeService.class);
        IModelService pModelService = Mockito.mock(IModelService.class);

        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        final List<AbstractEntity> findByTagsValueCol2IpId = new ArrayList<>();
        findByTagsValueCol2IpId.add(collection4);
        Mockito.when(entitiesRepositoryMocked.findByTags(collection2.getIpId().toString()))
                .thenReturn(findByTagsValueCol2IpId);

        EntityManager emMocked = Mockito.mock(EntityManager.class);

        entityServiceMocked = new AbstractEntityService(pModelAttributeService, entitiesRepositoryMocked, pModelService,
                null, null, null, emMocked) {

            @Override
            protected Logger getLogger() {
                return null;
            }

            @Override
            protected <T extends AbstractEntity> T beforeUpdate(T pEntity) {
                return pEntity;
            }

            @Override
            protected <T extends AbstractEntity> T beforeCreate(T pNewEntity) throws ModuleException {
                return pNewEntity;
            }

            @Override
            protected <T extends AbstractEntity> T doCheck(T pEntity) throws ModuleException {
                return pEntity;
            }
        };
        Mockito.when(entitiesRepositoryMocked.findById(1L)).thenReturn(data);
        Mockito.when(entitiesRepositoryMocked.findById(2L)).thenReturn(doc);
        Mockito.when(entitiesRepositoryMocked.findById(3L)).thenReturn(dataset);
    }

    @Test
    public void testAssociateCollectionToListData() throws EntityNotFoundException {
        final List<AbstractEntity> dataList = new ArrayList<>();
        dataList.add(data);
        final Set<UniformResourceName> dataURNList = new HashSet<>();
        dataURNList.add(data.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(dataURNList)).thenReturn(dataList);
        Mockito.when(entitiesRepositoryMocked.findOneByIpId(collection2.getIpId())).thenReturn(collection2);
        Mockito.when(entitiesRepositoryMocked.findById(collection2.getId())).thenReturn(collection2);

        entityServiceMocked.associate(collection2.getId(), dataURNList);
        Assert.assertTrue(collection2.getTags().contains(data.getIpId().toString()));
    }

    @Test
    public void testAssociateCollectionToListDocument() {
        final List<AbstractEntity> docList = new ArrayList<>();
        docList.add(doc);
        final Set<UniformResourceName> docURNList = new HashSet<>();
        docURNList.add(doc.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(docURNList)).thenReturn(docList);
        // TODO
        //        entityServiceMocked.associate(collection2, docURNList);
        Assert.assertFalse(collection2.getTags().contains(doc.getIpId().toString()));
    }

    /*    @Requirement("REGARDS_DSL_DAM_COL_050")
    @Purpose("Si une collection cible est associée à une collection source alors la collection source doit aussi être associée à la collection cible (navigation bidirectionnelle).")
    @Test
    public void testAssociateCollectionSourceToTarget() {
        final List<AbstractEntity> col3List = new ArrayList<>();
        col3List.add(collection3);
        final Set<UniformResourceName> col3URNList = new HashSet<>();
        col3URNList.add(collection3.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col3URNList)).thenReturn(col3List);
    
        // TODO
        //        entityServiceMocked.associate(collection2, col3URNList);
        Assert.assertTrue(collection3.getTags().contains(collection2.getIpId().toString()));
    }*/

    @Requirement("REGARDS_DSL_DAM_CAT_450")
    @Purpose("Le système doit permettre d’ajouter un tag de type « collection » sur un ou plusieurs AIP de type « data » à partir d’une liste d’IP_ID.")
    @Test
    public void testAssociateDataToCollectionList() throws EntityNotFoundException {
        final List<AbstractEntity> col3List = new ArrayList<>();
        col3List.add(collection3);
        final Set<UniformResourceName> col3URNList = new HashSet<>();
        col3URNList.add(collection3.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col3URNList)).thenReturn(col3List);
        Mockito.when(entitiesRepositoryMocked.findOneByIpId(data.getIpId())).thenReturn(data);
        // TODO
        entityServiceMocked.associate(data.getId(), col3URNList);
        Assert.assertTrue(data.getTags().contains(collection3.getIpId().toString()));
    }

    @Requirement("REGARDS_DSL_DAM_CAT_310")
    @Purpose("Le système doit permettre d’ajouter un AIP de données dans un jeu de données à partir de son IP_ID(ajout d'un tag sur l'AIP de données).")
    @Test
    public void testAssociateDataToDataSetList() throws EntityNotFoundException {
        final List<AbstractEntity> datasetList = new ArrayList<>();
        datasetList.add(dataset);
        final Set<UniformResourceName> datasetURNList = new HashSet<>();
        datasetURNList.add(dataset.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(datasetURNList)).thenReturn(datasetList);
        Mockito.when(entitiesRepositoryMocked.findOneByIpId(data.getIpId())).thenReturn(data);
        entityServiceMocked.associate(data.getId(), datasetURNList);
        Assert.assertTrue(data.getTags().contains(dataset.getIpId().toString()));
    }

    @Requirement("REGARDS_DSL_DAM_CAT_050")
    @Purpose("Le système doit permettre d’associer un document à une ou plusieurs collections.")
    @Test
    public void testAssociateDocToCollectionList() throws EntityNotFoundException {
        final List<AbstractEntity> col3List = new ArrayList<>();
        col3List.add(collection3);
        final Set<UniformResourceName> col3URNList = new HashSet<>();
        col3URNList.add(collection3.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col3URNList)).thenReturn(col3List);
        Mockito.when(entitiesRepositoryMocked.findById(data.getId())).thenReturn(data);
        Mockito.when(entitiesRepositoryMocked.findOneByIpId(data.getIpId())).thenReturn(data);

        entityServiceMocked.associate(data.getId(), col3URNList);
        Assert.assertTrue(data.getTags().contains(collection3.getIpId().toString()));
    }

    @Test
    public void testAssociateDataSetToAnything() {
        final List<AbstractEntity> entityList = new ArrayList<>();
        entityList.add(collection3);
        entityList.add(dataset2);
        entityList.add(data);
        entityList.add(doc);
        final Set<UniformResourceName> entityURNList = new HashSet<>();
        entityURNList.add(collection3.getIpId());
        entityURNList.add(dataset2.getIpId());
        entityURNList.add(data.getIpId());
        entityURNList.add(doc.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(entityURNList)).thenReturn(entityList);

        // TODO
        //        entityServiceMocked.associate(dataset, entityURNList);
        Assert.assertFalse(dataset.getTags().contains(collection3.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(dataset2.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(data.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(doc.getIpId().toString()));
    }

    /*    @Requirement("REGARDS_DSL_DAM_COL_230")
    @Purpose("Si la collection courante est dissociée d’une collection alors cette dernière doit aussi être dissociée de la collection courante (suppression de la navigation bidirectionnelle).")
    @Test
    public void testDissociate() {
        final List<AbstractEntity> col2List = new ArrayList<>();
        col2List.add(collection2);
        final Set<UniformResourceName> col2URNList = new HashSet<>();
        col2URNList.add(collection2.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col2URNList)).thenReturn(col2List);
        entityServiceMocked.dissociate(collection3, col2URNList);
        Assert.assertFalse(collection3.getTags().contains(collection2.getIpId().toString()));
        Assert.assertFalse(collection2.getTags().contains(collection3.getIpId().toString()));
    }*/

}