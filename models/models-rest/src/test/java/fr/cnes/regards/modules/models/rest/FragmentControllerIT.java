/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * Test fragment
 *
 * @author Marc Sordi
 *
 */
@MultitenantTransactional
public class FragmentControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentControllerIT.class);

    /**
     * Fragment repository to populate database for testing
     */
    @Autowired
    private IFragmentRepository fragmentRepository;

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Test
    public void createEmptyFragmentTest() {

        final Fragment fragment = new Fragment(null, null);

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isBadRequest());

        performDefaultPost(FragmentController.TYPE_MAPPING, fragment, expectations,
                           "Empty fragment shouldn't be created.");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Create model fragment (an object containing simple attributes)")
    public void addGeoFragment() {
        final Fragment fragment = Fragment.buildFragment("GEO", "Geo description");

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$.id", Matchers.notNullValue()));

        performDefaultPost(FragmentController.TYPE_MAPPING, fragment, expectations, "Fragment cannot be created.");
    }

    @Test
    public void getAllFragment() {
        populateDatabase();

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(3)));

        performDefaultGet(FragmentController.TYPE_MAPPING, expectations, "Should return result");
    }

    private void populateDatabase() {
        fragmentRepository.save(Fragment.buildDefault());
        fragmentRepository.save(Fragment.buildFragment("Geo", "Geographic information"));
        fragmentRepository.save(Fragment.buildFragment("Contact", "Contact card"));
    }
}
