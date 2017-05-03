/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.rest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.gson.JsonParseException;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.service.adapters.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.rest.ModelController;

/**
 * Test collection validation
 *
 * @author Marc Sordi
 * @author Maxime Bouveron
 */
@DirtiesContext
@MultitenantTransactional
public class CollectionValidation2IT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionValidation2IT.class);

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
     * @param pFilename model to import from resources folder
     */
    private void importModel(String pFilename) {

        final Path filePath = Paths.get("src", "test", "resources", pFilename);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultFileUpload(ModelController.TYPE_MAPPING + "/import", filePath, expectations,
                                 "Should be able to import a fragment");

        attributeAdapterFactory.refresh(DEFAULT_TENANT);
    }

    @Test
    public void test1CollectionWith() {
        importModel(modelFile);
    }

    /**
     * Test if a good collection is created
     *
     * @throws ModuleException module exception
     */
    @Test
    public void postCollection() throws ModuleException {

        // Create a good collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName(missionName);

        // Collection
        // final Collection collection = new Collection(sipId, model1, missionDesc, missionName);
        final Collection collection = new Collection(model1, null, COLLECTION_LABEL);
        collection.setSipId(sipId);
        collection.setCreationDate(OffsetDateTime.now());
        final Set<AbstractAttribute<?>> atts = new HashSet<>();

        atts.add(AttributeBuilder.buildString(refAtt, refValue));
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        atts.add(AttributeBuilder.buildObject(geo, AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setProperties(atts);

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, "Failed to create a new collection");
    }

    /**
     * Test if error occurs when an attribute has a bad type
     *
     * @throws ModuleException module exception
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
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, collectionCreationError);
    }

    /**
     * Test if an error occurs when giving an attribute a bad name
     *
     * @throws ModuleException module exception
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
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, collectionCreationError);
    }

    /**
     * Test if an error occurs when an enumaration restriction is violated
     *
     * @throws ModuleException module exception
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
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, collectionCreationError);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
