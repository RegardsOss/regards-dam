/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.gson.JsonParseException;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.entities.service.ICollectionService;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.rest.ModelController;
import fr.cnes.regards.modules.models.service.IAttributeModelService;

/**
 * Test collection validation
 *
 * @author Marc Sordi
 * @author Maxime Bouveron
 */
@DirtiesContext
@MultitenantTransactional
@ContextConfiguration(classes = { ControllerITConfig.class })
public class CollectionValidation2IT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionValidation2IT.class);

    @Autowired
    private ICollectionService collectionService;

    /**
     * Model Repository
     */
    @Autowired
    private IModelRepository modelRepository;

    /**
     * Attribute Adapter Factory
     */
    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory attributeAdapterFactory;

    /**
     * {@link IAttributeModelService} service
     */
    @Autowired
    private IAttributeModelService attributeModelService;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    /**
     * The XML file used as a model
     */
    private final String modelFile = "modelTest1.xml";

    /**
     * The mission name
     */
    private final String missionName = "MISSION";

    /**
     * The mission SipID
     */
    private final String sipId = "SIPID";

    /**
     * The reference attribute name
     */
    private final String refAtt = "reference";

    /**
     * the reference attribute value
     */
    private final String refValue = "REFTEST";

    /**
     * The active attribute name
     */
    private final String actAtt = "active";

    /**
     * the active attribute value
     */
    private final Boolean actValue = true;

    /**
     * the geo fragment name
     */
    private final String geo = "geo";

    /**
     * the crs attribute name
     */
    private final String crsAtt = "crs";

    /**
     * the crs attribute value
     */
    private final String crsValue = "Earth";

    /**
     * Collection label
     */
    private static final String COLLECTION_LABEL = "label";

    /**
     * Import a model
     *
     * @param filename
     *            model to import from resources folder
     */
    private void importModel(final String filename) {

        Path filePath = Paths.get("src", "test", "resources", filename);

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isCreated());

        performDefaultFileUpload(ModelController.TYPE_MAPPING + "/import", filePath, customizer,
                                 "Should be able to import a fragment");

        final List<AttributeModel> atts = attributeModelService.getAttributes(null, null, null);
        attributeAdapterFactory.refresh(getDefaultTenant(), atts);
    }

    @Before
    public void init() {
        tenantResolver.forceTenant(getDefaultTenant());
    }

    @Test
    public void test1CollectionWith() {
        importModel(modelFile);
    }

    /**
     * Test if a good collection is created
     *
     * @throws Exception
     */
    @Test
    public void postCollection() throws Exception {

        // Create a good collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName(missionName);

        // Collection
        // final Collection collection = new Collection(sipId, model1, missionDesc, missionName);
        Collection collection = new Collection(model1, getDefaultTenant(), COLLECTION_LABEL);
        collection.setProviderId(sipId);
        collection.setCreationDate(OffsetDateTime.now());
        Set<AbstractAttribute<?>> atts = new HashSet<>();

        atts.add(AttributeBuilder.buildString(refAtt, refValue));
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        atts.add(AttributeBuilder.buildObject(geo, AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setProperties(atts);

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isCreated());
        tenantResolver.forceTenant(getDefaultTenant());
        performDefaultPost(CollectionController.TYPE_MAPPING, collection, customizer,
                           "Failed to create a new collection");

        // lets test update without altering the non alterable attribute(active)

        tenantResolver.forceTenant(getDefaultTenant());
        collection = collectionService.load(collection.getIpId());
        atts = new HashSet<>();

        atts.add(AttributeBuilder.buildString(refAtt, refValue + "new"));
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));
        atts.add(AttributeBuilder.buildObject(geo, AttributeBuilder.buildString(crsAtt, crsValue)));
        collection.setProperties(atts);

        customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        performDefaultPost(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_MAPPING, collection,
                           customizer, "Failed to update a collection", collection.getId());

        // lets change the non alterable
        atts = new HashSet<>();
        Collection newCollection = new Collection(model1, getDefaultTenant(), "newone");
        newCollection.setCreationDate(collection.getCreationDate());
        newCollection.setGeometry(collection.getGeometry());
        newCollection.setGroups(collection.getGroups());
        newCollection.setId(collection.getId());
        newCollection.setIpId(collection.getIpId());
        newCollection.setLabel(collection.getLabel());
        newCollection.setLastUpdate(collection.getLastUpdate());
        newCollection.setModel(collection.getModel());

        atts.add(AttributeBuilder.buildString(refAtt, refValue + "new"));
        atts.add(AttributeBuilder.buildBoolean(actAtt, !actValue));
        atts.add(AttributeBuilder.buildObject(geo, AttributeBuilder.buildString(crsAtt, crsValue)));
        newCollection.setProperties(atts);

        customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isUnprocessableEntity());
        tenantResolver.forceTenant(getDefaultTenant());
        performDefaultPost(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_MAPPING, newCollection,
                           customizer, "Failed to update a collection", collection.getId());
    }

    @Test
    public void testOptionalNonAlterable() throws ModuleException {
        importModel("simpleCollectionOptional.xml");
        Model model = modelRepository.findByName("TestOptionalNonAlterable");
        // lets first create a collection with the optional non alterable attribute given.
        Collection optionalNonAlterable = new Collection(model, getDefaultTenant(), "optionalNonAlterable");
        optionalNonAlterable.setProviderId(sipId);
        optionalNonAlterable.setCreationDate(OffsetDateTime.now());
        Set<AbstractAttribute<?>> atts = new HashSet<>();
        atts.add(AttributeBuilder.buildString(refAtt, "ref"));
        optionalNonAlterable.setProperties(atts);

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isCreated());
        tenantResolver.forceTenant(getDefaultTenant());
        performDefaultPost(CollectionController.TYPE_MAPPING, optionalNonAlterable, customizer,
                           "Failed to create a new collection");

        // now lets try to update this collection and get an error
        tenantResolver.forceTenant(getDefaultTenant());
        optionalNonAlterable = collectionService.load(optionalNonAlterable.getIpId());
        atts = new HashSet<>();
        Collection optionalAltered = new Collection(model, getDefaultTenant(), "optionalAltered");
        optionalAltered.setCreationDate(optionalNonAlterable.getCreationDate());
        optionalAltered.setGeometry(optionalNonAlterable.getGeometry());
        optionalAltered.setGroups(optionalNonAlterable.getGroups());
        optionalAltered.setId(optionalNonAlterable.getId());
        optionalAltered.setIpId(optionalNonAlterable.getIpId());
        optionalAltered.setLabel(optionalNonAlterable.getLabel());
        optionalAltered.setLastUpdate(optionalNonAlterable.getLastUpdate());
        optionalAltered.setModel(optionalNonAlterable.getModel());

        atts.add(AttributeBuilder.buildString(refAtt, "other"));
        optionalAltered.setProperties(atts);

        customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isUnprocessableEntity());
        performDefaultPost(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_MAPPING, optionalAltered,
                           customizer, "Failed to update a collection", optionalNonAlterable.getId());

        // now lets try again without giving the value on the creation

        Collection optionalNotGivenNonAlterable = new Collection(model, getDefaultTenant(),
                "optionalNotGivenNonAlterable");
        optionalNonAlterable.setProviderId(sipId);
        optionalNonAlterable.setCreationDate(OffsetDateTime.now());

        customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isCreated());
        performDefaultPost(CollectionController.TYPE_MAPPING, optionalNotGivenNonAlterable, customizer,
                           "Failed to create a new collection");

        // now lets try to update this collection and give the optional value and be a success=
        tenantResolver.forceTenant(getDefaultTenant());
        optionalNotGivenNonAlterable = collectionService.load(optionalNotGivenNonAlterable.getIpId());
        atts = new HashSet<>();
        Collection optionalAlteredNotGiven = new Collection(model, getDefaultTenant(), "optionalAlteredNotGiven");
        optionalAlteredNotGiven.setCreationDate(optionalNotGivenNonAlterable.getCreationDate());
        optionalAlteredNotGiven.setGeometry(optionalNotGivenNonAlterable.getGeometry());
        optionalAlteredNotGiven.setGroups(optionalNotGivenNonAlterable.getGroups());
        optionalAlteredNotGiven.setId(optionalNotGivenNonAlterable.getId());
        optionalAlteredNotGiven.setIpId(optionalNotGivenNonAlterable.getIpId());
        optionalAlteredNotGiven.setLabel(optionalNotGivenNonAlterable.getLabel());
        optionalAlteredNotGiven.setLastUpdate(optionalNotGivenNonAlterable.getLastUpdate());
        optionalAlteredNotGiven.setModel(optionalNotGivenNonAlterable.getModel());

        atts.add(AttributeBuilder.buildString(refAtt, "other"));
        optionalAltered.setProperties(atts);

        customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        performDefaultPost(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_MAPPING,
                           optionalAlteredNotGiven, customizer, "Failed to update a collection",
                           optionalNotGivenNonAlterable.getId());
    }

    /**
     * Test if error occurs when an attribute has a bad type
     *
     * @throws ModuleException
     *             module exception
     */
    @Ignore
    @Test(expected = JsonParseException.class)
    public void postCollectionWithBadType() throws ModuleException {

        // Create a bad collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName(missionName);

        // Collection
        // final Collection collection = new Collection(sipId, model1, missionDesc, missionName);
        final Collection collection = new Collection(model1, null, COLLECTION_LABEL);
        final Set<AbstractAttribute<?>> atts = new HashSet<>();

        // bad values
        final int badRefValue = 5;
        atts.add(AttributeBuilder.buildInteger(refAtt, badRefValue));
        final String badActValue = "true";
        atts.add(AttributeBuilder.buildString(actAtt, badActValue));

        atts.add(AttributeBuilder.buildObject(geo, AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setProperties(atts);

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().is5xxServerError());
        tenantResolver.forceTenant(getDefaultTenant());
        performDefaultPost(CollectionController.TYPE_MAPPING, collection, customizer,
                           "Failed to create a new collection");
    }

    /**
     * Test if an error occurs when giving an attribute a bad name
     *
     * @throws ModuleException
     *             module exception
     */
    @Test(expected = AssertionError.class)
    public void postCollectionWithBadAttributeName() throws ModuleException {

        // Create a bad collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName(missionName);

        // Collection
        final Collection collection = new Collection(model1, null, COLLECTION_LABEL);
        final Set<AbstractAttribute<?>> atts = new HashSet<>();

        atts.add(AttributeBuilder.buildString(refAtt, refValue));
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        atts.add(AttributeBuilder.buildObject("notGeo", AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setProperties(atts);

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isUnprocessableEntity());
        tenantResolver.forceTenant(getDefaultTenant());
        performDefaultPost(CollectionController.TYPE_MAPPING, collection, customizer,
                           "Failed to create a new collection");
    }

    /**
     * Test if an error occurs when an enumaration restriction is violated
     *
     * @throws ModuleException
     *             module exception
     */
    @Test
    public void postCollectionWithWrongValue() throws ModuleException {

        // Create a bad collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName(missionName);

        // Collection
        // final Collection collection = new Collection(sipId, model1, missionDesc, missionName);
        final Collection collection = new Collection(model1, null, COLLECTION_LABEL);
        final Set<AbstractAttribute<?>> atts = new HashSet<>();

        atts.add(AttributeBuilder.buildString(refAtt, refValue));
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        atts.add(AttributeBuilder.buildObject(geo, AttributeBuilder.buildString(crsAtt, "notEarth")));

        collection.setProperties(atts);

        tenantResolver.forceTenant(getDefaultTenant());
        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isUnprocessableEntity());
        performDefaultPost(CollectionController.TYPE_MAPPING, collection, customizer,
                           "Failed to create a new collection");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
