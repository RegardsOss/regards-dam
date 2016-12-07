/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.nio.file.Path;
import java.nio.file.Paths;
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
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.ComputationMode;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.ModelType;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;

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

    /**
     * Model service
     */
    @Autowired
    private IModelService modelService;

    /**
     * Model repository
     */
    @Autowired
    private IModelRepository modelRepository;

    /**
     * Attribute model service
     */
    @Autowired
    private IAttributeModelService attributeModelService;

    /**
     * Model attribute service
     */
    @Autowired
    private IModelAttributeService modelAttributeService;

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

    /**
     * Export model
     *
     * @throws ModuleException
     *             module exception
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Export model - Allows to share model or export reference model")
    public void exportModel() throws ModuleException {

        final Model model = new Model();
        model.setName("EXPORT_MODEL");
        model.setDescription("Exported model");
        model.setType(ModelType.COLLECTION);
        modelService.createModel(model);

        // Attribute #1 in default fragment
        AttributeModel attMod = AttributeModelBuilder.build("att_string", AttributeType.STRING).withoutRestriction();
        attributeModelService.addAttribute(attMod);

        ModelAttribute modAtt = new ModelAttribute();
        modAtt.setAttribute(attMod);
        modelAttributeService.bindAttributeToModel(model.getId(), modAtt);

        // Attribute #2 in default fragment
        attMod = AttributeModelBuilder.build("att_boolean", AttributeType.BOOLEAN).isAlterable().withoutRestriction();
        attributeModelService.addAttribute(attMod);

        modAtt = new ModelAttribute();
        modAtt.setAttribute(attMod);
        modelAttributeService.bindAttributeToModel(model.getId(), modAtt);

        // Geo fragment
        final Fragment geo = Fragment.buildFragment("GEO", "Geographic information");

        // Attribute #3 in geo fragment
        attMod = AttributeModelBuilder.build("CRS", AttributeType.STRING).fragment(geo)
                .withEnumerationRestriction("Earth", "Mars", "Venus");
        attributeModelService.addAttribute(attMod);

        // Attribute #4 in geo fragment
        attMod = AttributeModelBuilder.build("GEOMETRY", AttributeType.GEOMETRY).fragment(geo).withoutRestriction();
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

    /**
     * Import model
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Import fragment - Allows to share model or add predefined model ")
    public void importFragment() throws ModuleException {

        final Path filePath = Paths.get("src", "test", "resources", "model_it.xml");

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultFileUpload(ModelController.TYPE_MAPPING + "/import", filePath, expectations,
                                 "Should be able to import a fragment");

        // Get model from repository
        final Model model = modelRepository.findByName("sample");
        Assert.assertNotNull(model);

        // Get model attributes
        final List<ModelAttribute> modAtts = modelAttributeService.getModelAttributes(model.getId());
        Assert.assertNotNull(modAtts);
        final int expectedSize = 4;
        Assert.assertEquals(expectedSize, modAtts.size());

        for (ModelAttribute modAtt : modAtts) {

            final AttributeModel attModel = modAtt.getAttribute();

            if ("att_string".equals(attModel.getName())) {
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.STRING, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertFalse(attModel.isFacetable());
                Assert.assertTrue(attModel.isOptional());
                Assert.assertFalse(attModel.isQueryable());
                Assert.assertNull(attModel.getRestriction());
                Assert.assertEquals(Fragment.buildDefault(), attModel.getFragment());
                Assert.assertEquals(ComputationMode.GIVEN, modAtt.getMode());
            }

            if ("CRS".equals(attModel.getName())) {
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.STRING, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertFalse(attModel.isFacetable());
                Assert.assertFalse(attModel.isOptional());
                Assert.assertFalse(attModel.isQueryable());
                Assert.assertNotNull(attModel.getRestriction());
                Assert.assertTrue(attModel.getRestriction() instanceof EnumerationRestriction);
                final EnumerationRestriction er = (EnumerationRestriction) attModel.getRestriction();
                final int expectedErSize = 3;
                Assert.assertEquals(expectedErSize, er.getAcceptableValues().size());
                Assert.assertTrue(er.getAcceptableValues().contains("Earth"));
                Assert.assertTrue(er.getAcceptableValues().contains("Mars"));
                Assert.assertTrue(er.getAcceptableValues().contains("Venus"));

                Assert.assertEquals("GEO", attModel.getFragment().getName());

                Assert.assertEquals(ComputationMode.GIVEN, modAtt.getMode());
            }

            if ("GEOMETRY".equals(attModel.getName())) {
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.GEOMETRY, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertFalse(attModel.isFacetable());
                Assert.assertFalse(attModel.isOptional());
                Assert.assertFalse(attModel.isQueryable());
                Assert.assertNull(attModel.getRestriction());

                Assert.assertEquals("GEO", attModel.getFragment().getName());

                Assert.assertEquals(ComputationMode.GIVEN, modAtt.getMode());
            }

            if ("Phone".equals(attModel.getName())) {
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.STRING, attModel.getType());
                Assert.assertTrue(attModel.isAlterable());
                Assert.assertTrue(attModel.isFacetable());
                Assert.assertTrue(attModel.isOptional());
                Assert.assertTrue(attModel.isQueryable());
                Assert.assertNotNull(attModel.getRestriction());

                Assert.assertNotNull(attModel.getRestriction());
                Assert.assertTrue(attModel.getRestriction() instanceof PatternRestriction);
                final PatternRestriction pr = (PatternRestriction) attModel.getRestriction();
                Assert.assertEquals("[0-9 ]{10}", pr.getPattern());

                Assert.assertEquals("Contact", attModel.getFragment().getName());

                Assert.assertEquals(ComputationMode.FROM_DESCENDANTS, modAtt.getMode());
            }
        }
    }
}
