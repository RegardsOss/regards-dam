/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.*;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;

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

    private Dataset dataset;

    private Dataset dataset2;

    private Model model2;

    @SuppressWarnings("unchecked")
    @Before
    public void init() {

        // populate the repository
        model2 = new Model();
        model2.setId(2L);

        collection2 = new Collection(model2, "PROJECT", "collection2");
        collection2.setId(2L);
        collection2.setDescriptionFile(new DescriptionFile("pDescription2"));
        collection3 = new Collection(model2, "PROJECT", "collection3");
        collection3.setId(3L);
        collection3.setDescriptionFile(new DescriptionFile("pDescription3"));
        collection3.setLabel("pName3");
        collection4 = new Collection(model2, "PROJECT", "collection4");
        collection4.setId(4L);
        collection4.setDescriptionFile(new DescriptionFile("pDescription4"));
        Set<String> collection2Tags = collection2.getTags();
        collection2Tags.add(collection4.getIpId().toString());
        collection2.setTags(collection2Tags);

        data = new DataObject(null, "PROJECT", "object");
        data.setId(1L);
        doc = new Document(model2, "PROJECT", "doc");
        doc.setId(2L);
        dataset = new Dataset(model2, "PROJECT", "dataset");
        dataset.setLicence("licence");
        dataset.setId(3L);
        dataset.setDescriptionFile(new DescriptionFile("datasetDesc"));
        dataset.setLabel("dataset");
        dataset2 = new Dataset(model2, "PROJECT", "dataset2");
        dataset2.setLicence("licence");
        dataset2.setDescriptionFile(new DescriptionFile("datasetDesc2"));

        IModelAttrAssocService pModelAttributeService = Mockito.mock(IModelAttrAssocService.class);
        IModelService pModelService = Mockito.mock(IModelService.class);

        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        final List<AbstractEntity> findByTagsValueCol2IpId = new ArrayList<>();
        findByTagsValueCol2IpId.add(collection4);
        Mockito.when(entitiesRepositoryMocked.findByTags(collection2.getIpId().toString()))
                .thenReturn(findByTagsValueCol2IpId);

        EntityManager emMocked = Mockito.mock(EntityManager.class);

        IPublisher publisherMocked = Mockito.mock(IPublisher.class);
        IRuntimeTenantResolver runtimeTenantResolver=Mockito.mock(IRuntimeTenantResolver.class);
        Mockito.when(runtimeTenantResolver.getTenant()).thenReturn("Tenant");

        entityServiceMocked = new EntityService(pModelAttributeService, entitiesRepositoryMocked, pModelService, null,
                null, null, entitiesRepositoryMocked, emMocked, publisherMocked, runtimeTenantResolver);

        //        entityServiceMocked = new EntityService(pModelAttributeService, entitiesRepositoryMocked, pModelService, null,
        //                null, null, emMocked, publisherMocked);
        Mockito.when(entitiesRepositoryMocked.findById(1L)).thenReturn(data);
        Mockito.when(entitiesRepositoryMocked.findById(2L)).thenReturn(doc);
        Mockito.when(entitiesRepositoryMocked.findById(3L)).thenReturn(dataset);
    }

    /*
     * @Requirement("REGARDS_DSL_DAM_COL_050")
     *
     * @Purpose("Si une collection cible est associée à une collection source alors la collection source doit aussi être associée à la collection cible (navigation bidirectionnelle)."
     * )
     *
     * @Test public void testAssociateCollectionSourceToTarget() { final List<AbstractEntity> col3List = new
     * ArrayList<>(); col3List.add(collection3); final Set<UniformResourceName> col3URNList = new HashSet<>();
     * col3URNList.add(collection3.getIpId());
     * Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col3URNList)).thenReturn(col3List);
     *
     * // TODO // entityServiceMocked.associate(collection2, col3URNList);
     * Assert.assertTrue(collection3.getTags().contains(collection2.getIpId().toString())); }
     */

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
        Mockito.when(entitiesRepositoryMocked.save(data)).thenReturn(data);
        entityServiceMocked.associate(data.getId(), col3URNList);
        Assert.assertTrue(data.getTags().contains(collection3.getIpId().toString()));
    }

    @Requirement("REGARDS_DSL_DAM_CAT_310")
    @Purpose("Le système doit permettre d’ajouter un AIP de données dans un jeu de données à partir de son IP_ID (ajout d'un tag sur l'AIP de données).")
    @Test
    public void testAssociateDataToDatasetList() throws EntityNotFoundException {
        final List<AbstractEntity> datasetList = new ArrayList<>();
        datasetList.add(dataset);
        final Set<UniformResourceName> datasetURNList = new HashSet<>();
        datasetURNList.add(dataset.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(datasetURNList)).thenReturn(datasetList);
        Mockito.when(entitiesRepositoryMocked.findOneByIpId(data.getIpId())).thenReturn(data);
        Mockito.when(entitiesRepositoryMocked.save(data)).thenReturn(data);
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
        Mockito.when(entitiesRepositoryMocked.save(data)).thenReturn(data);
        entityServiceMocked.associate(data.getId(), col3URNList);
        Assert.assertTrue(data.getTags().contains(collection3.getIpId().toString()));
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_400")
    @Requirement("REGARDS_DSL_SYS_ARC_420")
    @Purpose("A document identifier is an URN")
    public void testAssociateDatasetToAnything() {
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
        // entityServiceMocked.associate(dataset, entityURNList);
        Assert.assertFalse(dataset.getTags().contains(collection3.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(dataset2.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(data.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(doc.getIpId().toString()));
    }

    /*
     * @Requirement("REGARDS_DSL_DAM_COL_230")
     *
     * @Purpose("Si la collection courante est dissociée d’une collection alors cette dernière doit aussi être dissociée de la collection courante (suppression de la navigation bidirectionnelle)."
     * )
     *
     * @Test public void testDissociate() { final List<AbstractEntity> col2List = new ArrayList<>();
     * col2List.add(collection2); final Set<UniformResourceName> col2URNList = new HashSet<>();
     * col2URNList.add(collection2.getIpId());
     * Mockito.when(entitiesRepositoryMocked.findByIpIdIn(col2URNList)).thenReturn(col2List);
     * entityServiceMocked.dissociate(collection3, col2URNList);
     * Assert.assertFalse(collection3.getTags().contains(collection2.getIpId().toString()));
     * Assert.assertFalse(collection2.getTags().contains(collection3.getIpId().toString())); }
     */

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_450")
    @Purpose("The URN identifier of a document is uniq")
    public void documentUrnUnicity() throws ModuleException, IOException {
        String docName = "un document";

        Document document1 = new Document(model2, "PROJECT", docName);
        Document document2 = new Document(model2, "PROJECT", docName);

        Assert.assertNotNull(document1);
        Assert.assertNotNull(document2);
        Assert.assertNotNull(document1.getIpId());
        Assert.assertNotNull(document2.getIpId());
        Assert.assertNotEquals(document1.getIpId(), document2.getIpId());
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_450")
    @Purpose("The URN identifier of a data object is uniq")
    public void dataUrnUnicity() throws ModuleException, IOException {
        String dataObjectName = "un document";

        DataObject document1 = new DataObject(model2, "PROJECT", dataObjectName);
        DataObject document2 = new DataObject(model2, "PROJECT", dataObjectName);

        Assert.assertNotNull(document1);
        Assert.assertNotNull(document2);
        Assert.assertNotNull(document1.getIpId());
        Assert.assertNotNull(document2.getIpId());
        Assert.assertNotEquals(document1.getIpId(), document2.getIpId());
    }

}