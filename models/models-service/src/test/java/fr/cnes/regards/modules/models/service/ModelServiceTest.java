/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.EntityUnexpectedIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.dao.IModelAttributeRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.exception.FragmentAttributeException;

/**
 * @author Marc Sordi
 *
 */
public class ModelServiceTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceTest.class);

    /**
     * Sample model name
     */
    private static final String MODEL_NAME = "model";

    /**
     * Sample attribute model name
     */
    private static final String ATT_MOD_NAME = "attmod";

    /**
     * Model repository
     */
    private IModelRepository mockModelR;

    /**
     * Model attribute repository
     */
    private IModelAttributeRepository mockModelAttR;

    /**
     * Attribute model service
     */
    private IAttributeModelService mockAttModelS;

    /**
     * Model and model attribute services
     */
    private ModelService modelService;

    @Before
    public void beforeTest() {
        mockModelR = Mockito.mock(IModelRepository.class);
        mockModelAttR = Mockito.mock(IModelAttributeRepository.class);
        mockAttModelS = Mockito.mock(IAttributeModelService.class);
        modelService = new ModelService(mockModelR, mockModelAttR, mockAttModelS);
    }

    @Test(expected = EntityUnexpectedIdentifierException.class)
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Test unexpected model creation")
    public void createUnexpectedModelTest() throws ModuleException {
        final Model model = new Model();
        model.setId(1L);
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        modelService.createModel(model);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Test model creation with conflict")
    public void createAlreadyExistsModelTest() throws ModuleException {
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        Mockito.when(mockModelR.findByName(MODEL_NAME)).thenReturn(model);

        modelService.createModel(model);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Test model creation")
    public void createModelTest() throws ModuleException {
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        Mockito.when(mockModelR.findByName(MODEL_NAME)).thenReturn(null);
        Mockito.when(mockModelR.save(model)).thenReturn(model);

        Assert.assertNotNull(modelService.createModel(model));
    }

    @Test(expected = EntityNotFoundException.class)
    public void getUnknownModelTest() throws ModuleException {
        final Long modelId = 1L;

        Mockito.when(mockModelR.exists(modelId)).thenReturn(false);

        modelService.getModel(modelId);
    }

    @Test
    public void getModelTest() throws ModuleException {
        final Long modelId = 1L;
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(true);
        Mockito.when(mockModelR.findOne(modelId)).thenReturn(model);

        Assert.assertNotNull(modelService.getModel(modelId));
    }

    @Test(expected = EntityNotIdentifiableException.class)
    public void updateUnexpectedModelTest() throws ModuleException {
        final Long modelId = 1L;
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        modelService.updateModel(modelId, model);
    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void updateInconsistentModelTest() throws ModuleException {
        final Long modelId = 1L;
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        model.setId(2L);

        modelService.updateModel(modelId, model);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateUnknownModelTest() throws ModuleException {
        final Long modelId = 1L;
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        model.setId(modelId);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(false);

        modelService.updateModel(modelId, model);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Test model update")
    public void updateModelTest() throws ModuleException {
        final Long modelId = 1L;
        final Model model = new Model();
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);
        model.setId(modelId);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(true);
        Mockito.when(mockModelR.save(model)).thenReturn(model);

        Assert.assertNotNull(modelService.updateModel(modelId, model));
    }

    @Test
    public void deleteModelTest() throws ModuleException {
        final Long modelId = 1L;

        Mockito.when(mockModelR.exists(modelId)).thenReturn(true);
        final IModelService spy = Mockito.spy(modelService);
        Mockito.doNothing().when(spy).deleteModel(modelId);

        modelService.deleteModel(modelId);
    }

    /**
     * Do not bind an attribute that is part of fragment
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test(expected = FragmentAttributeException.class)
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Test error occurs binding attribute that is part of a fragment")
    public void bindAttributeToModelTest() throws ModuleException {

        final Long modelId = 1L;
        final Model model = new Model();
        model.setId(modelId);
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        final Fragment frag = Fragment.buildFragment("FRAG", null);
        final Long attId = 10L;
        final AttributeModel attModel = AttributeModelBuilder.build(ATT_MOD_NAME, AttributeType.STRING).fragment(frag)
                .withId(attId).get();

        final ModelAttribute modAtt = new ModelAttribute(attModel, model);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(true);
        Mockito.when(mockModelR.findOne(modelId)).thenReturn(model);
        Mockito.when(mockAttModelS.isFragmentAttribute(attId)).thenReturn(true);

        modelService.bindAttributeToModel(modelId, modAtt);
    }

    // TODO do not rebind an attribute

    /**
     * Do not unbind an attribute that is part of fragment
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test(expected = FragmentAttributeException.class)
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Test error occurs unbinding attribute that is part of a fragment")
    public void unbindAttributeFromModelTest() throws ModuleException {

        final Long modelId = 1L;
        final Model model = new Model();
        model.setId(modelId);
        model.setName(MODEL_NAME);
        model.setType(EntityType.COLLECTION);

        final Fragment frag = Fragment.buildFragment("FR2AG", null);
        final Long attId = 10L;
        final AttributeModel attModel = AttributeModelBuilder.build(ATT_MOD_NAME, AttributeType.STRING).fragment(frag)
                .withId(attId).withPatternRestriction(".*");

        final Long modAttId = 10L;
        final ModelAttribute modAtt = new ModelAttribute(attModel, model);
        modAtt.setId(modAttId);

        Mockito.when(mockModelR.exists(modelId)).thenReturn(true);
        Mockito.when(mockModelR.findOne(modelId)).thenReturn(model);
        Mockito.when(mockModelAttR.findOne(modAttId)).thenReturn(modAtt);
        Mockito.when(mockAttModelS.isFragmentAttribute(attId)).thenReturn(true);

        modelService.unbindAttributeFromModel(modelId, modAttId);
    }

    /**
     * Test model export
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test
    public void exportModelTest() throws ModuleException {

        final Long modelId = 1L;
        final Model model = new Model();
        model.setId(modelId);
        model.setName("sample");
        model.setDescription("Model description");
        model.setType(EntityType.COLLECTION);

        final List<ModelAttribute> modAtts = new ArrayList<>();

        // Attribute #1 in default fragment
        AttributeModel attMod = AttributeModelBuilder.build("att_string", AttributeType.STRING)
                .fragment(Fragment.buildDefault()).withoutRestriction();
        ModelAttribute modAtt = new ModelAttribute(attMod, model);
        modAtts.add(modAtt);

        // Attribute #2 in default fragment
        attMod = AttributeModelBuilder.build("att_boolean", AttributeType.BOOLEAN).fragment(Fragment.buildDefault())
                .withoutRestriction();
        modAtt = new ModelAttribute(attMod, model);
        modAtts.add(modAtt);

        // Geo fragment
        final Fragment geo = Fragment.buildFragment("GEO", "Geographic information");

        // Attribute #3 in geo fragment
        attMod = AttributeModelBuilder.build("CRS", AttributeType.STRING).fragment(geo)
                .withEnumerationRestriction("Earth", "Mars", "Venus");
        modAtt = new ModelAttribute(attMod, model);
        modAtts.add(modAtt);

        // Attribute #4 in geo fragment
        attMod = AttributeModelBuilder.build("GEOMETRY", AttributeType.GEOMETRY).fragment(geo).withoutRestriction();
        modAtt = new ModelAttribute(attMod, model);
        modAtts.add(modAtt);

        // Geo fragment
        final Fragment contact = Fragment.buildFragment("Contact", "Contact information");

        // Attribute #5 in contact fragment
        attMod = AttributeModelBuilder.build("Phone", AttributeType.STRING).fragment(contact)
                .withPatternRestriction("[0-9 ]{10}");
        modAtt = new ModelAttribute(attMod, model);
        modAtts.add(modAtt);

        Mockito.when(mockModelR.findOne(modelId)).thenReturn(model);
        Mockito.when(mockModelAttR.findByModelId(modelId)).thenReturn(modAtts);

        try {
            final OutputStream output = Files.newOutputStream(Paths.get("target", model.getName() + ".xml"));
            modelService.exportModel(modelId, output);
        } catch (IOException e) {
            LOGGER.debug("Cannot export fragment");
        }
    }

}
