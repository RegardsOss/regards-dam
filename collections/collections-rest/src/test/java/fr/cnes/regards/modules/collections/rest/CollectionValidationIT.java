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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.modules.models.rest.ModelController;

/**
 *
 * Test collection validation
 *
 * @author Marc Sordi
 *
 */
// @Ignore // TODO activate
@MultitenantTransactional
public class CollectionValidationIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionValidationIT.class);

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
        importModel("modelTest1.xml");

    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
