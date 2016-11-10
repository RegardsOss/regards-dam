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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonpath.JsonPath;

import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.framework.test.integration.RequestParamBuilder;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Test module API
 *
 * @author msordi
 *
 */
public class AttributeControllerIT extends AbstractRegardsIT {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeControllerIT.class);

    /**
     * Class level mapping
     */
    private static final String TYPE_MAPPING = "/models/attributes";

    /**
     * Test get attributes
     *
     */
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Get model attributes to manage data models")
    @Test
    public void testGetAttributes() {

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Perform test
        performDefaultGet(TYPE_MAPPING, expectations, "Cannot get all attributes");
    }

    /**
     * Test persisting and loading a simple attribute
     */
    @Test
    public void addSimpleAttribute() {

        final String attName = "NAME";
        final String idPath = "$.id";

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(idPath, Matchers.notNullValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.equalTo(attName)));

        // Content
        final AttributeModel attModel = AttributeModelBuilder.build(attName, AttributeType.STRING).get();

        final ResultActions resultActions = performDefaultPost(TYPE_MAPPING, attModel, expectations,
                                                               "Cannot add attribute");
        final String json = payload(resultActions);
        final Integer id = JsonPath.read(json, idPath);

        // Retrieve attribute
        performDefaultGet(TYPE_MAPPING + "/{pAttributeId}", expectations, "Cannot retrieve attribute", id);
    }

    /**
     * Check if inserting same attribute throws a conflict exception
     */
    @Test
    public void manageConflictedAttributes() {
        final String attName = "ALPHABET";
        final String[] acceptableValues = new String[] { "ALPHA", "BETA", "GAMMA" };

        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        // Content
        final AttributeModel attModel = AttributeModelBuilder.build(attName, AttributeType.ENUMERATION)
                .withEnumerationRestriction(acceptableValues);

        performDefaultPost(TYPE_MAPPING, attModel, expectations, "Cannot add attribute with enum restriction");

        // Define conflict expectations
        final List<ResultMatcher> conflictExpectations = new ArrayList<>();
        conflictExpectations.add(MockMvcResultMatchers.status().isConflict());

        // Same clone model ... replay
        final AttributeModel conflictAttModel = AttributeModelBuilder.build(attName, AttributeType.ENUMERATION)
                .withEnumerationRestriction(acceptableValues);

        performDefaultPost(TYPE_MAPPING, conflictAttModel, conflictExpectations, "Conflict not detected");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Test
    public void getAllRestrictions() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isServiceUnavailable());
        performDefaultGet(TYPE_MAPPING + "/restrictions", expectations, "Restriction must be retrieve by type");
    }

    @Test
    public void getRestrictionForString() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        performDefaultGet(TYPE_MAPPING + "/restrictions", expectations, "Restriction must be retrieve by type",
                          RequestParamBuilder.build().param("type", AttributeType.STRING.toString()));
    }
}
