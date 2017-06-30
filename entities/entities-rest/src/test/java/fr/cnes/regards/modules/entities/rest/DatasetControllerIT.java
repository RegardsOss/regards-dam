/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.utils.HttpConstants;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.modules.models.client.IAttributeModelClient;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@DirtiesContext
@TestPropertySource(locations = { "classpath:test.properties" })
@MultitenantTransactional
@ContextConfiguration(classes = { ControllerITConfig.class })
public class DatasetControllerIT extends AbstractRegardsTransactionalIT {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetControllerIT.class);

    private Model model1;

    private Dataset dataSet1;

    private Dataset dataSet3;

    private Dataset dataSet4;

    private Model modelOfData;

    @Autowired
    private IDatasetService dsService;

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IAttributeModelService attributeModelService;

    @Autowired
    private IModelRepository modelRepository;

    private List<ResultMatcher> expectations;

    @Autowired
    private IAttributeModelClient attributeModelClient;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    @Before
    public void init() {

        expectations = new ArrayList<>();
        // Bootstrap default values
        model1 = Model.build("modelName1", "model desc", EntityType.DATASET);
        model1 = modelRepository.save(model1);

        modelOfData = Model.build("modelOfData", "model desc", EntityType.DATA);
        modelOfData = modelRepository.save(modelOfData);
        dataSet1 = new Dataset(model1, "PROJECT", "collection1");
        dataSet1.setCreationDate(OffsetDateTime.now());
        dataSet1.setLicence("licence");
        dataSet1.setSipId("SipId1");
        dataSet1.setLabel("label");
        dataSet3 = new Dataset(model1, "PROJECT", "collection3");
        dataSet3.setCreationDate(OffsetDateTime.now());
        dataSet3.setLicence("licence");
        dataSet3.setSipId("SipId3");
        dataSet3.setLabel("label");
        dataSet4 = new Dataset(model1, "PROJECT", "collection4");
        dataSet4.setCreationDate(OffsetDateTime.now());
        dataSet4.setLicence("licence");
        dataSet4.setSipId("SipId4");
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

    @Test
    public void testGetAllDatasets() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DatasetController.DATASET_PATH, expectations, "Failed to fetch dataset list");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_010")
    @Requirement("REGARDS_DSL_DAM_SET_020")
    @Requirement("REGARDS_DSL_DAM_SET_110")
    @Requirement("REGARDS_DSL_DAM_SET_120")
    @Purpose(
            "Un modèle de jeu de données possède des attributs obligatoires par défaut : description, citations,licence. Un modèle de jeu de données possède des attributs internes par défaut : score. Ces attributs ne sont utiles qu’au catalogue REGARDS et ne doivent pas être archivés dans un quelconque AIP. Le système doit permettre de créer des jeux de données par l’instanciation d’un modèle de jeu de données. Un jeu de données doit être associé au maximum à une vue sur une source de données.")
    public void testPostDataset() throws Exception {

        importModel("dataModel.xml");
        importModel("datasetModel.xml");
        final Model dataModel = modelService.getModelByName("dataModel");
        final Model datasetModel = modelService.getModelByName("datasetModel");
        Mockito.when(attributeModelClient.getAttributes(null,null)).thenReturn(
                ResponseEntity.ok(HateoasUtils.wrapList(attributeModelService.getAttributes(null, null))));

        final Dataset dataSet2 = new Dataset(datasetModel, DEFAULT_TENANT, "Coucou");
        dataSet2.setLicence("licence");
        dataSet2.setCreationDate(OffsetDateTime.now());
        dataSet2.setSipId("SipId2");
        dataSet2.setDataModel(dataModel.getId());
        dataSet2.getProperties().add(AttributeBuilder.buildDate("START_DATE", OffsetDateTime.now().minusDays(1)));
        dataSet2.getProperties().add(AttributeBuilder.buildDate("STOP_DATE", OffsetDateTime.now().plusDays(1)));
        dataSet2.getProperties().add(AttributeBuilder.buildInteger("FILE_SIZE", 445445));
        dataSet2.getProperties().add(AttributeBuilder.buildLong("count", 454L));

        // Set test case
        dataSet2.setOpenSearchSubsettingClause("properties.FILE_SIZE:10");
        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        final MockMultipartFile firstFile = new MockMultipartFile("file", "filename.txt", "text/markdown",
                                                                  "some xml".getBytes());
        final MockMultipartFile dataset = new MockMultipartFile("dataset", "", MediaType.APPLICATION_JSON_VALUE,
                                                                gson(dataSet2).getBytes());

        final List<MockMultipartFile> fileList = new ArrayList<>(2);
        fileList.add(dataset);
        fileList.add(firstFile);

        performDefaultFileUploadPost(DatasetController.DATASET_PATH, fileList, expectations,
                                     "Failed to create a new dataset");

        final Dataset dataSet21 = new Dataset(model1, DEFAULT_TENANT, "dataSet21");
        dataSet21.setLicence("licence");
        dataSet21.setCreationDate(OffsetDateTime.now());

        final byte[] input = Files.readAllBytes(Paths.get("src", "test", "resources", "test.pdf"));
        final MockMultipartFile pdf = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, input);
        final MockMultipartFile dataset21 = new MockMultipartFile("dataset", "", MediaType.APPLICATION_JSON_VALUE,
                                                                  gson(dataSet21).getBytes());
        fileList.clear();
        fileList.add(pdf);
        fileList.add(dataset21);
        performDefaultFileUploadPost(DatasetController.DATASET_PATH, fileList, expectations,
                                     "Failed to create a new dataset");
    }

    @Test
    public void testDatasetDescriptionFile() throws IOException, ModuleException {

        Dataset dataSet21 = new Dataset(model1, DEFAULT_TENANT, "dataSet21");
        dataSet21.setLicence("licence");
        dataSet21.setCreationDate(OffsetDateTime.now());
        dataSet21.setDescriptionFile(new DescriptionFile(new byte[0], MediaType.APPLICATION_PDF));
        final byte[] input = Files.readAllBytes(Paths.get("src", "test", "resources", "test.pdf"));
        final MockMultipartFile pdf = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, input);
        dataSet21 = dsService.create(dataSet21, pdf);
        expectations.add(MockMvcResultMatchers.status().is2xxSuccessful());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PDF_VALUE));
        expectations.add(MockMvcResultMatchers.content().bytes(pdf.getBytes()));
        performDefaultGet(DatasetController.DATASET_PATH + DatasetController.DATASET_ID_PATH_FILE, expectations,
                          "Could not fetch dataset description file", dataSet21.getId());

        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(DatasetController.DATASET_PATH + DatasetController.DATASET_ID_PATH_FILE, expectations,
                             "Could not delete dataset description file", dataSet21.getId());
    }

    protected MockHttpServletRequestBuilder getRequestBuilder(final String pAuthToken, final HttpMethod pHttpMethod,
            final String pUrlTemplate, final Object... pUrlVars) {

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .request(pHttpMethod, pUrlTemplate, pUrlVars);
        addSecurityHeader(requestBuilder, pAuthToken);

        requestBuilder.header(HttpConstants.CONTENT_TYPE, "application/json");

        return requestBuilder;
    }

    @Test
    public void testGetDatasetById() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DatasetController.DATASET_PATH + DatasetController.DATASET_ID_PATH, expectations,
                          "Failed to fetch a specific dataset using its id", dataSet1.getId());
    }

    @Test
    public void testUpdateDataset() {
        final Dataset dataSetClone = new Dataset(dataSet1.getModel(), "", "dataset1clone");
        dataSetClone.setLicence("licence");
        dataSetClone.setCreationDate(OffsetDateTime.now());
        dataSetClone.setIpId(dataSet1.getIpId());
        dataSetClone.setId(dataSet1.getId());
        dataSetClone.setTags(dataSet1.getTags());
        dataSetClone.setSipId(dataSet1.getSipId() + "new");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        final MockMultipartFile dataset = new MockMultipartFile("dataset", "", MediaType.APPLICATION_JSON_VALUE,
                                                                gson(dataSetClone).getBytes());
        List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(dataset);

        performDefaultFileUpload(RequestMethod.POST, DatasetController.DATASET_PATH + DatasetController.DATASET_ID_PATH,
                                 parts, expectations, "Failed to update a specific dataset using its id",
                                 dataSetClone.getId());

    }

    @Test
    public void testFullUpdate() {
        final Dataset dataSetClone = new Dataset(dataSet1.getModel(), "", "collection1clone");
        dataSetClone.setLicence("licence");
        dataSetClone.setCreationDate(OffsetDateTime.now());
        dataSetClone.setIpId(dataSet1.getIpId());
        dataSetClone.setId(dataSet1.getId());
        dataSetClone.setSipId(dataSet1.getSipId() + "new");
        dataSetClone.setLabel("label");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        final MockMultipartFile dataset = new MockMultipartFile("dataset", "", MediaType.APPLICATION_JSON_VALUE,
                                                                gson(dataSetClone).getBytes());
        List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(dataset);

        performDefaultFileUpload(RequestMethod.POST, DatasetController.DATASET_PATH + DatasetController.DATASET_ID_PATH,
                                 parts, expectations, "Failed to update a specific dataset using its id",
                                 dataSetClone.getId());
    }

    @Test
    public void testDeleteDataset() {
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(DatasetController.DATASET_PATH + DatasetController.DATASET_ID_PATH, expectations,
                             "Failed to delete a specific dataset using its id", dataSet1.getId());
    }

    @Test
    public void testGetDataAttributes() throws ModuleException {
        importModel("dataModel.xml");
        importModel("datasetModel.xml");
        final Model dataModel = modelService.getModelByName("dataModel");
        final Model datasetModel = modelService.getModelByName("datasetModel");
        Dataset ds = new Dataset(datasetModel, DEFAULT_TENANT, "dataset for getDataAttribute tests");
        ds.setCreationDate(OffsetDateTime.now());
        ds.setLicence("pLicence");
        ds.setDataModel(dataModel.getId());
        ds = datasetRepository.save(ds);
        final StringJoiner sj = new StringJoiner("&", "?", "");
        sj.add("modelIds=" + datasetModel.getId());
        String queryParams = sj.toString();

        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(20)));
        performDefaultGet(DatasetController.DATASET_PATH + DatasetController.DATASET_DATA_ATTRIBUTES_PATH + queryParams,
                          expectations, "failed to fetch the data attributes");
        sj.add("page=1");
        queryParams = sj.toString();
        expectations.set(expectations.size() - 1, MockMvcResultMatchers.jsonPath("$.content", Matchers.hasSize(5)));
        performDefaultGet(DatasetController.DATASET_PATH + DatasetController.DATASET_DATA_ATTRIBUTES_PATH + queryParams,
                          expectations, "failed to fetch the data attributes");
    }

    @Test
    public void testSubsettingValidation() throws ModuleException {

        importModel("dataModel.xml");
        Mockito.when(attributeModelClient.getAttributes(null,null)).thenReturn(
                ResponseEntity.ok(HateoasUtils.wrapList(attributeModelService.getAttributes(null, null))));
        final Model dataModel = modelService.getModelByName("dataModel");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$.validity", Matchers.equalTo(true)));

        DatasetController.Query query=new DatasetController.Query("properties.FILE_SIZE:10");
        performDefaultPost(DatasetController.DATASET_PATH+DatasetController.DATA_SUB_SETTING_VALIDATION+"?dataModelId="+dataModel.getId(),query,expectations,"Could not validate that subsetting clause");

        query=new DatasetController.Query("properties.DO_NOT_EXIST:10");
        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$.validity", Matchers.equalTo(false)));
        performDefaultPost(DatasetController.DATASET_PATH+DatasetController.DATA_SUB_SETTING_VALIDATION+"?dataModelId="+dataModel.getId(),query,expectations,"Could not validate that subsetting clause");
    }

    /**
     * Import model definition file from resources directory
     *
     * @param pFilename
     *            filename
     * @return list of created model attributes
     * @throws ModuleException
     *             if error occurs
     */
    private void importModel(final String pFilename) throws ModuleException {
        try {
            final InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));
            modelService.importModel(input);
            final List<AttributeModel> atts = attributeModelService.getAttributes(null, null);
            gsonAttributeFactory.refresh(DEFAULT_TENANT, atts);
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
