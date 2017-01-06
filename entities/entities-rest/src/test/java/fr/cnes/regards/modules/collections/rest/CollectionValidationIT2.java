/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.rest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.entities.domain.adapters.gson.AttributeAdapterFactory;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.BooleanAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.GeometryAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.rest.ModelController;

/**
 *
 * Test collection validation
 *
 * @author Marc Sordi
 * @author Maxime Bouveron
 *
 */
@MultitenantTransactional
public class CollectionValidationIT2 extends AbstractRegardsTransactionalIT {

    @Autowired
    private IModelRepository modelRepository;

    @Autowired
    private AttributeAdapterFactory attributeAdapterFactory;

    private final String modelFile = "modelTest1.xml";

    private final String collectionAPI = "/collections";

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionValidationIT2.class);

    /**
     * Import a model
     *
     * @param pFilename
     *            model to import from resources folder
     */
    private void importModel(String pFilename) {

        final Path filePath = Paths.get("src", "test", "resources", pFilename);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());

        performDefaultFileUpload(ModelController.TYPE_MAPPING + "/import", filePath, expectations,
                                 "Should be able to import a fragment");
    }

    @Test
    public void test1CollectionWith() {
        importModel(modelFile);
    }

    @Test
    public void postCollection() throws ModuleException {

        // Create a good collection

        // Model
        importModel("modelTest1.xml");
        final Model model1 = modelRepository.findByName("MISSION");

        // Collection
        final Collection collection = new Collection("SIPID", model1, "Sample mission", "MISSION");
        final List<AbstractAttribute<?>> atts = new ArrayList<>();

        // ATTRIBUTES

        // Reference
        final String refAtt = "reference";
        final String refValue = "REFTEST";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, refAtt);
        atts.add(AttributeBuilder.buildString(refAtt, refValue));

        // Active
        final String actAtt = "active";
        final Boolean actValue = true;
        attributeAdapterFactory.registerSubtype(BooleanAttribute.class, actAtt);
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        String geo = "geo";

        // Coordinates
        final String coorAtt = "coordinate";
        final String coorValue = "POLYGON(...)";
        attributeAdapterFactory.registerSubtype(GeometryAttribute.class, coorAtt, geo);

        // crs
        final String crsAtt = "crs";
        final String crsValue = "Earth";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, crsAtt, geo);

        // GEO
        attributeAdapterFactory.registerSubtype(ObjectAttribute.class, geo);
        atts.add(AttributeBuilder.buildObject("geo", AttributeBuilder.buildGeometry(coorAtt, coorValue),
                                              AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setAttributes(atts);

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, "Failed to create a new collection");
    }

    @Test
    public void postCollectionWithBadType() throws ModuleException {

        // Create a bad collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName("MISSION");

        // Collection
        final Collection collection = new Collection("SIPID", model1, "Sample mission", "MISSION");
        final List<AbstractAttribute<?>> atts = new ArrayList<>();

        // ATTRIBUTES

        // Reference
        final String refAtt = "reference";
        final int refValue = 5;
        attributeAdapterFactory.registerSubtype(IntegerAttribute.class, refAtt);
        atts.add(AttributeBuilder.buildInteger("reference", refValue));

        // Active
        final String actAtt = "active";
        final String actValue = "true";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, actAtt);
        atts.add(AttributeBuilder.buildString(actAtt, actValue));

        String geo = "geo";

        // Coordinates
        final String coorAtt = "coordinate";
        final String coorValue = "POLYGON(...)";
        attributeAdapterFactory.registerSubtype(GeometryAttribute.class, coorAtt, geo);

        // crs
        final String crsAtt = "crs";
        final String crsValue = "Earth";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, crsAtt, geo);

        // GEO
        attributeAdapterFactory.registerSubtype(ObjectAttribute.class, geo);
        atts.add(AttributeBuilder.buildObject("geo", AttributeBuilder.buildGeometry(coorAtt, coorValue),
                                              AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setAttributes(atts);

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        // FIXME mettre un code erreur HTTP adapté
        expectations.add(MockMvcResultMatchers.status().is5xxServerError());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, "Collection should not be created");
    }

    @Test
    public void postCollectionWithBadName() throws ModuleException {

        // Create a bad collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName("MISSION");

        // Collection
        final Collection collection = new Collection("SIPID", model1, "Bad mission", "notMISSION");
        final List<AbstractAttribute<?>> atts = new ArrayList<>();

        // ATTRIBUTES

        // Reference
        final String refAtt = "reference";
        final String refValue = "REFTEST";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, refAtt);
        atts.add(AttributeBuilder.buildString(refAtt, refValue));

        // Active
        final String actAtt = "active";
        final Boolean actValue = true;
        attributeAdapterFactory.registerSubtype(BooleanAttribute.class, actAtt);
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        String geo = "geo";

        // Coordinates
        final String coorAtt = "coordinate";
        final String coorValue = "POLYGON(...)";
        attributeAdapterFactory.registerSubtype(GeometryAttribute.class, coorAtt, geo);

        // crs
        final String crsAtt = "crs";
        final String crsValue = "Earth";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, crsAtt, geo);

        // GEO
        attributeAdapterFactory.registerSubtype(ObjectAttribute.class, geo);
        atts.add(AttributeBuilder.buildObject("geo", AttributeBuilder.buildGeometry(coorAtt, coorValue),
                                              AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setAttributes(atts);

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        // FIXME mettre un code erreur HTTP adapté
        expectations.add(MockMvcResultMatchers.status().is5xxServerError());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, "Collection should not be created");
    }

    @Test
    public void postCollectionWithBadAttributeName() throws ModuleException {

        // Create a bad collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName("MISSION");

        // Collection
        final Collection collection = new Collection("SIPID", model1, "Sample mission", "MISSION");
        final List<AbstractAttribute<?>> atts = new ArrayList<>();

        // ATTRIBUTES

        // Reference
        final String refAtt = "reference";
        final String refValue = "REFTEST";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, refAtt);
        atts.add(AttributeBuilder.buildString(refAtt, refValue));

        // Active
        final String actAtt = "active";
        final Boolean actValue = true;
        attributeAdapterFactory.registerSubtype(BooleanAttribute.class, actAtt);
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        String geo = "notGeo";

        // Coordinates
        final String coorAtt = "coordinate";
        final String coorValue = "POLYGON(...)";
        attributeAdapterFactory.registerSubtype(GeometryAttribute.class, coorAtt, geo);

        // crs
        final String crsAtt = "crs";
        final String crsValue = "Earth";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, crsAtt, geo);

        // GEO
        attributeAdapterFactory.registerSubtype(ObjectAttribute.class, geo);
        atts.add(AttributeBuilder.buildObject("geo", AttributeBuilder.buildGeometry(coorAtt, coorValue),
                                              AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setAttributes(atts);

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        // FIXME mettre un code erreur HTTP adapté
        expectations.add(MockMvcResultMatchers.status().is5xxServerError());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, "Collection should not be created");
    }

    @Test
    public void postCollectionWithWrongValue() throws ModuleException {

        // Create a bad collection

        // Model
        importModel(modelFile);
        final Model model1 = modelRepository.findByName("MISSION");

        // Collection
        final Collection collection = new Collection("SIPID", model1, "Sample mission", "MISSION");
        final List<AbstractAttribute<?>> atts = new ArrayList<>();

        // ATTRIBUTES

        // Reference
        final String refAtt = "reference";
        final String refValue = "REFTEST";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, refAtt);
        atts.add(AttributeBuilder.buildString(refAtt, refValue));

        // Active
        final String actAtt = "active";
        final Boolean actValue = true;
        attributeAdapterFactory.registerSubtype(BooleanAttribute.class, actAtt);
        atts.add(AttributeBuilder.buildBoolean(actAtt, actValue));

        String geo = "geo";

        // Coordinates
        final String coorAtt = "coordinate";
        final String coorValue = "POLYGON(...)";
        attributeAdapterFactory.registerSubtype(GeometryAttribute.class, coorAtt, geo);

        // crs
        final String crsAtt = "crs";
        final String crsValue = "notEarth";
        attributeAdapterFactory.registerSubtype(StringAttribute.class, crsAtt, geo);

        // GEO
        attributeAdapterFactory.registerSubtype(ObjectAttribute.class, geo);
        atts.add(AttributeBuilder.buildObject("geo", AttributeBuilder.buildGeometry(coorAtt, coorValue),
                                              AttributeBuilder.buildString(crsAtt, crsValue)));

        collection.setAttributes(atts);

        final List<ResultMatcher> expectations = new ArrayList<ResultMatcher>();

        // FIXME mettre un code erreur HTTP adapté
        expectations.add(MockMvcResultMatchers.status().is5xxServerError());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPost(collectionAPI, collection, expectations, "Collection should not be created");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
