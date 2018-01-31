/**
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.plugin.AbstractDataObjectComputePlugin;
import fr.cnes.regards.modules.entities.plugin.CountPlugin;
import fr.cnes.regards.modules.entities.plugin.IntSumComputePlugin;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
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

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepos;

    @Autowired
    private IPluginService pluginService;

    /**
     * Attribute endpoint
     */
    private final String apiAttribute = "/{pAttributeId}";

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Before
    public void setUp() throws ModuleException {
        datasetRepository.deleteAll();
        entityRepos.deleteAll();
        modelAttributeRepository.deleteAll();
        attributeModelRepository.deleteAll();
        modelRepository.deleteAll();
        fragmentRepository.deleteAll();
    }

    private Model createModel(String pName) {
        return createModel(pName, EntityType.COLLECTION);
    }

    private Model createModel(String pName, EntityType type) {
        final Model mod = new Model();
        mod.setName("model" + pName);
        mod.setType(type);
        return modelRepository.save(mod);
    }

    private AttributeModel createAttribute(String pName) throws ModuleException {
        final AttributeModel att = AttributeModelBuilder.build("att" + pName, AttributeType.STRING, "ForTests").get();
        return attributeModelService.addAttribute(att, false);
    }

    private ModelAttrAssoc createModelAttribute(AttributeModel pAtt, Model pModel) {
        final ModelAttrAssoc modAtt = new ModelAttrAssoc(pAtt, pModel);
        return modelAttributeRepository.save(modAtt);
    }

    /**
     * Generates a standard List of ResultMatchers
     *
     * @param pAtt The AttributeModel
     * @param pMod The Model
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
     * @throws ModuleException if attribute can't be created
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
        performDefaultPost(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING, modAtt,
                           expectations, "Attribute should be binded", mod.getName());
    }

    /**
     * List all of a model's attributes
     *
     * @throws ModuleException if attribute can't be created
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

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING, expectations,
                          "All attributes should be listed", mod.getName());

    }

    /**
     * Get a ModelAttribute from his id
     *
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void getModelAttribute() throws ModuleException {
        final String name = "GMA";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final ModelAttrAssoc modAtt = createModelAttribute(att, mod);

        // Define expectations
        final List<ResultMatcher> expectations = defaultExpectations(att, mod);

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING + apiAttribute,
                          expectations, "Should return an attribute", mod.getName(), modAtt.getId());
    }

    @Test
    public void testGetMappingForComputedAttribute() throws ModuleException {
        // lets add a package where we know there is plugin to get some results
        pluginService.addPluginPackage(CountPlugin.class.getPackage().getName());
        List<PluginParameter> params = PluginParametersFactory.build()
                .addParameter(AbstractDataObjectComputePlugin.PARAMETER_ATTRIBUTE_NAME, "toto").getParameters();
        PluginConfiguration confWithUnknownParameter = PluginUtils
                .getPluginConfiguration(params, IntSumComputePlugin.class,
                                        Lists.newArrayList(IntSumComputePlugin.class.getPackage().getName()));
        pluginService.savePluginConfiguration(confWithUnknownParameter);
        List<ResultMatcher> expectations = Lists.newArrayList();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(AttributeType.values().length)));
        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.COMPUTATION_TYPE_MAPPING,
                          expectations, "Should return mappings possible for computed attribute");
    }

    @Test
    public void getModelAttributeForCollections() throws ModuleException {
        final List<ResultMatcher> expectations = new ArrayList<>();
        List<ModelAttrAssoc> shouldBe = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Model mod = createModel("GMA" + i, getEntityType(i));
            AttributeModel att = createAttribute(mod.getName());
            shouldBe.add(createModelAttribute(att, mod));
        }
        List<ModelAttrAssoc> shouldBeCollections = shouldBe.stream()
                .filter(item -> item.getModel().getType().equals(EntityType.COLLECTION)).collect(Collectors.toList());
        List<ModelAttrAssoc> shouldBeData = shouldBe.stream()
                .filter(item -> item.getModel().getType().equals(EntityType.DATA)).collect(Collectors.toList());

        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$").isArray());
        expectations.add(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(shouldBeCollections.size())));
        expectations.add(MockMvcResultMatchers.content().json(gson(shouldBeCollections), false));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.ASSOCS_MAPPING + "?type="
                + EntityType.COLLECTION, expectations, "Should return model attribute association");

        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$").isArray());
        expectations.add(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(shouldBeData.size())));
        expectations.add(MockMvcResultMatchers.content().json(gson(shouldBeData), false));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.ASSOCS_MAPPING + "?type="
                + EntityType.DATA, expectations, "Should return model attribute association");

        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$").isArray());
        expectations.add(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(shouldBe.size())));
        expectations.add(MockMvcResultMatchers.content().json(gson(shouldBe), false));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.ASSOCS_MAPPING, expectations,
                          "Should return model attribute association");
    }

    private EntityType getEntityType(int pI) {
        int mod = pI % 4;
        switch (mod) {
            case 0:
                return EntityType.COLLECTION;
            case 1:
                return EntityType.DATA;
            case 2:
                return EntityType.DATASET;
            case 3:
                return EntityType.DOCUMENT;
            default:
                throw new RuntimeException("learn to dev!");
        }
    }

    /**
     * Update a ModelAttribute from his id
     *
     * @throws ModuleException if attribute can't be created
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

        performDefaultPut(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING + apiAttribute,
                          modAtt, expectations, "Should update the model attribute", mod.getName(), modAtt.getId());
    }

    /**
     * Remove a ModelAttribute
     *
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void removeModelAttribute() throws ModuleException {
        final String name = "RmNA";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final ModelAttrAssoc modAtt = createModelAttribute(att, mod);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultDelete(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING
                + apiAttribute, expectations, "Model should be deleted", mod.getName(), modAtt.getId());
    }

    /**
     * Bind a fragment to a Model
     *
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void bindFragment() throws ModuleException {
        final String name = "PostFrag";
        final Model mod = createModel(name);

        final Fragment frag = Fragment.buildFragment("testFrag", null);
        fragmentRepository.save(frag);

        final AttributeModel att = AttributeModelBuilder.build("att" + name, AttributeType.STRING, "ForTests")
                .fragment(frag).get();
        final AttributeModel att2 = AttributeModelBuilder.build("att2" + name, AttributeType.STRING, "ForTests")
                .fragment(frag).get();
        attributeModelService.addAttribute(att, false);
        attributeModelService.addAttribute(att2, false);

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

        performDefaultPost(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING
                + ModelAttrAssocController.FRAGMENT_BIND_MAPPING, frag, expectations, "Should bind fragment",
                           mod.getName());
    }

    /**
     * Unbind a fragment to a Model
     *
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void unbindFragment() throws ModuleException {
        final String name = "DeleteFrag";
        final Model mod = createModel(name);

        final Fragment frag = Fragment.buildFragment(name, null);
        fragmentRepository.save(frag);

        final AttributeModel att = AttributeModelBuilder.build("att" + name, AttributeType.STRING, "ForTests")
                .fragment(frag).get();
        final AttributeModel att2 = AttributeModelBuilder.build("att2" + name, AttributeType.STRING, "ForTests")
                .fragment(frag).get();
        attributeModelService.addAttribute(att, false);
        attributeModelService.addAttribute(att2, false);

        modelAttributeService.bindNSAttributeToModel(mod.getName(), frag);

        final List<ModelAttrAssoc> modelAttributes = modelAttributeService.getModelAttrAssocs(name);

        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultDelete(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING
                + ModelAttrAssocController.FRAGMENT_UNBIND_MAPPING, expectations,
                             "Fragment's attributes should be deleted", mod.getName(), frag.getId());

        expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNotFound());

        for (ModelAttrAssoc modAtt : modelAttributes) {
            performDefaultGet(ModelAttrAssocController.TYPE_MAPPING + ModelAttrAssocController.TYPE_MAPPING
                    + apiAttribute, expectations, "ModelAttribute shouldn't exist anymore", mod.getId(),
                              modAtt.getId());
        }
    }
}
