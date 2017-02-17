/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.rest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.entities.dao.IDataSetRepository;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.rest.DataSetController;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource(locations = { "classpath:test.properties" })
@MultitenantTransactional
public class DataSetControllerIT extends AbstractRegardsTransactionalIT {

    private static final Logger LOG = LoggerFactory.getLogger(DataSetControllerIT.class);

    private Model model1;

    private DataSet dataSet1;

    private DataSet dataSet3;

    private DataSet dataSet4;

    private DataSource dataSource1;

    private DataSource dataSource2;

    @Autowired
    private IDataSetRepository dataSetRepository;

    @Autowired
    private IModelRepository modelRepository;

    private List<ResultMatcher> expectations;

    @Before
    public void init() {

        expectations = new ArrayList<>();
        // Bootstrap default values
        model1 = Model.build("modelName1", "model desc", EntityType.DATASET);
        model1 = modelRepository.save(model1);

        Model modelOfData = Model.build("modelOfData", "model desc", EntityType.DATA);
        modelOfData = modelRepository.save(modelOfData);

        dataSet1 = new DataSet(model1, getUrn(), "collection1", "licence");
        dataSet1.setSipId("SipId1");
        dataSet1.setLabel("label");
        dataSet3 = new DataSet(model1, getUrn(), "collection3", "licence");
        dataSet3.setSipId("SipId3");
        dataSet3.setLabel("label");
        dataSet4 = new DataSet(model1, getUrn(), "collection4", "licence");
        dataSet4.setSipId("SipId4");
        dataSet4.setLabel("label");
        final Set<String> col1Tags = new HashSet<>();
        final Set<String> col4Tags = new HashSet<>();
        col1Tags.add(dataSet4.getIpId().toString());
        col4Tags.add(dataSet1.getIpId().toString());
        dataSet1.setTags(col1Tags);
        dataSet4.setTags(col4Tags);

        dataSet1 = dataSetRepository.save(dataSet1);
        dataSet3 = dataSetRepository.save(dataSet3);
        dataSet4 = dataSetRepository.save(dataSet4);
    }

    @Test
    public void testGetAllDataSets() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DataSetController.DATASET_PATH, expectations, "Failed to fetch dataset list");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_010")
    @Requirement("REGARDS_DSL_DAM_SET_020")
    @Requirement("REGARDS_DSL_DAM_SET_110")
    @Requirement("REGARDS_DSL_DAM_SET_120")
    @Purpose("Un modèle de jeu de données possède des attributs obligatoires par défaut : description, citations,licence. Un modèle de jeu de données possède des attributs internes par défaut : score. Ces attributs ne sont utiles qu’au catalogue REGARDS et ne doivent pas être archivés dans un quelconque AIP. Le système doit permettre de créer des jeux de données par l’instanciation d’un modèle de jeu de données. Un jeu de données doit être associé au maximum à une vue sur une source de données.")
    public void testPostDataSet() throws Exception {
        final DataSet dataSet2 = new DataSet(model1, null, "dataSet2", "licence");
        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        MockMultipartFile firstFile = new MockMultipartFile("file", "filename.txt", "text/markdown",
                "some xml".getBytes());
        MockMultipartFile dataset = new MockMultipartFile("dataset", "", MediaType.APPLICATION_JSON_VALUE,
                gson(dataSet2).getBytes());

        List<MockMultipartFile> fileList = new ArrayList<>(2);
        fileList.add(dataset);
        fileList.add(firstFile);

        performDefaultFileUpload(DataSetController.DATASET_PATH, fileList, expectations,
                                 "Failed to create a new dataset");

        DataSet dataSet21 = new DataSet(model1, null, "dataSet21", "licence");

        final byte[] input = Files.readAllBytes(Paths.get("src", "test", "resources",
                                                          "SGDS-CP-12200-0010-CS[DossierConceptionPréliminaire].pdf"));
        MockMultipartFile pdf = new MockMultipartFile("file",
                "SGDS-CP-12200-0010-CS[DossierConceptionPréliminaire].pdf", MediaType.APPLICATION_PDF_VALUE, input);
        MockMultipartFile dataset21 = new MockMultipartFile("dataset", "", MediaType.APPLICATION_JSON_VALUE,
                gson(dataSet21).getBytes());
        fileList.clear();
        fileList.add(pdf);
        fileList.add(dataset21);
        performDefaultFileUpload(DataSetController.DATASET_PATH, fileList, expectations,
                                 "Failed to create a new dataset");
    }

    @Test
    public void testGetDataSetById() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DataSetController.DATASET_PATH + DataSetController.DATASET_ID_PATH, expectations,
                          "Failed to fetch a specific dataset using its id", dataSet1.getId());
    }

    @Test
    public void testUpdateDataSet() {
        final DataSet dataSetClone = new DataSet(dataSet1.getModel(), dataSet1.getIpId(), "dataset1clone",
                "licenceClone");
        dataSetClone.setId(dataSet1.getId());
        dataSetClone.setTags(dataSet1.getTags());
        dataSetClone.setSipId(dataSet1.getSipId() + "new");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultPut(DataSetController.DATASET_PATH + DataSetController.DATASET_ID_PATH, dataSetClone,
                          expectations, "Failed to update a specific dataset using its id", dataSet1.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_210")
    @Purpose("Le système permet de changer la vue associée au jeu de données.")
    public void testUpdateDataSetDataSource() {
        final DataSet dataSetClone = new DataSet(dataSet1.getModel(), dataSet1.getIpId(), "dataset1clone",
                "licenceClone");
        dataSetClone.setId(dataSet1.getId());
        dataSetClone.setTags(dataSet1.getTags());
        dataSetClone.setSipId(dataSet1.getSipId() + "new");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultPut(DataSetController.DATASET_PATH + DataSetController.DATASET_ID_PATH, dataSetClone,
                          expectations, "Failed to update a specific dataset using its id", dataSet1.getId());
    }

    @Test
    public void testFullUpdate() {
        final DataSet dataSetClone = new DataSet(dataSet1.getModel(), dataSet1.getIpId(), "collection1clone",
                "licenceClone");
        dataSetClone.setId(dataSet1.getId());
        dataSetClone.setSipId(dataSet1.getSipId() + "new");
        dataSetClone.setLabel("label");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPut(DataSetController.DATASET_PATH + DataSetController.DATASET_ID_PATH, dataSetClone,
                          expectations, "Failed to update a specific dataset using its id", dataSet1.getId());

    }

    @Test
    public void testDeleteDataSet() {
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(DataSetController.DATASET_PATH + DataSetController.DATASET_ID_PATH, expectations,
                             "Failed to delete a specific dataset using its id", dataSet1.getId());
    }

    @Test
    public void testGetServices() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DataSetController.DATASET_PATH + DataSetController.DATASET_ID_SERVICES_PATH, expectations,
                          "Failed to fetch services list", dataSet1.getId());
    }

    private UniformResourceName getUrn() {
        return new UniformResourceName(OAISIdentifier.AIP, EntityType.DATASET, "PROJECT", UUID.randomUUID(), 1);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
