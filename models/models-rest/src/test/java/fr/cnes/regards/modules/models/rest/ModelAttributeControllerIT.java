/**
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;

/**
 * Test {@link ModelAttrAssoc} API
 *
 * @author Maxime Bouveron
 */
@MultitenantTransactional
public class ModelAttributeControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelAttributeControllerIT.class);

    /**
     * Model Repository
     */
    @Autowired
    private IModelRepository modelRepository;

    /**
     * AttributeModel service
     */
    @Autowired
    private IAttributeModelService attributeModelService;

    /**
     * AttributeModel Repository
     */
    @Autowired
    private IAttributeModelRepository attributeModelRepository;

    /**
     * ModelAttribute Repository
     */
    @Autowired
    private IModelAttrAssocRepository modelAttributeRepository;

    /**
     * Fragment Repository
     */
    @Autowired
    private IFragmentRepository fragmentRepository;

    /**
     * ModelAttribute Service
     */
    @Autowired
    private IModelAttrAssocService modelAttributeService;

    /**
     * Attribute endpoint
     */
    private final String apiAttribute = "/{pAttributeId}";

    /**
     * Fragment endpoint
     */
    private final String fragmentApi = "/fragments/{pFragmentId}";

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Before
    public void setUp() throws ModuleException {
        modelAttributeRepository.deleteAll();
        attributeModelRepository.deleteAll();
        modelRepository.deleteAll();
        fragmentRepository.deleteAll();
    }

    private Model createModel(String pName) {
        final Model mod = new Model();
        mod.setName("model" + pName);
        mod.setType(EntityType.COLLECTION);
        return modelRepository.save(mod);
    }

    private AttributeModel createAttribute(String pName) throws ModuleException {
        final AttributeModel att = AttributeModelBuilder.build("att" + pName, AttributeType.STRING).get();
        return attributeModelService.addAttribute(att);
    }

    private ModelAttrAssoc createModelAttribute(AttributeModel pAtt, Model pModel) {
        final ModelAttrAssoc modAtt = new ModelAttrAssoc(pAtt, pModel);
        return modelAttributeRepository.save(modAtt);
    }

    /**
     * Generates a standard List of ResultMatchers
     *
     * @param pAtt
     *            The AttributeModel
     * @param pMod
     *            The Model
     * @return The List of ResultMatchers
     */
    private List<ResultMatcher> defaultExpectations(AttributeModel pAtt, Model pMod) {

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Test attribute
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.attribute.id").value(pAtt.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.attribute.name").value(pAtt.getName()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.attribute.type").value(pAtt.getType().toString()));

        // Test Model
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.model.id").value(pMod.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.model.name").value(pMod.getName()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.content.model.type").value(pMod.getType().toString()));

        return expectations;
    }

    /**
     * Bind an attribute to a model
     *
     * @throws ModuleException
     *             if attribute can't be created
     */
    @Test
    public void bindFirstAttribute() throws ModuleException {
        final String name = "PostAM";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final ModelAttrAssoc modAtt = new ModelAttrAssoc(att, mod);

        // Define expectations
        final List<ResultMatcher> expectations = defaultExpectations(att, mod);

        // Perform request
        performDefaultPost(ModelAttrAssocController.TYPE_MAPPING, modAtt, expectations, "Attribute should be binded",
                           mod.getId());
    }

    /**
     * List all of a model's attributes
     *
     * @throws ModuleException
     *             if attribute can't be created
     */
    @Test
    public void listAllAttributes() throws ModuleException {
        final String name = "LsAM";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final AttributeModel att2 = createAttribute(name + 2);
        createModelAttribute(att, mod);
        createModelAttribute(att2, mod);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        expectations.add(MockMvcResultMatchers.jsonPath("$.[0].content.attribute.id").value(att.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0].content.attribute.name").value(att.getName()));
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.[0].content.attribute.type").value(att.getType().toString()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0]content.model.id").value(mod.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0]content.model.name").value(mod.getName()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0]content.model.type").value(mod.getType().toString()));

        expectations.add(MockMvcResultMatchers.jsonPath("$.[1].content.attribute.id").value(att2.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[1].content.attribute.name").value(att2.getName()));
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.[1].content.attribute.type").value(att2.getType().toString()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[1]content.model.id").value(mod.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[1]content.model.name").value(mod.getName()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[1]content.model.type").value(mod.getType().toString()));

        performDefaultGet(ModelAttrAssocController.TYPE_MAPPING, expectations, "All attributes should be listed",
                          mod.getId());

    }

    /**
     * Get a ModelAttribute from his id
     *
     * @throws ModuleException
     *             if attribute can't be created
     */
    @Test
    public void getModelAttribute() throws ModuleException {
        final String name = "GMA";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final ModelAttrAssoc modAtt = createModelAttribute(att, mod);

        // Define expectations
        final List<ResultMatcher> expectations = defaultExpectations(att, mod);

        performDefaultGet(ModelAttrAssocController.TYPE_MAPPING + apiAttribute, expectations,
                          "Should return an attribute", mod.getId(), modAtt.getId());
    }

    /**
     * Update a ModelAttribute from his id
     *
     * @throws ModuleException
     *             if attribute can't be created
     */
    @Test
    public void updateModelAttribute() throws ModuleException {
        final String name = "UpMA";
        final Model mod = createModel(name);

        final AttributeModel att = createAttribute(name);
        final AttributeModel newAtt = createAttribute("new" + name);

        final ModelAttrAssoc modAtt = createModelAttribute(att, mod);

        modAtt.setAttribute(newAtt);

        // Define expectations
        final List<ResultMatcher> expectations = defaultExpectations(newAtt, mod);

        performDefaultPut(ModelAttrAssocController.TYPE_MAPPING + apiAttribute, modAtt, expectations,
                          "Should update the model attribute", mod.getId(), modAtt.getId());
    }

    /**
     * Remove a ModelAttribute
     *
     * @throws ModuleException
     *             if attribute can't be created
     */
    @Test
    public void removeModelAttribute() throws ModuleException {
        final String name = "RmNA";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final ModelAttrAssoc modAtt = createModelAttribute(att, mod);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultDelete(ModelAttrAssocController.TYPE_MAPPING + apiAttribute, expectations,
                             "Model should be deleted", mod.getId(), modAtt.getId());
    }

    /**
     * Bind a fragment to a Model
     *
     * @throws ModuleException
     *             if attribute can't be created
     */
    @Test
    public void bindFragment() throws ModuleException {
        final String name = "PostFrag";
        final Model mod = createModel(name);

        final Fragment frag = Fragment.buildFragment("testFrag", null);
        fragmentRepository.save(frag);

        final AttributeModel att = AttributeModelBuilder.build("att" + name, AttributeType.STRING).fragment(frag).get();
        final AttributeModel att2 = AttributeModelBuilder.build("att2" + name, AttributeType.STRING).fragment(frag)
                .get();
        attributeModelService.addAttribute(att);
        attributeModelService.addAttribute(att2);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        expectations.add(MockMvcResultMatchers.jsonPath("$.[0].content.attribute.id").value(att.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0].content.attribute.name").value(att.getName()));
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.[0].content.attribute.type").value(att.getType().toString()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0]content.model.id").value(mod.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0]content.model.name").value(mod.getName()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0]content.model.type").value(mod.getType().toString()));

        expectations.add(MockMvcResultMatchers.jsonPath("$.[1].content.attribute.id").value(att2.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[1].content.attribute.name").value(att2.getName()));
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.[1].content.attribute.type").value(att2.getType().toString()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[1]content.model.id").value(mod.getId().intValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[1]content.model.name").value(mod.getName()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[1]content.model.type").value(mod.getType().toString()));

        performDefaultPost(ModelAttrAssocController.TYPE_MAPPING + fragmentApi, null, expectations,
                           "Should bind fragment", mod.getId(), frag.getId());
    }

    /**
     * Unbind a fragment to a Model
     *
     * @throws ModuleException
     *             if attribute can't be created
     */
    @Test
    public void unbindFragment() throws ModuleException {
        final String name = "DeleteFrag";
        final Model mod = createModel(name);

        final Fragment frag = Fragment.buildFragment(name, null);
        fragmentRepository.save(frag);

        final AttributeModel att = AttributeModelBuilder.build("att" + name, AttributeType.STRING).fragment(frag).get();
        final AttributeModel att2 = AttributeModelBuilder.build("att2" + name, AttributeType.STRING).fragment(frag)
                .get();
        attributeModelService.addAttribute(att);
        attributeModelService.addAttribute(att2);

        modelAttributeService.bindNSAttributeToModel(mod.getId(), frag.getId());

        final List<ModelAttrAssoc> modelAttributes = modelAttributeService.getModelAttrAssocs(mod.getId());

        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultDelete(ModelAttrAssocController.TYPE_MAPPING + fragmentApi, expectations,
                             "Fragment's attributes should be deleted", mod.getId(), frag.getId());

        expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        for (ModelAttrAssoc modAtt : modelAttributes) {
            performDefaultGet(ModelAttrAssocController.TYPE_MAPPING + apiAttribute, expectations,
                              "ModelAttribute shouldn't exist anymore", mod.getId(), modAtt.getId());
        }
    }
}
