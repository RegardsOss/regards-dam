/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.entities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.assertj.core.util.Sets;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.client.models.IAttributeModelClient;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.dao.models.IAttributeModelRepository;
import fr.cnes.regards.modules.dam.dao.models.IAttributePropertyRepository;
import fr.cnes.regards.modules.dam.dao.models.IModelAttrAssocRepository;
import fr.cnes.regards.modules.dam.dao.models.IModelRepository;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.gson.entities.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.dam.rest.DamRestConfiguration;
import fr.cnes.regards.modules.dam.rest.entities.dto.DatasetDataAttributesRequestBody;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@DirtiesContext
@TestPropertySource(locations = { "classpath:test.properties" },
        properties = { "spring.jpa.properties.hibernate.default_schema=dam_datasets_test" })
@ContextConfiguration(classes = { DamRestConfiguration.class })
public class DatasetControllerIT extends AbstractRegardsIT {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetControllerIT.class);

    private Model model1;

    private Dataset dataSet1;

    private Dataset dataSet3;

    private Dataset dataSet4;

    private Model modelOfData;

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IAttributeModelService attributeModelService;

    @Autowired
    private IModelRepository modelRepository;

    @Autowired
    private IAttributeModelRepository attModelRepo;

    @Autowired
    private IModelAttrAssocRepository attrModelAssocRepo;

    @Autowired
    private IAttributePropertyRepository attModelPropertyRepo;

    @Autowired
    private IAttributeModelClient attributeModelClient;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Before
    public void init() {

        tenantResolver.forceTenant(getDefaultTenant());

        clear();

        // Bootstrap default values
        model1 = Model.build("modelName1", "model desc", EntityType.DATASET);
        model1 = modelRepository.save(model1);

        modelOfData = Model.build("modelOfData", "model desc", EntityType.DATA);
        modelOfData = modelRepository.save(modelOfData);
        dataSet1 = new Dataset(model1, "PROJECT", "DS1", "collection1");
        dataSet1.setCreationDate(OffsetDateTime.now());
        dataSet1.setLicence("licence");
        dataSet1.setProviderId("ProviderId1");
        dataSet1.setLabel("label");
        dataSet3 = new Dataset(model1, "PROJECT", "DS3", "collection3");
        dataSet3.setCreationDate(OffsetDateTime.now());
        dataSet3.setLicence("licence");
        dataSet3.setProviderId("ProviderId3");
        dataSet3.setLabel("label");
        dataSet4 = new Dataset(model1, "PROJECT", "DS4", "collection4");
        dataSet4.setCreationDate(OffsetDateTime.now());
        dataSet4.setLicence("licence");
        dataSet4.setProviderId("ProviderId4");
        dataSet4.setLabel("label");
        final Set<String> col1Tags = new HashSet<>();
        final Set<String> col4Tags = new HashSet<>();
        col1Tags.add(dataSet4.getIpId().toString());
        col4Tags.add(dataSet1.getIpId().toString());
        dataSet1.setTags(col1Tags);
        dataSet4.setTags(col4Tags);

        dataSet1 = datasetRepository.save(dataSet1);
        dataSet3 = datasetRepository.save(dataSet3);
        dataSet4 = datasetRepository.save(dataSet4);
    }

    public void clear() {

        datasetRepository.deleteAll();

        attrModelAssocRepo.deleteAll();
        attModelPropertyRepo.deleteAll();
        attModelRepo.deleteAll();
        modelRepository.deleteAll();
    }

    @Test
    public void testGetAllDatasets() {
        RequestBuilderCustomizer customizer = customizer();
        customizer.expect(MockMvcResultMatchers.status().isOk());
        customizer.expect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DatasetController.TYPE_MAPPING, customizer, "Failed to fetch dataset list");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_010")
    @Requirement("REGARDS_DSL_DAM_SET_020")
    @Requirement("REGARDS_DSL_DAM_SET_110")
    @Requirement("REGARDS_DSL_DAM_SET_120")
    @Purpose("Dataset creation")
    public void testPostDataset() throws Exception {

        importModel("dataModel.xml");
        importModel("datasetModel.xml");
        final Model dataModel = modelService.getModelByName("dataModel");
        final Model datasetModel = modelService.getModelByName("datasetModel");
        Mockito.when(attributeModelClient.getAttributes(null, null)).thenReturn(ResponseEntity
                .ok(HateoasUtils.wrapList(attributeModelService.getAttributes(null, null, null))));

        final Dataset dataSet2 = new Dataset(datasetModel, getDefaultTenant(), "DS1", "Coucou");
        dataSet2.setLicence("licence");
        dataSet2.setCreationDate(OffsetDateTime.now());
        dataSet2.setProviderId("ProviderId2");
        dataSet2.setDataModel(dataModel.getName());
        dataSet2.addProperty(AttributeBuilder.buildDate("START_DATE", OffsetDateTime.now().minusDays(1)));
        dataSet2.addProperty(AttributeBuilder.buildDate("STOP_DATE", OffsetDateTime.now().plusDays(1)));
        dataSet2.addProperty(AttributeBuilder.buildInteger("FILE_SIZE", 445445));
        dataSet2.addProperty(AttributeBuilder.buildLong("vcount", 454L));

        // Set test case
        dataSet2.setOpenSearchSubsettingClause("tags:10");

        RequestBuilderCustomizer customizer = customizer();
        customizer.expect(MockMvcResultMatchers.status().isCreated());
        performDefaultPost(DatasetController.TYPE_MAPPING, dataSet2, customizer, "Failed to create a new dataset");

        final Dataset dataSet21 = new Dataset(model1, getDefaultTenant(), "DS21", "dataSet21");
        dataSet21.setLicence("licence");
        dataSet21.setCreationDate(OffsetDateTime.now());

        customizer = customizer();
        customizer.expect(MockMvcResultMatchers.status().isCreated());
        performDefaultPost(DatasetController.TYPE_MAPPING, dataSet21, customizer, "Failed to create a new dataset");
    }

    @Test
    public void testGetDatasetById() {
        RequestBuilderCustomizer customizer = customizer();
        customizer.expect(MockMvcResultMatchers.status().isOk());
        customizer.expect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DatasetController.TYPE_MAPPING + DatasetController.DATASET_ID_PATH, customizer,
                          "Failed to fetch a specific dataset using its id", dataSet1.getId());
    }

    @Test
    public void testUpdateDataset() {
        final Dataset dataSetClone = new Dataset(dataSet1.getModel(), "", "DS1CLONE", "dataset1clone");
        dataSetClone.setLicence("licence");
        dataSetClone.setCreationDate(OffsetDateTime.now());
        dataSetClone.setIpId(dataSet1.getIpId());
        dataSetClone.setId(dataSet1.getId());
        dataSetClone.setTags(dataSet1.getTags());
        dataSetClone.setProviderId(dataSet1.getProviderId() + "new");
        RequestBuilderCustomizer expectations = customizer();
        expectations.expect(MockMvcResultMatchers.status().isOk());
        expectations.expect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPut(DatasetController.TYPE_MAPPING + DatasetController.DATASET_ID_PATH, dataSet1, expectations,
                          "Failed to update a specific dataset using its id", dataSetClone.getId());

    }

    @Test
    public void testFullUpdate() {
        final Dataset dataSetClone = new Dataset(dataSet1.getModel(), "", "DS1CLONE", "collection1clone");
        dataSetClone.setLicence("licence");
        dataSetClone.setCreationDate(OffsetDateTime.now());
        dataSetClone.setIpId(dataSet1.getIpId());
        dataSetClone.setId(dataSet1.getId());
        dataSetClone.setProviderId(dataSet1.getProviderId() + "new");
        dataSetClone.setLabel("label");
        RequestBuilderCustomizer expectations = customizer();
        expectations.expect(MockMvcResultMatchers.status().isOk());
        expectations.expect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPut(DatasetController.TYPE_MAPPING + DatasetController.DATASET_ID_PATH, dataSetClone,
                          expectations, "Failed to update a specific dataset using its id", dataSetClone.getId());
    }

    @Test
    public void testDeleteDataset() {
        RequestBuilderCustomizer expectations = customizer();
        expectations.expect(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(DatasetController.TYPE_MAPPING + DatasetController.DATASET_ID_PATH, expectations,
                             "Failed to delete a specific dataset using its id", dataSet1.getId());
    }

    @Test
    public void testGetDataAttributes() throws ModuleException {
        importModel("dataModel.xml");
        importModel("datasetModel.xml");
        final Model dataModel = modelService.getModelByName("dataModel");
        final Model datasetModel = modelService.getModelByName("datasetModel");
        Dataset ds = new Dataset(datasetModel, getDefaultTenant(), "DS1", "dataset for getDataAttribute tests");
        ds.setCreationDate(OffsetDateTime.now());
        ds.setLicence("pLicence");
        ds.setDataModel(dataModel.getName());
        ds = datasetRepository.save(ds);

        DatasetDataAttributesRequestBody body = new DatasetDataAttributesRequestBody();
        Set<Long> modelIds = Sets.newHashSet();
        modelIds.add(datasetModel.getId());
        body.setModelIds(modelIds);

        RequestBuilderCustomizer expectations = customizer();
        expectations.expect(MockMvcResultMatchers.status().isOk());
        expectations.expect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(20)));
        performDefaultPost(DatasetController.TYPE_MAPPING + DatasetController.DATASET_DATA_ATTRIBUTES_PATH, body,
                           expectations, "failed to fetch the data attributes");

        final StringJoiner sj = new StringJoiner("&", "?", "");
        sj.add("page=1");
        String queryParams = sj.toString();
        expectations = customizer();
        expectations.expect(MockMvcResultMatchers.status().isOk());
        expectations.expect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(5)));
        performDefaultPost(DatasetController.TYPE_MAPPING + DatasetController.DATASET_DATA_ATTRIBUTES_PATH
                + queryParams, body, expectations, "failed to fetch the data attributes");
    }

    @Test
    public void testGetDataSetAttributes() throws ModuleException {
        importModel("dataModel.xml");
        importModel("datasetModel.xml");
        final Model dataModel = modelService.getModelByName("dataModel");
        final Model datasetModel = modelService.getModelByName("datasetModel");
        Dataset ds = new Dataset(datasetModel, getDefaultTenant(), "DS2", "dataset for getDataAttribute tests");
        ds.setCreationDate(OffsetDateTime.now());
        ds.setLicence("pLicence");
        ds.setDataModel(dataModel.getName());
        ds = datasetRepository.save(ds);

        DatasetDataAttributesRequestBody body = new DatasetDataAttributesRequestBody();
        Set<Long> modelIds = Sets.newHashSet();
        modelIds.add(datasetModel.getId());
        body.setModelIds(modelIds);

        RequestBuilderCustomizer expectations = customizer();
        expectations.expect(MockMvcResultMatchers.status().isOk());
        expectations.expect(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(4)));
        performDefaultPost(DatasetController.TYPE_MAPPING + DatasetController.DATASET_ATTRIBUTES_PATH, body,
                           expectations, "failed to fetch the data attributes");
    }

    @Test
    public void testSubsettingValidation() throws ModuleException {

        importModel("dataModel.xml");
        Mockito.when(attributeModelClient.getAttributes(null, null)).thenReturn(ResponseEntity
                .ok(HateoasUtils.wrapList(attributeModelService.getAttributes(null, null, null))));
        final Model dataModel = modelService.getModelByName("dataModel");

        RequestBuilderCustomizer expectations = customizer();
        expectations.expect(MockMvcResultMatchers.status().isOk());
        expectations.expect(MockMvcResultMatchers.jsonPath("$.validity", Matchers.equalTo(true)));

        DatasetController.Query query = new DatasetController.Query("properties.FILE_SIZE:10%20AND%20tags:abc");
        performDefaultPost(DatasetController.TYPE_MAPPING + DatasetController.DATA_SUB_SETTING_VALIDATION
                + "?dataModelName=" + dataModel.getName(), query, expectations,
                           "Could not validate that subsetting clause");

        query = new DatasetController.Query("properties.DO_NOT_EXIST:10");
        expectations = customizer();
        expectations.expect(MockMvcResultMatchers.status().isOk());
        expectations.expect(MockMvcResultMatchers.jsonPath("$.validity", Matchers.equalTo(false)));
        performDefaultPost(DatasetController.TYPE_MAPPING + DatasetController.DATA_SUB_SETTING_VALIDATION
                + "?dataModelName=" + dataModel.getName(), query, expectations,
                           "Could validate that subsetting clause");
    }

    /**
     * Import model definition file from resources directory
     *
     * @param pFilename
     *            filename
     * @throws ModuleException
     *             if error occurs
     */
    private void importModel(final String pFilename) throws ModuleException {
        try {
            final InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));
            modelService.importModel(input);
            final List<AttributeModel> atts = attributeModelService.getAttributes(null, null, null);
            gsonAttributeFactory.refresh(getDefaultTenant(), atts);
        } catch (final IOException e) {
            final String errorMessage = "Cannot import " + pFilename;
            throw new AssertionError(errorMessage);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
