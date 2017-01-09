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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.rest.ModelController;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 *
 * Test collection validation
 *
 * @author Marc Sordi
 *
 */
@MultitenantTransactional
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

    /**
     * Instance with a simple single root attribute
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test
    public void testSimpleModel() throws ModuleException {
        importModel("simple-model.xml");

        Model mission = modelService.getModelByName("MISSION");

        Collection mission1 = new Collection(mission, null);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        performDefaultPost("/collections", mission1, expectations, "...");
    }

    @Test
    public void test1CollectionWith() {
        importModel("modelTest1.xml");

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
