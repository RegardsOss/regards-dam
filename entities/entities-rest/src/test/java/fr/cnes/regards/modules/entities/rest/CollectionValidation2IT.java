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
import java.time.OffsetDateTime;
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
import com.google.gson.JsonParseException;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
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
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;

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

    @Autowired
    private IModelAttrAssocService modelAttrAssocService;

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

    @Autowired
    private Gson gson;

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
     * The mission description
     */
    private final String missionDesc = "Sample mission";

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
     * the coordinate attribute name
     */
    private final String coorAtt = "coordinate";

    /**
     * the coordinate attribute value
     */
    private final String coorValue = "POLYGON(...)";

    /**
     * the crs attribute name
     */
    private final String crsAtt = "crs";

    /**
     * the crs attribute value
     */
    private final String crsValue = "Earth";

    /**
     * The collection endpoint
     */
    private final String collectionAPI = "/collections";

    /**
     * The error message if a collection is created when it should not
     */
    private final String collectionCreationError = "Collection should not be created";

    /**
     * Collection label
     */
    private static final String COLLECTION_LABEL = "label";

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
        Collection collection = new Collection(model1, DEFAULT_TENANT, COLLECTION_LABEL);
        collection.setSipId(sipId);
        collection.setCreationDate(OffsetDateTime.now());
        Set<AbstractAttribute<?>> atts = new HashSet<>();

        atts.add(AttributeBuilder.buildString(refAtt, refValue));
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        atts.add(AttributeBuilder.buildObject(geo, AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setProperties(atts);

        List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        expectations.add(MockMvcResultMatchers.status().isCreated());

        tenantResolver.forceTenant(DEFAULT_TENANT);
        String collectionStr = gson.toJson(collection);
        MockMultipartFile collectionPart = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                                                                 collectionStr.getBytes());
        List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(collectionPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING, parts, expectations,
                                 "Failed to create a new collection");

        //lets test update without altering the non alterable attribute(active)

        tenantResolver.forceTenant(DEFAULT_TENANT);
        collection = collectionService.load(collection.getIpId());
        atts = new HashSet<>();

        atts.add(AttributeBuilder.buildString(refAtt, refValue + "new"));
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));
        atts.add(AttributeBuilder.buildObject(geo, AttributeBuilder.buildString(crsAtt, crsValue)));
        collection.setProperties(atts);

        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        collectionStr = gson.toJson(collection);
        collectionPart = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                                               collectionStr.getBytes());
        parts = new ArrayList<>();
        parts.add(collectionPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING + "/{collection_id}", parts, expectations,
                                 "Failed to update a collection", collection.getId());

        //lets change the non alterable
        tenantResolver.forceTenant(DEFAULT_TENANT);
        atts = new HashSet<>();
        Collection newCollection = new Collection();
        newCollection.setDescriptionFile(collection.getDescriptionFile());
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

        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());
        collectionStr = gson.toJson(newCollection);
        collectionPart = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                                               collectionStr.getBytes());
        parts = new ArrayList<>();
        parts.add(collectionPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING + "/{collection_id}", parts, expectations,
                                 "Failed to update a collection", collection.getId());
    }

    @Test
    public void testOptionalNonAlterable() throws ModuleException {
        importModel("simpleCollectionOptional.xml");
        Model model=modelRepository.findByName("TestOptionalNonAlterable");
        //lets first create a collection with the optional non alterable attribute given.
        Collection optionalNonAlterable=new Collection(model, DEFAULT_TENANT, "optionalNonAlterable");
        optionalNonAlterable.setSipId(sipId);
        optionalNonAlterable.setCreationDate(OffsetDateTime.now());
        Set<AbstractAttribute<?>> atts = new HashSet<>();
        atts.add(AttributeBuilder.buildString(refAtt, "ref"));
        optionalNonAlterable.setProperties(atts);

        tenantResolver.forceTenant(DEFAULT_TENANT);
        String collectionStr = gson.toJson(optionalNonAlterable);
        MockMultipartFile collectionPart = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                                                                 collectionStr.getBytes());
        List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(collectionPart);

        List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();
        expectations.add(MockMvcResultMatchers.status().isCreated());
        performDefaultFileUpload(CollectionController.ROOT_MAPPING, parts, expectations,
                                 "Failed to create a new collection");
        // now lets try to update this collection and get an error
        tenantResolver.forceTenant(DEFAULT_TENANT);
        optionalNonAlterable = collectionService.load(optionalNonAlterable.getIpId());
        atts = new HashSet<>();
        Collection optionalAltered = new Collection();
        optionalAltered.setDescriptionFile(optionalNonAlterable.getDescriptionFile());
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

        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());
        collectionStr = gson.toJson(optionalAltered);
        collectionPart = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                                               collectionStr.getBytes());
        parts = new ArrayList<>();
        parts.add(collectionPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING + "/{collection_id}", parts, expectations,
                                 "Failed to update a collection", optionalNonAlterable.getId());

        //now lets try again without giving the value on the creation

        Collection optionalNotGivenNonAlterable=new Collection(model, DEFAULT_TENANT, "optionalNotGivenNonAlterable");
        optionalNonAlterable.setSipId(sipId);
        optionalNonAlterable.setCreationDate(OffsetDateTime.now());

        tenantResolver.forceTenant(DEFAULT_TENANT);
        String collectionNotGivenStr = gson.toJson(optionalNotGivenNonAlterable);
        MockMultipartFile collectionNotGivenPart = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                                                                 collectionNotGivenStr.getBytes());
        List<MockMultipartFile> partsNotGiven = new ArrayList<>();
        partsNotGiven.add(collectionNotGivenPart);

        List<ResultMatcher> expectationsNotGiven = new ArrayList<ResultMatcher>();
        expectationsNotGiven.add(MockMvcResultMatchers.status().isCreated());
        performDefaultFileUpload(CollectionController.ROOT_MAPPING, partsNotGiven, expectationsNotGiven,
                                 "Failed to create a new collection");
        // now lets try to update this collection and give the optional value and be a success
        tenantResolver.forceTenant(DEFAULT_TENANT);
        optionalNotGivenNonAlterable = collectionService.load(optionalNotGivenNonAlterable.getIpId());
        atts = new HashSet<>();
        Collection optionalAlteredNotGiven = new Collection();
        optionalAlteredNotGiven.setDescriptionFile(optionalNotGivenNonAlterable.getDescriptionFile());
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

        expectationsNotGiven.clear();
        expectationsNotGiven.add(MockMvcResultMatchers.status().isOk());
        collectionNotGivenStr = gson.toJson(optionalAlteredNotGiven);
        collectionNotGivenPart = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                                               collectionNotGivenStr.getBytes());
        partsNotGiven = new ArrayList<>();
        partsNotGiven.add(collectionNotGivenPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING + "/{collection_id}", partsNotGiven, expectationsNotGiven,
                                 "Failed to update a collection", optionalNotGivenNonAlterable.getId());
    }

    /**
     * Test if error occurs when an attribute has a bad type
     *
     * @throws ModuleException
     *             module exception
     */
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

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        expectations.add(MockMvcResultMatchers.status().is5xxServerError());

        tenantResolver.forceTenant(DEFAULT_TENANT);
        final String collectionStr = gson.toJson(collection);
        final MockMultipartFile collectionPart = new MockMultipartFile("collection", "",
                                                                       MediaType.APPLICATION_JSON_VALUE,
                                                                       collectionStr.getBytes());
        final List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(collectionPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING, parts, expectations,
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

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());

        tenantResolver.forceTenant(DEFAULT_TENANT);
        final String collectionStr = gson.toJson(collection);
        final MockMultipartFile collectionPart = new MockMultipartFile("collection", "",
                                                                       MediaType.APPLICATION_JSON_VALUE,
                                                                       collectionStr.getBytes());
        final List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(collectionPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING, parts, expectations,
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

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        expectations.add(MockMvcResultMatchers.status().isUnprocessableEntity());

        tenantResolver.forceTenant(DEFAULT_TENANT);
        final String collectionStr = gson.toJson(collection);
        final MockMultipartFile collectionPart = new MockMultipartFile("collection", "",
                                                                       MediaType.APPLICATION_JSON_VALUE,
                                                                       collectionStr.getBytes());
        final List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(collectionPart);
        performDefaultFileUpload(CollectionController.ROOT_MAPPING, parts, expectations,
                                 "Failed to create a new collection");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
