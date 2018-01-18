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
package fr.cnes.regards.modules.entities.rest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.gson.Gson;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.rest.ModelController;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 *
 * Test collection validation
 *
 * @author Marc Sordi
 *
 */
@DirtiesContext
@MultitenantTransactional
@ContextConfiguration(classes = { ControllerITConfig.class })
public class CollectionValidationIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionValidationIT.class);

    /**
     * {@link Model} service
     */
    @Autowired
    private IModelService modelService;

    /**
     * {@link IAttributeModelService} service
     */
    @Autowired
    private IAttributeModelService attributeModelService;

    /**
     * Attribute Adapter Factory
     */
    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory attributeAdapterFactory;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private Gson gson;

    /**
     * Import a model
     *
     * @param pFilename
     *            model to import from resources folder
     */
    private void importModel(final String pFilename) {

        final Path filePath = Paths.get("src", "test", "resources", pFilename);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isCreated());

        performDefaultFileUpload(ModelController.TYPE_MAPPING + "/import", filePath, expectations,
                                 "Should be able to import a fragment");

        final List<AttributeModel> atts = attributeModelService.getAttributes(null, null);
        attributeAdapterFactory.refresh(DEFAULT_TENANT, atts);
    }

    /**
     * Instance with a simple single root attribute
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test(expected = AssertionError.class)
    public void testSimpleModel() throws ModuleException {
        importModel("simple-model.xml");

        final Model mission = modelService.getModelByName("MISSION");

        final Collection mission1 = new Collection(mission, null, "SPOT");

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultPost(CollectionController.ROOT_MAPPING, mission1, expectations, "...");
    }

    @Test
    public void test1CollectionWith() {
        importModel("modelTest1.xml");

    }

    @Test
    public void testPropertyCase() throws ModuleException {
        importModel("simple-model-label.xml");

        Model model = modelService.getModelByName("MISSION_WITH_LABEL");

        Collection collection = new Collection(model, DEFAULT_TENANT, "mission");
        Set<AbstractAttribute<?>> atts = new HashSet<>();
        atts.add(AttributeBuilder.buildString("LABEL", "uppercaselabel"));
        collection.setProperties(atts);

        // Set multitenant factory tenant
        tenantResolver.forceTenant(DEFAULT_TENANT);

        String collectionStr = gson.toJson(collection);
        MockMultipartFile collectionPart = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                collectionStr.getBytes());

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isCreated());

        List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(collectionPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING, parts, expectations,
                                 "Failed to create a new collection");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    // @Requirement("REGARDS_DSL_DAM_COL_010")
    // @Requirement("REGARDS_DSL_DAM_COL_020")
    // @Purpose("Shall create a new collection")
    // @Test
    // public void testPostCollectionWithAttributes() {
    // final Collection collectionWithAtt = new Collection("IpID2", model1, "pDescription2", "pName2");
    // List<AbstractAttribute<?>> atts = new ArrayList<>();
    //
    // // Add attributes
    // String attributeName = "name";
    // String attributeGeo = "geo";
    // attributeAdapterFactory.registerSubtype(StringAttribute.class, attributeName);
    // attributeAdapterFactory.registerSubtype(GeometryAttribute.class, attributeGeo);
    // atts.add(AttributeBuilder.buildString(attributeName, "test name"));
    // atts.add(AttributeBuilder.buildGeometry(attributeGeo, "POLYGON(...)"));
    // collectionWithAtt.setAttributes(atts);
    //
    // expectations.add(MockMvcResultMatchers.status().isCreated());
    // expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    //
    // performDefaultPost(COLLECTIONS, collectionWithAtt, expectations, "Failed to create a new collection");
    //
    // }
}
