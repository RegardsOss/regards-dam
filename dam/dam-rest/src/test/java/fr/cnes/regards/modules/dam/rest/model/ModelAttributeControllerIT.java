/**
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.framework.test.integration.ConstrainedFields;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.plugin.entities.AbstractDataObjectComputePlugin;
import fr.cnes.regards.modules.dam.plugin.entities.IntSumComputePlugin;
import fr.cnes.regards.modules.model.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.model.dao.IFragmentRepository;
import fr.cnes.regards.modules.model.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.model.dao.IModelRepository;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;
import fr.cnes.regards.modules.model.rest.ModelAttrAssocController;
import fr.cnes.regards.modules.model.service.IAttributeModelService;
import fr.cnes.regards.modules.model.service.IModelAttrAssocService;

/**
 * Test {@link ModelAttrAssoc} API
 * @author Maxime Bouveron
 */
@TestPropertySource(locations = { "classpath:test.properties" },
        properties = { "spring.jpa.properties.hibernate.default_schema=models_rest" })
public class ModelAttributeControllerIT extends AbstractRegardsIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelAttributeControllerIT.class);

    /**
     * Attribute endpoint
     */
    private static final String ATTRIBUTE_ID = "/{attributeId}";

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
     * ModelAttribute Repositorytrue
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
    private IAbstractEntityRepository<AbstractEntity<?>> entityRepos;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    protected IRuntimeTenantResolver runtimetenantResolver;

    public static List<FieldDescriptor> documentBody(boolean creation, String prefix) {
        String prefixPath = Strings.isNullOrEmpty(prefix) ? "" : prefix + ".";
        ConstrainedFields constrainedFields = new ConstrainedFields(ModelAttrAssoc.class);
        List<FieldDescriptor> descriptors = new ArrayList<>();
        if (!creation) {
            descriptors
                    .add(constrainedFields.withPath(prefixPath + "id", "id", "Model attribute association identifier"));
        }
        descriptors.add(constrainedFields.withPath(prefixPath + "attribute", "attribute",
                                                   "Model attribute association attribute"));
        descriptors.addAll(AttributeModelControllerIT.documentBody(false, prefixPath + "attribute"));
        descriptors.add(constrainedFields.withPath(prefixPath + "model", "model", "Model attribute association model"));
        descriptors.addAll(ModelControllerIT.documentBody(false, prefixPath + "model"));
        descriptors.add(constrainedFields
                .withPath(prefixPath + "computationConf", "computationConf", "Computation plugin configuration",
                          "Should respect PluginConfiguration structure")
                .type(JSON_OBJECT_TYPE).optional());
        descriptors.add(constrainedFields
                .withPath(prefixPath + "pos", "pos", "Position (allows to sort attribute in model)",
                          "Should be a whole number. Defaults to 0")
                .type(JSON_NUMBER_TYPE).optional());
        return descriptors;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Before
    public void setUp() throws ModuleException {
        runtimetenantResolver.forceTenant(getDefaultTenant());
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
        final AttributeModel att = AttributeModelBuilder.build("att" + pName, PropertyType.STRING, "ForTests").get();
        return attributeModelService.addAttribute(att, false);
    }

    private ModelAttrAssoc createModelAttribute(AttributeModel pAtt, Model pModel) {
        final ModelAttrAssoc modAtt = new ModelAttrAssoc(pAtt, pModel);
        return modelAttributeRepository.save(modAtt);
    }

    /**
     * Generates a standard List of ResultMatchers
     * @param pAtt The AttributeModel
     * @param pMod The Model
     */
    private void defaultExpectations(RequestBuilderCustomizer customizer, AttributeModel pAtt, Model pMod) {

        // Define expectations
        customizer.expect(MockMvcResultMatchers.status().isOk());

        // Test attribute
        customizer.expect(MockMvcResultMatchers.jsonPath("$.content.attribute.id").value(pAtt.getId().intValue()));
        customizer.expect(MockMvcResultMatchers.jsonPath("$.content.attribute.name").value(pAtt.getName()));
        customizer.expect(MockMvcResultMatchers.jsonPath("$.content.attribute.type").value(pAtt.getType().toString()));

        // Test Model
        customizer.expect(MockMvcResultMatchers.jsonPath("$.content.model.id").value(pMod.getId().intValue()));
        customizer.expect(MockMvcResultMatchers.jsonPath("$.content.model.name").value(pMod.getName()));
        customizer.expect(MockMvcResultMatchers.jsonPath("$.content.model.type").value(pMod.getType().toString()));
    }

    /**
     * Bind an attribute to a model
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void bindFirstAttribute() throws ModuleException {
        final String name = "PostAM";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final ModelAttrAssoc modAtt = new ModelAttrAssoc(att, mod);

        // Define expectations
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        defaultExpectations(requestBuilderCustomizer, att, mod);

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("Model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE))));

        requestBuilderCustomizer.document(PayloadDocumentation.requestFields(documentBody(true, "")));
        requestBuilderCustomizer.document(PayloadDocumentation.responseFields(documentBody(false, "content")));

        // Perform request
        performDefaultPost(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING, modAtt,
                           requestBuilderCustomizer, "Attribute should be binded", mod.getName());
    }

    /**
     * List all of a model's attributes
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
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());

        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.attribute.id")
                .value(att.getId().intValue()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.attribute.name")
                .value(att.getName()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.attribute.type")
                .value(att.getType().toString()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.model.id")
                .value(mod.getId().intValue()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.model.name")
                .value(mod.getName()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.model.type")
                .value(mod.getType().toString()));

        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.attribute.id")
                .value(att2.getId().intValue()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.attribute.name")
                .value(att2.getName()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.attribute.type")
                .value(att2.getType().toString()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.model.id")
                .value(mod.getId().intValue()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.model.name")
                .value(mod.getName()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.model.type")
                .value(mod.getType().toString()));

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("Model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE))));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING,
                          requestBuilderCustomizer, "All attributes should be listed", mod.getName());

    }

    /**
     * Get a ModelAttribute from his id
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void getModelAttribute() throws ModuleException {
        final String name = "GMA";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final ModelAttrAssoc modAtt = createModelAttribute(att, mod);

        // Define expectations
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        defaultExpectations(requestBuilderCustomizer, att, mod);

        requestBuilderCustomizer.document(PayloadDocumentation.responseFields(documentBody(false, "content")));

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("Model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE)),
                                RequestDocumentation.parameterWithName("attributeId")
                                        .description("Attribute identifier").attributes(Attributes
                                                .key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_NUMBER_TYPE))));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING + ATTRIBUTE_ID,
                          requestBuilderCustomizer, "Should return an attribute", mod.getName(), modAtt.getId());
    }

    @Test
    public void testGetMappingForComputedAttribute() throws ModuleException {
        // lets add a package where we know there is plugin to get some results
        Set<IPluginParam> params = IPluginParam
                .set(IPluginParam.build(AbstractDataObjectComputePlugin.PARAMETER_ATTRIBUTE_NAME, "toto"),
                     IPluginParam.build(AbstractDataObjectComputePlugin.RESULT_ATTRIBUTE_NAME, "titi"));

        PluginConfiguration conf = PluginConfiguration.build(IntSumComputePlugin.class, "intcount", params);
        conf = pluginService.savePluginConfiguration(conf);
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer
                .expect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(PropertyType.values().length)));
        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.COMPUTATION_TYPE_MAPPING,
                          requestBuilderCustomizer, "Should return mappings possible for computed attribute");
    }

    @Test
    public void getModelAttributeForCollections() throws ModuleException {
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

        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer.expect(MockMvcResultMatchers.jsonPath("$").isArray());
        requestBuilderCustomizer
                .expect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(shouldBeCollections.size())));
        requestBuilderCustomizer.expect(MockMvcResultMatchers.content().json(gson(shouldBeCollections), false));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.ASSOCS_MAPPING + "?type="
                + EntityType.COLLECTION, requestBuilderCustomizer, "Should return model attribute association");

        requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer.expect(MockMvcResultMatchers.jsonPath("$").isArray());
        requestBuilderCustomizer.expect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(shouldBeData.size())));
        requestBuilderCustomizer.expect(MockMvcResultMatchers.content().json(gson(shouldBeData), false));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.ASSOCS_MAPPING + "?type="
                + EntityType.DATA, requestBuilderCustomizer, "Should return model attribute association");

        requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer.expect(MockMvcResultMatchers.jsonPath("$").isArray());
        requestBuilderCustomizer.expect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(shouldBe.size())));
        requestBuilderCustomizer.expect(MockMvcResultMatchers.content().json(gson(shouldBe), false));

        requestBuilderCustomizer
                .document(RequestDocumentation.requestParameters(RequestDocumentation.parameterWithName("type")
                        .description("Model type for which we want the associations")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE),
                                    Attributes.key(RequestBuilderCustomizer.PARAM_CONSTRAINTS)
                                            .value("Available values: " + Arrays.stream(EntityType.values())
                                                    .map(type -> type.name()).collect(Collectors.joining(", "))))
                        .optional()));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.ASSOCS_MAPPING,
                          requestBuilderCustomizer, "Should return model attribute association");
    }

    private EntityType getEntityType(int pI) {
        int mod = pI % 3;
        switch (mod) {
            case 0:
                return EntityType.COLLECTION;
            case 1:
                return EntityType.DATA;
            case 2:
                return EntityType.DATASET;
            default:
                throw new RuntimeException("learn to dev!");
        }
    }

    /**
     * Update a ModelAttribute from his id
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
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        defaultExpectations(requestBuilderCustomizer, newAtt, mod);

        requestBuilderCustomizer.document(PayloadDocumentation.requestFields(documentBody(false, "")));
        requestBuilderCustomizer.document(PayloadDocumentation.responseFields(documentBody(false, "content")));

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("Model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE)),
                                RequestDocumentation.parameterWithName("attributeId")
                                        .description("Attribute identifier").attributes(Attributes
                                                .key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_NUMBER_TYPE))));

        performDefaultPut(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING + ATTRIBUTE_ID,
                          modAtt, requestBuilderCustomizer, "Should update the model attribute", mod.getName(),
                          modAtt.getId());
    }

    /**
     * Remove a ModelAttribute
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void removeModelAttribute() throws ModuleException {
        final String name = "RmNA";
        final Model mod = createModel(name);
        final AttributeModel att = createAttribute(name);
        final ModelAttrAssoc modAtt = createModelAttribute(att, mod);

        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isNoContent());

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("Model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE)),
                                RequestDocumentation.parameterWithName("attributeId")
                                        .description("Attribute identifier").attributes(Attributes
                                                .key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_NUMBER_TYPE))));

        performDefaultDelete(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING
                + ATTRIBUTE_ID, requestBuilderCustomizer, "Model should be deleted", mod.getName(), modAtt.getId());
    }

    /**
     * Bind a fragment to a Model
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void bindFragment() throws ModuleException {
        final String name = "PostFrag";
        final Model mod = createModel(name);

        final Fragment frag = Fragment.buildFragment("testFrag", null);
        fragmentRepository.save(frag);

        final AttributeModel att = AttributeModelBuilder.build("att" + name, PropertyType.STRING, "ForTests")
                .fragment(frag).get();
        final AttributeModel att2 = AttributeModelBuilder.build("att2" + name, PropertyType.STRING, "ForTests")
                .fragment(frag).get();
        attributeModelService.addAttribute(att, false);
        attributeModelService.addAttribute(att2, false);

        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());

        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.attribute.id")
                .value(att.getId().intValue()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.attribute.name")
                .value(att.getName()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.attribute.type")
                .value(att.getType().toString()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.model.id")
                .value(mod.getId().intValue()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.model.name")
                .value(mod.getName()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att.getName() + "\')].content.model.type")
                .value(mod.getType().toString()));

        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.attribute.id")
                .value(att2.getId().intValue()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.attribute.name")
                .value(att2.getName()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.attribute.type")
                .value(att2.getType().toString()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.model.id")
                .value(mod.getId().intValue()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.model.name")
                .value(mod.getName()));
        requestBuilderCustomizer.expect(MockMvcResultMatchers
                .jsonPath("$.[?(@.content.attribute.name == \'" + att2.getName() + "\')].content.model.type")
                .value(mod.getType().toString()));

        requestBuilderCustomizer
                .document(PayloadDocumentation.requestFields(FragmentControllerIT.documentBody(false, "")));

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("Model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE))));

        performDefaultPost(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING
                + ModelAttrAssocController.FRAGMENT_BIND_MAPPING, frag, requestBuilderCustomizer,
                           "Should bind fragment", mod.getName());
    }

    /**
     * Unbind a fragment to a Model
     * @throws ModuleException if attribute can't be created
     */
    @Test
    public void unbindFragment() throws ModuleException {
        final String name = "DeleteFrag";
        final Model mod = createModel(name);

        final Fragment frag = Fragment.buildFragment(name, null);
        fragmentRepository.save(frag);

        final AttributeModel att = AttributeModelBuilder
                .build("att" + name, PropertyType.STRING, "For Tests Label looooooooooooooooooooooooooooong")
                .fragment(frag).get();
        final AttributeModel att2 = AttributeModelBuilder.build("att2" + name, PropertyType.STRING, "ForTests")
                .fragment(frag).get();
        attributeModelService.addAttribute(att, false);
        attributeModelService.addAttribute(att2, false);

        modelAttributeService.bindNSAttributeToModel(mod.getName(), frag);

        modelAttributeService.getModelAttrAssocs(name);

        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isNoContent());

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("Model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE)),
                                RequestDocumentation.parameterWithName("fragmentId").description("Fragment identifier")
                                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE)
                                                .value(JSON_NUMBER_TYPE))));

        performDefaultDelete(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING
                + ModelAttrAssocController.FRAGMENT_UNBIND_MAPPING, requestBuilderCustomizer,
                             "Fragment's attributes should be deleted", mod.getName(), frag.getId());
    }

}
