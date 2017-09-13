/*
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
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
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
        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());

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
        AttributeModel attMod = AttributeModelBuilder.build("att_string", AttributeType.STRING, "ForTests").withoutRestriction();
        attributeModelService.addAttribute(attMod, false);

        ModelAttrAssoc modAtt = new ModelAttrAssoc();
        modAtt.setAttribute(attMod);
        modelAttributeService.bindAttributeToModel(model.getId(), modAtt);

        // Attribute #2 in default fragment
        attMod = AttributeModelBuilder.build("att_boolean", AttributeType.BOOLEAN, "ForTests").isAlterable().withoutRestriction();
        attributeModelService.addAttribute(attMod, false);

        modAtt = new ModelAttrAssoc();
        modAtt.setAttribute(attMod);
        modelAttributeService.bindAttributeToModel(model.getId(), modAtt);

        // Geo fragment
        final Fragment geo = Fragment.buildFragment("GEO", "Geographic information");

        // Attribute #3 in geo fragment
        attMod = AttributeModelBuilder.build("CRS", AttributeType.STRING, "ForTests").fragment(geo)
                .withEnumerationRestriction("Earth", "Mars", "Venus");
        attributeModelService.addAttribute(attMod, false);

        modelAttributeService.bindNSAttributeToModel(model.getId(), attMod.getFragment());

        // Contact fragment
        final Fragment contact = Fragment.buildFragment("Contact", "Contact information");

        // Attribute #5 in contact fragment
        attMod = AttributeModelBuilder.build("Phone", AttributeType.STRING, "ForTests").fragment(contact)
                .withPatternRestriction("[0-9 ]{10}");
        attributeModelService.addAttribute(attMod, false);

        modelAttributeService.bindNSAttributeToModel(model.getId(), attMod.getFragment());

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        final ResultActions resultActions = performDefaultGet(ModelController.TYPE_MAPPING + "/{pModelId}/export",
                                                              expectations, "Should return result", model.getId());

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
                .createModel(Model.build("MODEL", "I will be deleted soon", EntityType.DOCUMENT));

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        // Perform test
        performDefaultDelete(ModelController.TYPE_MAPPING + "/{pModelId}", expectations, "Model should be deleted",
                             model.getId());
    }
}
