/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRequestRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDeletedEntityRepository;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.dam.service.models.exception.ImportException;
import fr.cnes.regards.modules.dam.service.models.xml.XmlImportHelper;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;

/**
 * @author Sylvain Vissiere-Guerinet
 */
public class DatasetServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceTest.class);

    private Model model1;

    private Model model2;

    private Model modelOfObjects;

    private AttributeModel attString;

    private AttributeModel attBoolean;

    private AttributeModel GEO_CRS;

    private AttributeModel Contact_Phone;

    private Dataset dataSet1;

    private Dataset dataSet2;

    private IDatasetRepository dataSetRepositoryMocked;

    private DatasetService dataSetServiceMocked;

    private IAbstractEntityRepository<AbstractEntity<?>> entitiesRepositoryMocked;

    private IModelAttrAssocService pModelAttributeService;

    private IAttributeModelService pAttributeModelService;

    private IModelService modelService;

    private IPublisher publisherMocked;

    private EntityManager emMocked;

    /**
     * initialize the repo before each test
     *
     * @throws ModuleException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Before
    public void init() throws ModuleException {
        JWTService jwtService = new JWTService();
        jwtService.injectMockToken("Tenant", "PUBLIC");
        dataSetRepositoryMocked = Mockito.mock(IDatasetRepository.class);
        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        pModelAttributeService = Mockito.mock(IModelAttrAssocService.class);
        modelService = Mockito.mock(IModelService.class);
        pAttributeModelService = Mockito.mock(IAttributeModelService.class);
        emMocked = Mockito.mock(EntityManager.class);

        IRuntimeTenantResolver runtimeTenantResolver = Mockito.mock(IRuntimeTenantResolver.class);
        Mockito.when(runtimeTenantResolver.getTenant()).thenReturn("Tenant");

        // populate the repository
        model1 = new Model();
        model1.setId(1L);
        model2 = new Model();
        model2.setId(2L);

        dataSet1 = new Dataset(model1, "PROJECT", "DS1", "dataSet1");
        dataSet1.setLicence("licence");
        dataSet1.setId(1L);
        dataSet2 = new Dataset(model2, "PROJECT", "DS1", "dataSet2");
        dataSet2.setLicence("licence");
        setModelInPlace(importModel("sample-datamodel-minimal.xml"));
        Mockito.when(modelService.getModel(modelOfObjects.getId())).thenReturn(modelOfObjects);
        dataSet2.setDataModel(modelOfObjects.getName());
        dataSet2.setSubsettingClause(getValidClause());
        dataSet2.setId(2L);

        dataSet1.addTags(dataSet2.getIpId().toString());
        dataSet2.addTags(dataSet1.getIpId().toString());

        // create a mock repository
        Mockito.when(dataSetRepositoryMocked.findById(dataSet1.getId())).thenReturn(Optional.of(dataSet1));
        Mockito.when(dataSetRepositoryMocked.findById(dataSet2.getId())).thenReturn(Optional.of(dataSet2));

        List<AbstractEntity<?>> findByTagsValueCol2IpId = new ArrayList<>();
        findByTagsValueCol2IpId.add(dataSet1);
        Mockito.when(entitiesRepositoryMocked.findByTags(dataSet2.getIpId().toString()))
                .thenReturn(findByTagsValueCol2IpId);
        Mockito.when(entitiesRepositoryMocked.findById(dataSet1.getId())).thenReturn(Optional.of(dataSet1));
        Mockito.when(entitiesRepositoryMocked.findById(dataSet2.getId())).thenReturn(Optional.of(dataSet2));

        IDeletedEntityRepository deletedEntityRepositoryMocked = Mockito.mock(IDeletedEntityRepository.class);

        publisherMocked = Mockito.mock(IPublisher.class);
        dataSetServiceMocked = new DatasetService(dataSetRepositoryMocked, pAttributeModelService,
                pModelAttributeService, entitiesRepositoryMocked, modelService, deletedEntityRepositoryMocked, null,
                emMocked, publisherMocked, runtimeTenantResolver, Mockito.mock(IOpenSearchService.class),
                Mockito.mock(IPluginService.class), Mockito.mock(IAbstractEntityRequestRepository.class));
    }

    /**
     * @param modelAttrAssocs
     */
    private void setModelInPlace(List<ModelAttrAssoc> modelAttrAssocs) {
        modelOfObjects = modelAttrAssocs.get(0).getModel();
        modelOfObjects.setId(3L);
        ModelAttrAssoc attStringModelAtt = modelAttrAssocs.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals(Fragment.getDefaultName())
                        && ma.getAttribute().getName().equals("att_string"))
                .findAny().get();
        attString = attStringModelAtt.getAttribute();
        attString.setId(1L);
        Mockito.when(pAttributeModelService.findByNameAndFragmentName(attString.getName(), null)).thenReturn(attString);
        Mockito.when(pModelAttributeService.getModelAttrAssoc(modelOfObjects.getId(), attString))
                .thenReturn(attStringModelAtt);
        ModelAttrAssoc attBooleanModelAtt = modelAttrAssocs.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals(Fragment.getDefaultName())
                        && ma.getAttribute().getName().equals("att_boolean"))
                .findAny().get();
        attBoolean = attBooleanModelAtt.getAttribute();
        attBoolean.setId(2L);
        Mockito.when(pAttributeModelService.findByNameAndFragmentName(attBoolean.getName(), null))
                .thenReturn(attBoolean);
        Mockito.when(pModelAttributeService.getModelAttrAssoc(modelOfObjects.getId(), attBoolean))
                .thenReturn(attBooleanModelAtt);
        ModelAttrAssoc CRS_CRSModelAtt = modelAttrAssocs.stream()
                .filter(ma -> ma.getAttribute().getFragment().getName().equals("GEO")
                        && ma.getAttribute().getName().equals("CRS"))
                .findAny().get();
        GEO_CRS = CRS_CRSModelAtt.getAttribute();
        GEO_CRS.setId(3L);
        Mockito.when(pAttributeModelService
                .findByNameAndFragmentName(GEO_CRS.getName(), GEO_CRS.getFragment().getName())).thenReturn(GEO_CRS);
        Mockito.when(pModelAttributeService.getModelAttrAssoc(modelOfObjects.getId(), GEO_CRS))
                .thenReturn(CRS_CRSModelAtt);
        ModelAttrAssoc Contact_PhoneModelAtt = modelAttrAssocs.stream()
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

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_400")
    @Requirement("REGARDS_DSL_SYS_ARC_420")
    @Purpose("The dataset identifier is an URN")
    public void createDataset() throws ModuleException, IOException {
        Mockito.when(dataSetRepositoryMocked.save(dataSet2)).thenReturn(dataSet2);
        final Dataset dataSet = dataSetServiceMocked.create(dataSet2);
        Assert.assertEquals(dataSet2, dataSet);
    }

    private List<ModelAttrAssoc> importModel(String pFilename) {
        InputStream input;
        try {
            input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));

            return XmlImportHelper.importModel(input, new ArrayList<>());
        } catch (IOException | ImportException e) {
            LOG.debug("import of model failed", e);
            Assert.fail();
        }
        // never reached because test will fail first
        return null;
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_450")
    @Purpose("The URN identifier of a dataset is uniq")
    public void dataSetUrnUnicity() throws ModuleException, IOException {

        String dataSetName = "dataSet1";

        Dataset dataSet1 = new Dataset(model1, "PROJECT", dataSetName, dataSetName);
        Dataset dataSet2 = new Dataset(model1, "PROJECT", dataSetName, dataSetName);

        Assert.assertNotNull(dataSet1);
        Assert.assertNotNull(dataSet2);
        Assert.assertNotNull(dataSet1.getIpId());
        Assert.assertNotNull(dataSet2.getIpId());
        Assert.assertNotEquals(dataSet1.getIpId(), dataSet2.getIpId());
    }
}
