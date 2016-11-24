/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelType;

/**
 *
 * Test model creation
 *
 * @author Marc Sordi
 *
 */
@MultitenantTransactional
public class ModelControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelControllerIT.class);

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Test
    public void createEmptyModelTest() {

        final Model model = new Model();

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isBadRequest());

        performDefaultPost(ModelController.TYPE_MAPPING, model, expectations, "Empty model shouldn't be created.");
    }

    /**
     * Create a collection model
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Create collection model")
    public void createCollectionModelTest() {
        createModel("MISSION", "Mission description", ModelType.COLLECTION);
    }

    /**
     * Create a dataset model
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Create data model")
    public void createDataModelTest() {
        createModel("DATA_MODEL", "Data model description", ModelType.DATA);
    }

    /**
     * Create a dataset model
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Requirement("REGARDS_DSL_DAM_MOD_040")
    @Purpose("Create dataset model (dataset is a model type)")
    public void createDatasetModelTest() {
        createModel("DATASET", "Dataset description", ModelType.DATASET);
    }

    /**
     * Create a dataset model
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Create document model")
    public void createDocumentModelTest() {
        createModel("DOCUMENT", "Document description", ModelType.DOCUMENT);
    }

    /**
     * Create a model
     *
     * @param pName
     *            name
     * @param pDescription
     *            description
     * @param pType
     *            type
     */
    private void createModel(String pName, String pDescription, ModelType pType) {
        Assert.assertNotNull(pName);
        Assert.assertNotNull(pDescription);
        Assert.assertNotNull(pType);

        final Model model = new Model();
        model.setName(pName);
        model.setDescription(pDescription);
        model.setType(pType);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$.id", Matchers.notNullValue()));

        performDefaultPost(ModelController.TYPE_MAPPING, model, expectations, "Consistent model should be created.");
    }

}
