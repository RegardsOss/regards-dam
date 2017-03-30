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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * Test model creation
 *
 * @author Marc Sordi
 */
@MultitenantTransactional
public class ModelControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelControllerIT.class);

    /**
     * JSON path
     */
    private static final String JSON_ID = "$.content.id";

    /**
     * Model service
     */
    @Autowired
    private IModelService modelService;

    /**
     * Attribute model service
     */
    @Autowired
    private IAttributeModelService attributeModelService;

    /**
     * Model attribute service
     */
    @Autowired
    private IModelAttrAssocService modelAttributeService;

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
        createModel("MISSION", "Mission description", EntityType.COLLECTION);
    }

    /**
     * Create a dataset model
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Create data model")
    public void createDataModelTest() {
        createModel("DATA_MODEL", "Data model description", EntityType.DATA);
    }

    /**
     * Create a dataset model
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Requirement("REGARDS_DSL_DAM_MOD_040")
    @Purpose("Create dataset model (dataset is a model type)")
    public void createDatasetModelTest() {
        createModel("DATASET", "Dataset description", EntityType.DATASET);
    }

    /**
     * Create a dataset model
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Create document model")
    public void createDocumentModelTest() {
        createModel("DOCUMENT", "Document description", EntityType.DOCUMENT);
    }

    /**
     * Create a model
     *
     * @param pName name
     * @param pDescription description
     * @param pType type
     */
    private void createModel(String pName, String pDescription, EntityType pType) {
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
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));

        performDefaultPost(ModelController.TYPE_MAPPING, model, expectations, "Consistent model should be created.");
    }

    /**
     * Export model
     *
     * @throws ModuleException module exception
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Export model - Allows to share model or export reference model")
    public void exportModel() throws ModuleException {

        final Model model = new Model();
        model.setName("EXPORT_MODEL");
        model.setDescription("Exported model");
        model.setType(EntityType.COLLECTION);
        modelService.createModel(model);

        // Attribute #1 in default fragment
        AttributeModel attMod = AttributeModelBuilder.build("att_string", AttributeType.STRING).withoutRestriction();
        attributeModelService.addAttribute(attMod);

        ModelAttrAssoc modAtt = new ModelAttrAssoc();
        modAtt.setAttribute(attMod);
        modelAttributeService.bindAttributeToModel(model.getId(), modAtt);

        // Attribute #2 in default fragment
        attMod = AttributeModelBuilder.build("att_boolean", AttributeType.BOOLEAN).isAlterable().withoutRestriction();
        attributeModelService.addAttribute(attMod);

        modAtt = new ModelAttrAssoc();
        modAtt.setAttribute(attMod);
        modelAttributeService.bindAttributeToModel(model.getId(), modAtt);

        // Geo fragment
        final Fragment geo = Fragment.buildFragment("GEO", "Geographic information");

        // Attribute #3 in geo fragment
        attMod = AttributeModelBuilder.build("CRS", AttributeType.STRING).fragment(geo)
                .withEnumerationRestriction("Earth", "Mars", "Venus");
        attributeModelService.addAttribute(attMod);

        modelAttributeService.bindNSAttributeToModel(model.getId(), attMod.getFragment().getId());

        // Contact fragment
        final Fragment contact = Fragment.buildFragment("Contact", "Contact information");

        // Attribute #5 in contact fragment
        attMod = AttributeModelBuilder.build("Phone", AttributeType.STRING).fragment(contact)
                .withPatternRestriction("[0-9 ]{10}");
        attributeModelService.addAttribute(attMod);

        modelAttributeService.bindNSAttributeToModel(model.getId(), attMod.getFragment().getId());

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        final ResultActions resultActions = performDefaultGet(ModelController.TYPE_MAPPING + "/{pModelId}/export",
                                                              expectations, "Should return result", model.getId());

        assertMediaType(resultActions, MediaType.APPLICATION_OCTET_STREAM);
        Assert.assertNotNull(payload(resultActions));
    }
}
