/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.entities.dao.IDataSetRepository;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.rest.DataSetController;
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

        dataSet1 = new DataSet(model1, "PROJECT", "collection1");
        dataSet1.setSipId("SipId1");
        dataSet1.setLabel("label");
        dataSet3 = new DataSet(model1, "PROJECT", "collection3");
        dataSet3.setSipId("SipId3");
        dataSet3.setLabel("label");
        dataSet4 = new DataSet(model1, "PROJECT", "collection4");
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
    public void testPostDataSet() {
        final DataSet dataSet2 = new DataSet(model1, null, "collection2");

        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(DataSetController.DATASET_PATH, dataSet2, expectations, "Failed to create a new dataset");

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
        final DataSet dataSetClone = new DataSet(dataSet1.getModel(), "", "dataset1clone");
        dataSetClone.setIpId(dataSet1.getIpId());
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
        final DataSet dataSetClone = new DataSet(dataSet1.getModel(), "", "collection1clone");
        dataSetClone.setIpId(dataSet1.getIpId());
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

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
