/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.modules.datasources.service.DataSourceService;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.models.service.exception.ImportException;
import fr.cnes.regards.modules.models.service.xml.XmlImportHelper;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class DatasetServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceTest.class);

    private Model pModel1;

    private Model pModel2;

    private Model modelOfObjects;

    private AttributeModel attString;

    private AttributeModel attBoolean;

    private AttributeModel GEO_CRS;

    private AttributeModel GEO_GEOMETRY;

    private AttributeModel Contact_Phone;

    private Dataset dataSet1;

    private Dataset dataSet2;

    private UniformResourceName dataSet2URN;

    private IDatasetRepository dataSetRepositoryMocked;

    private DatasetService dataSetServiceMocked;

    private IAbstractEntityRepository<AbstractEntity> entitiesRepositoryMocked;

    private DataSourceService dataSourceServiceMocked;

    private IModelAttrAssocService pModelAttributeService;

    private IAttributeModelService pAttributeModelService;

    private ArrayList<Long> pluginConfigurationIds;

    private IModelService modelService;

    /**
     * initialize the repo before each test
     *
     * @throws ModuleException
     *
     */
    @SuppressWarnings("unchecked")
    @Before
    public void init() throws ModuleException {
        JWTService jwtService = new JWTService();
        jwtService.injectMockToken("Tenant", "PUBLIC");
        dataSetRepositoryMocked = Mockito.mock(IDatasetRepository.class);
        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        pModelAttributeService = Mockito.mock(IModelAttrAssocService.class);
        modelService = Mockito.mock(IModelService.class);
        pAttributeModelService = Mockito.mock(IAttributeModelService.class);
        dataSourceServiceMocked = Mockito.mock(DataSourceService.class);
        // populate the repository
        pModel1 = new Model();
        pModel1.setId(1L);
        pModel2 = new Model();
        pModel2.setId(2L);

        dataSet1 = new Dataset(pModel1, "PROJECT", "dataSet1");
        dataSet1.setLicence("licence");
        dataSet1.setId(1L);
        dataSet2 = new Dataset(pModel2, "PROJECT", "dataSet2");
        dataSet2.setLicence("licence");
        setModelInPlace(importModel("sample-model-minimal.xml"));
        Mockito.when(modelService.getModel(modelOfObjects.getId())).thenReturn(modelOfObjects);
        dataSet2.setDataModel(modelOfObjects.getId());
        dataSet2.setSubsettingClause(getValidClause());
        dataSet2.setId(2L);

        dataSet2URN = dataSet2.getIpId();
        Set<String> dataSet1Tags = dataSet1.getTags();
        dataSet1Tags.add(dataSet2URN.toString());
        Set<String> dataSet2Tags = dataSet2.getTags();
        dataSet2Tags.add(dataSet1.getIpId().toString());
        dataSet2.setTags(dataSet2Tags);

        // create a mock repository
        Mockito.when(dataSetRepositoryMocked.findOne(dataSet1.getId())).thenReturn(dataSet1);
        Mockito.when(dataSetRepositoryMocked.findOne(dataSet2.getId())).thenReturn(dataSet2);

        final List<AbstractEntity> findByTagsValueCol2IpId = new ArrayList<>();
        findByTagsValueCol2IpId.add(dataSet1);
        Mockito.when(entitiesRepositoryMocked.findByTags(dataSet2.getIpId().toString()))
                .thenReturn(findByTagsValueCol2IpId);
        Mockito.when(entitiesRepositoryMocked.findOne(dataSet1.getId())).thenReturn(dataSet1);
        Mockito.when(entitiesRepositoryMocked.findOne(dataSet2.getId())).thenReturn(dataSet2);

        IDeletedEntityRepository deletedEntityRepositoryMocked = Mockito.mock(IDeletedEntityRepository.class);
        IPublisher publisherMocked = Mockito.mock(IPublisher.class);

        dataSetServiceMocked = new DatasetService(dataSetRepositoryMocked, pAttributeModelService,
                pModelAttributeService, dataSourceServiceMocked, entitiesRepositoryMocked, modelService,
                deletedEntityRepositoryMocked, null, null, publisherMocked);

    }

    /**
     * @param pImportModel
     */
    private void setModelInPlace(List<ModelAttrAssoc> pImportModel) {
        modelOfObjects = pImportModel.get(0).getModel();
        modelOfObjects.setId(3L);
        ModelAttrAssoc attStringModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals(Fragment.getDefaultName())
                        && ma.getAttribute().getName().equals("att_string"))
                .findAny().get();
        attString = attStringModelAtt.getAttribute();
        attString.setId(1L);
        Mockito.when(pAttributeModelService.findByNameAndFragmentName(attString.getName(), null)).thenReturn(attString);
        Mockito.when(pModelAttributeService.getModelAttrAssoc(modelOfObjects.getId(), attString))
                .thenReturn(attStringModelAtt);
        ModelAttrAssoc attBooleanModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals(Fragment.getDefaultName())
                        && ma.getAttribute().getName().equals("att_boolean"))
                .findAny().get();
        attBoolean = attBooleanModelAtt.getAttribute();
        attBoolean.setId(2L);
        Mockito.when(pAttributeModelService.findByNameAndFragmentName(attBoolean.getName(), null))
                .thenReturn(attBoolean);
        Mockito.when(pModelAttributeService.getModelAttrAssoc(modelOfObjects.getId(), attBoolean))
                .thenReturn(attBooleanModelAtt);
        ModelAttrAssoc CRS_CRSModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals("GEO")
                        && ma.getAttribute().getName().equals("CRS"))
                .findAny().get();
        GEO_CRS = CRS_CRSModelAtt.getAttribute();
        GEO_CRS.setId(3L);
        Mockito.when(pAttributeModelService
                .findByNameAndFragmentName(GEO_CRS.getName(), GEO_CRS.getFragment().getName())).thenReturn(GEO_CRS);
        Mockito.when(pModelAttributeService.getModelAttrAssoc(modelOfObjects.getId(), GEO_CRS))
                .thenReturn(CRS_CRSModelAtt);
        ModelAttrAssoc CRS_GEOMETRYModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals("GEO")
                        && ma.getAttribute().getName().equals("GEOMETRY"))
                .findAny().get();
        GEO_GEOMETRY = CRS_GEOMETRYModelAtt.getAttribute();
        GEO_GEOMETRY.setId(4L);
        Mockito.when(pAttributeModelService.findByNameAndFragmentName(GEO_GEOMETRY.getName(),
                                                                      GEO_GEOMETRY.getFragment().getName()))
                .thenReturn(GEO_GEOMETRY);
        Mockito.when(pModelAttributeService.getModelAttrAssoc(modelOfObjects.getId(), GEO_GEOMETRY))
                .thenReturn(CRS_GEOMETRYModelAtt);
        ModelAttrAssoc Contact_PhoneModelAtt = pImportModel.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals("Contact")
                        && ma.getAttribute().getName().equals("Phone"))
                .findAny().get();
        Contact_Phone = Contact_PhoneModelAtt.getAttribute();
        Contact_Phone.setId(5L);
        Mockito.when(pAttributeModelService.findByNameAndFragmentName(Contact_Phone.getName(),
                                                                      Contact_Phone.getFragment().getName()))
                .thenReturn(Contact_Phone);
        Mockito.when(pModelAttributeService.getModelAttrAssoc(modelOfObjects.getId(), Contact_Phone))
                .thenReturn(Contact_PhoneModelAtt);
    }

    /**
     * @return
     */
    private ICriterion getValidClause() {
        // textAtt contains "testContains"
        ICriterion containsCrit = ICriterion.contains("attributes." + attString.getName(), "testContains");
        // textAtt ends with "testEndsWith"
        ICriterion endsWithCrit = ICriterion
                .endsWith("attributes." + GEO_CRS.getFragment().getName() + "." + GEO_CRS.getName(), "testEndsWith");
        // textAtt strictly equals "testEquals"
        ICriterion equalsCrit = ICriterion
                .eq("attributes." + Contact_Phone.getFragment().getName() + "." + Contact_Phone.getName(),
                        "testEquals");

        ICriterion booleanCrit = new BooleanMatchCriterion("attributes." + attBoolean.getName(), true);

        // All theses criterions (AND)
        ICriterion rootCrit = ICriterion.and(containsCrit, endsWithCrit, equalsCrit, booleanCrit);
        return rootCrit;
    }

    @Purpose("Le système doit permettre de créer une dataSet à partir d’un modèle préalablement défini et d’archiver cette dataSet sous forme d’AIP dans le composant « Archival storage ».")
    @Test
    public void createDataset() throws ModuleException, IOException, PluginUtilsException {
        Mockito.when(dataSetRepositoryMocked.save(dataSet2)).thenReturn(dataSet2);
        final Dataset dataSet = dataSetServiceMocked.create(dataSet2, null);
        Assert.assertEquals(dataSet2, dataSet);
    }

    private List<ModelAttrAssoc> importModel(String pFilename) {
        InputStream input;
        try {
            input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));

            return XmlImportHelper.importModel(input);
        } catch (IOException | ImportException e) {
            LOG.debug("import of model failed", e);
            Assert.fail();
        }
        // never reached because test will fail first
        return null;
    }

}
