/*
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
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.ConstrainedFields;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;
import fr.cnes.regards.modules.model.rest.ModelController;
import fr.cnes.regards.modules.model.service.IAttributeModelService;
import fr.cnes.regards.modules.model.service.IModelAttrAssocService;
import fr.cnes.regards.modules.model.service.IModelService;

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

    public static List<FieldDescriptor> documentBody(boolean creation, String prefix) {
        String prefixPath = Strings.isNullOrEmpty(prefix) ? "" : prefix + ".";
        ConstrainedFields constrainedFields = new ConstrainedFields(Model.class);
        List<FieldDescriptor> descriptors = new ArrayList<>();
        if (!creation) {
            descriptors.add(constrainedFields.withPath(prefixPath + "id", "id", "model identifier"));
        }
        descriptors.add(constrainedFields.withPath(prefixPath + "name", "name", "model name"));
        descriptors.add(constrainedFields.withPath(prefixPath + "description", "description", "model description")
                .type(JSON_STRING_TYPE).optional());
        descriptors.add(constrainedFields.withPath(prefixPath + "version", "version", "model version")
                .type(JSON_STRING_TYPE).optional());
        descriptors.add(
                        constrainedFields
                                .withPath(prefixPath + "type", "type", "model type",
                                          "Available values: " + Arrays.stream(EntityType.values())
                                                  .map(type -> type.name()).collect(Collectors.joining(", ")))
                                .type(JSON_STRING_TYPE));
        // ignore links
        ConstrainedFields ignoreFields = new ConstrainedFields(Resource.class);
        descriptors.add(ignoreFields.withPath("links", "links", "hateoas links").optional().ignored());
        ignoreFields = new ConstrainedFields(Link.class);
        descriptors.add(ignoreFields.withPath("links[].rel", "rel", "hateoas links rel").optional().ignored());
        descriptors.add(ignoreFields.withPath("links[].href", "href", "hateoas links href").optional().ignored());
        return descriptors;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Test
    public void createEmptyModelTest() {

        final Model model = new Model();

        // Define expectations
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isUnprocessableEntity());

        performDefaultPost(ModelController.TYPE_MAPPING, model, requestBuilderCustomizer,
                           "Empty model shouldn't be created.");
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
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer.expect(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));

        requestBuilderCustomizer.document(PayloadDocumentation.requestFields(documentBody(true, "")));
        requestBuilderCustomizer.document(PayloadDocumentation.responseFields(documentBody(false, "content")));

        performDefaultPost(ModelController.TYPE_MAPPING, model, requestBuilderCustomizer,
                           "Consistent model should be created.");
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

        Model model = new Model();
        model.setName("EXPORT_MODEL");
        model.setDescription("Exported model");
        model.setType(EntityType.COLLECTION);
        modelService.createModel(model);

        // Attribute #1 in default fragment
        AttributeModel attMod = AttributeModelBuilder.build("att_string", PropertyType.STRING, "ForTests")
                .withoutRestriction();
        attributeModelService.addAttribute(attMod, false);

        ModelAttrAssoc modAtt = new ModelAttrAssoc();
        modAtt.setAttribute(attMod);
        modelAttributeService.bindAttributeToModel(model.getName(), modAtt);

        // Attribute #2 in default fragment
        attMod = AttributeModelBuilder.build("att_boolean", PropertyType.BOOLEAN, "ForTests").isAlterable()
                .withoutRestriction();
        attributeModelService.addAttribute(attMod, false);

        modAtt = new ModelAttrAssoc();
        modAtt.setAttribute(attMod);
        modelAttributeService.bindAttributeToModel(model.getName(), modAtt);

        // Geo fragment
        final Fragment geo = Fragment.buildFragment("GEO", "Geographic information");

        // Attribute #3 in geo fragment
        attMod = AttributeModelBuilder.build("CRS", PropertyType.STRING, "ForTests").fragment(geo)
                .withEnumerationRestriction("Earth", "Mars", "Venus");
        attributeModelService.addAttribute(attMod, false);

        modelAttributeService.bindNSAttributeToModel(model.getName(), attMod.getFragment());

        // Contact fragment
        final Fragment contact = Fragment.buildFragment("Contact", "Contact information");

        // Attribute #5 in contact fragment
        attMod = AttributeModelBuilder.build("Phone", PropertyType.STRING, "ForTests").fragment(contact)
                .withPatternRestriction("[0-9 ]{10}");
        attributeModelService.addAttribute(attMod, false);

        modelAttributeService.bindNSAttributeToModel(model.getName(), attMod.getFragment());

        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE))));

        final ResultActions resultActions = performDefaultGet(ModelController.TYPE_MAPPING
                + ModelController.MODEL_MAPPING + "/export", requestBuilderCustomizer, "Should return result",
                                                              model.getName());

        assertMediaType(resultActions, MediaType.APPLICATION_XML);
        Assert.assertNotNull(payload(resultActions));
    }

    /**
     * Create a dataset model
     * @throws ModuleException
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Delete a model")
    public void deleteModelTest_shouldDeleteModel() throws ModuleException {
        // Prepare test
        final Model model = modelService
                .createModel(Model.build("MODEL", "I will be deleted soon", EntityType.DATA));

        // Define expectations
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isNoContent());

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE))));

        // Perform test
        performDefaultDelete(ModelController.TYPE_MAPPING + ModelController.MODEL_MAPPING, requestBuilderCustomizer,
                             "Model should be deleted", model.getName());
    }
}
