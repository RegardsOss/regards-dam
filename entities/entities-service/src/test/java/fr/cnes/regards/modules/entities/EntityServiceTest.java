/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.Tag;
import fr.cnes.regards.modules.entities.service.EntityService;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelAttributeService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class EntityServiceTest {

    private EntityService entityServiceMocked;

    private IAbstractEntityRepository<AbstractEntity> entitiesRepositoryMocked;

    private Collection collection2;

    private Collection collection3;

    private Collection collection4;

    private DataObject data;

    private Document doc;

    private DataSet dataset;

    private DataSet dataset2;

    @Before
    public void init() {

        JWTService jwtService = new JWTService();
        jwtService.injectMockToken("Tenant", "PUBLIC");
        // populate the repository
        Model pModel2 = new Model();
        pModel2.setId(2L);

        collection2 = new Collection(pModel2,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, "PUBLIC", UUID.randomUUID(), 1),
                null);
        collection2.setId(2L);
        collection2.setDescription("pDescription2");
        collection3 = new Collection(pModel2,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, "PUBLIC", UUID.randomUUID(), 1),
                "pName3");
        collection3.setId(3L);
        collection3.setDescription("pDescription3");
        collection4 = new Collection(pModel2,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, "PUBLIC", UUID.randomUUID(), 1),
                "pName4");
        collection4.setId(4L);
        collection4.setDescription("pDescription4");
        Set<Tag> collection2Tags = collection2.getTags();
        collection2Tags.add(new Tag(collection4.getIpId().toString()));
        collection2.setTags(collection2Tags);

        data = new DataObject();
        data.setIpId(new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, "PUBLIC", UUID.randomUUID(), 1));
        doc = new Document(pModel2,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.DOCUMENT, "PUBLIC", UUID.randomUUID(), 1), null);
        dataset = new DataSet(pModel2,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.DATASET, "PUBLIC", UUID.randomUUID(), 1),
                "dataset");
        dataset.setDescription("datasetDesc");
        dataset2 = new DataSet(pModel2,
                new UniformResourceName(OAISIdentifier.AIP, EntityType.DATASET, "PUBLIC", UUID.randomUUID(), 1),
                "dataset2");
        dataset2.setDescription("datasetDesc2");

        IModelAttributeService pModelAttributeService = Mockito.mock(IModelAttributeService.class);

        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        final List<AbstractEntity> findByTagsValueCol2IpId = new ArrayList<>();
        findByTagsValueCol2IpId.add(collection4);
        Mockito.when(entitiesRepositoryMocked.findByTagsValue(collection2.getIpId().toString()))
                .thenReturn(findByTagsValueCol2IpId);

        entityServiceMocked = new EntityService(pModelAttributeService, entitiesRepositoryMocked);
    }

    @Requirement("REGARDS_DSL_DAM_COL_040")
    @Purpose("Le système doit permettre d’associer une collection à d’autres collections.")
    @Test
    public void testAssociateCollectionToList() {
        final List<AbstractEntity> col3List = new ArrayList<>();
        col3List.add(collection3);
        final Set<UniformResourceName> col3URNList = new HashSet<>();
        col3URNList.add(collection3.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col3URNList)).thenReturn(col3List);

        entityServiceMocked.associate(collection2, col3URNList);
        Assert.assertTrue(collection2.getTags().contains(new Tag(collection3.getIpId().toString())));
    }

    @Test
    public void testAssociateCollectionToListData() {
        final List<AbstractEntity> dataList = new ArrayList<>();
        dataList.add(data);
        final Set<UniformResourceName> dataURNList = new HashSet<>();
        dataURNList.add(data.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(dataURNList)).thenReturn(dataList);
        entityServiceMocked.associate(collection2, dataURNList);
        Assert.assertTrue(collection2.getTags().contains(new Tag(data.getIpId().toString())));
    }

    @Test
    public void testAssociateCollectionToListDocument() {
        final List<AbstractEntity> docList = new ArrayList<>();
        docList.add(doc);
        final Set<UniformResourceName> docURNList = new HashSet<>();
        docURNList.add(doc.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(docURNList)).thenReturn(docList);
        entityServiceMocked.associate(collection2, docURNList);
        Assert.assertFalse(collection2.getTags().contains(new Tag(doc.getIpId().toString())));
    }

    @Requirement("REGARDS_DSL_DAM_COL_050")
    @Purpose("Si une collection cible est associée à une collection source alors la collection source doit aussi être associée à la collection cible (navigation bidirectionnelle).")
    @Test
    public void testAssociateCollectionSourceToTarget() {
        final List<AbstractEntity> col3List = new ArrayList<>();
        col3List.add(collection3);
        final Set<UniformResourceName> col3URNList = new HashSet<>();
        col3URNList.add(collection3.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col3URNList)).thenReturn(col3List);
        entityServiceMocked.associate(collection2, col3URNList);
        Assert.assertTrue(collection3.getTags().contains(new Tag(collection2.getIpId().toString())));
    }

    @Requirement("REGARDS_DSL_DAM_CAT_450")
    @Purpose("Le système doit permettre d’ajouter un tag de type « collection » sur un ou plusieurs AIP de type « data » à partir d’une liste d’IP_ID.")
    @Test
    public void testAssociateDataToCollectionList() {
        final List<AbstractEntity> col3List = new ArrayList<>();
        col3List.add(collection3);
        final Set<UniformResourceName> col3URNList = new HashSet<>();
        col3URNList.add(collection3.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col3URNList)).thenReturn(col3List);
        entityServiceMocked.associate(data, col3URNList);
        Assert.assertTrue(data.getTags().contains(new Tag(collection3.getIpId().toString())));
    }

    @Requirement("REGARDS_DSL_DAM_CAT_310")
    @Purpose("Le système doit permettre d’ajouter un AIP de données dans un jeu de données à partir de son IP_ID(ajout d'un tag sur l'AIP de données).")
    @Test
    public void testAssociateDataToDataSetList() {
        final List<AbstractEntity> datasetList = new ArrayList<>();
        datasetList.add(dataset);
        final Set<UniformResourceName> datasetURNList = new HashSet<>();
        datasetURNList.add(dataset.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(datasetURNList)).thenReturn(datasetList);
        entityServiceMocked.associate(data, datasetURNList);
        Assert.assertTrue(data.getTags().contains(new Tag(dataset.getIpId().toString())));
    }

    @Requirement("REGARDS_DSL_DAM_CAT_050")
    @Purpose("Le système doit permettre d’associer un document à une ou plusieurs collections.")
    @Test
    public void testAssociateDocToCollectionList() {
        final List<AbstractEntity> col3List = new ArrayList<>();
        col3List.add(collection3);
        final Set<UniformResourceName> col3URNList = new HashSet<>();
        col3URNList.add(collection3.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col3URNList)).thenReturn(col3List);
        entityServiceMocked.associate(data, col3URNList);
        Assert.assertTrue(data.getTags().contains(new Tag(collection3.getIpId().toString())));
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
        entityServiceMocked.associate(dataset, entityURNList);
        Assert.assertFalse(dataset.getTags().contains(new Tag(collection3.getIpId().toString())));
        Assert.assertFalse(dataset.getTags().contains(new Tag(dataset2.getIpId().toString())));
        Assert.assertFalse(dataset.getTags().contains(new Tag(data.getIpId().toString())));
        Assert.assertFalse(dataset.getTags().contains(new Tag(doc.getIpId().toString())));
    }

    @Requirement("REGARDS_DSL_DAM_COL_230")
    @Purpose("Si la collection courante est dissociée d’une collection alors cette dernière doit aussi être dissociée de la collection courante (suppression de la navigation bidirectionnelle).")
    @Test
    public void testDissociate() {
        final List<AbstractEntity> col2List = new ArrayList<>();
        col2List.add(collection2);
        final Set<UniformResourceName> col2URNList = new HashSet<>();
        col2URNList.add(collection2.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col2URNList)).thenReturn(col2List);
        entityServiceMocked.dissociate(collection3, col2URNList);
        Assert.assertFalse(collection3.getTags().contains(new Tag(collection2.getIpId().toString())));
        Assert.assertFalse(collection2.getTags().contains(new Tag(collection3.getIpId().toString())));
    }

}