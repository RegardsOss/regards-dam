/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonpath.JsonPath;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestParamBuilder;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * Test module API
 *
 * @author msordi
 *
 */
@MultitenantTransactional
public class AttributeModelControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeModelControllerIT.class);

    /**
     * Restriction method mapping
     *
     */
    private static final String RESTRICTION_MAPPING = "/restrictions";

    /**
     * JSON path
     */
    private static final String JSON_ID = "$.id";

    /**
     * JPA entity manager : use it to flush context to prevent false positive
     */
    @PersistenceContext
    protected EntityManager entityManager;

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
        performDefaultGet(AttributeModelController.TYPE_MAPPING, expectations, "Cannot get all attributes");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createBooleanAttribute() {
        createAttribute("BOOLEAN_ATT", "boolean description", AttributeType.BOOLEAN);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createDateArrayAttribute() {
        createAttribute("DATE_ARRAY_ATT", "date array description", AttributeType.DATE_ARRAY);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createDateIntervalAttribute() {
        createAttribute("DATE_INTERV_ATT", "date interval description", AttributeType.DATE_INTERVAL);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createDateISOAttribute() {
        createAttribute("DATE_ISO", "date ISO description", AttributeType.DATE_ISO8601);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createEnumAttribute() {
        createAttribute("ENUM_ATT", "enum description", AttributeType.ENUMERATION);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createFloatAttribute() {
        createAttribute("FLOAT_ATT", "float description", AttributeType.FLOAT);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createFloatArrayAttribute() {
        createAttribute("FLOAT_ARRAY_ATT", "float array description", AttributeType.FLOAT_ARRAY);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createFloatIntervalAttribute() {
        createAttribute("FLOAT_INTERVAL_ATT", "float interval description", AttributeType.FLOAT_INTERVAL);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createGeoAttribute() {
        createAttribute("GEO", "geo description", AttributeType.GEOMETRY);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createIntegerAttribute() {
        createAttribute("INTEGER_ATT", "Integer description", AttributeType.INTEGER);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createIntegerArrayAttribute() {
        createAttribute("INTEGER_ARRAY_ATT", "Integer array description", AttributeType.INTEGER_ARRAY);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createIntegerIntervalAttribute() {
        createAttribute("INTEGER_INTERVAL_ATT", "Integer interval description", AttributeType.INTEGER_INTERVAL);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createStringAttribute() {
        createAttribute("STRING_ATT", "string description", AttributeType.STRING);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createStringArrayAttribute() {
        createAttribute("STRING_ARRAY_ATT", "string array description", AttributeType.STRING_ARRAY);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createUrlAttribute() {
        createAttribute("URL_ATT", "url description", AttributeType.URL);
    }

    /**
     * Test object creation related to fragment
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Create an object by grouping simple attributes in a fragment")
    public void createObject() {
        final String fragmentName = "Contact";
        final Fragment fragment = Fragment.buildFragment(fragmentName, "User contact information");
        createAttribute("City", null, AttributeType.STRING, fragment);
        createAttribute("Phone", null, AttributeType.STRING, fragment);
        createAttribute("Age", null, AttributeType.INTEGER, fragment);

        final Integer expectedSize = 3;
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(expectedSize)));

        performDefaultGet(AttributeModelController.TYPE_MAPPING, expectations,
                          "Should return result " + expectedSize + " attributes.", RequestParamBuilder.build()
                                  .param(AttributeModelController.PARAM_FRAGMENT_NAME, fragmentName));

    }

    /**
     * POST a new attribute
     *
     * @param pName
     *            name
     * @param pDescription
     *            description
     * @param pType
     *            type
     * @param pFragment
     *            fragment
     * @return {@link ResultActions}
     */
    private ResultActions createAttribute(String pName, String pDescription, AttributeType pType, Fragment pFragment) {
        final AttributeModel attModel = AttributeModelBuilder.build(pName, pType).description(pDescription)
                .fragment(pFragment).get();
        // Define expectations
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.equalTo(pName)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.description", Matchers.equalTo(pDescription)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.type", Matchers.equalTo(pType.toString())));
        return performDefaultPost(AttributeModelController.TYPE_MAPPING, attModel, expectations,
                                  "Consistent attribute should be created.");
    }

    private ResultActions createAttribute(String pName, String pDescription, AttributeType pType) {
        return createAttribute(pName, pDescription, pType, null);
    }

    /**
     * Test persisting and loading a simple attribute
     */
    @Test
    public void addSimpleAttribute() {

        final String attName = "NAME";

        final ResultActions resultActions = createAttribute(attName, "name description", AttributeType.STRING);

        final String json = payload(resultActions);
        final Integer id = JsonPath.read(json, JSON_ID);

        // Retrieve attribute
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));
        performDefaultGet(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", expectations,
                          "Cannot retrieve attribute", id);
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

        performDefaultPost(AttributeModelController.TYPE_MAPPING, attModel, expectations,
                           "Cannot add attribute with enum restriction");

        // Define conflict expectations
        final List<ResultMatcher> conflictExpectations = new ArrayList<>();
        conflictExpectations.add(MockMvcResultMatchers.status().isConflict());

        // Same clone model ... replay
        final AttributeModel conflictAttModel = AttributeModelBuilder.build(attName, AttributeType.ENUMERATION)
                .withEnumerationRestriction(acceptableValues);

        performDefaultPost(AttributeModelController.TYPE_MAPPING, conflictAttModel, conflictExpectations,
                           "Conflict not detected");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Test
    public void getAllRestrictions() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isServiceUnavailable());
        performDefaultGet(AttributeModelController.TYPE_MAPPING + RESTRICTION_MAPPING, expectations,
                          "Restriction must be retrieve by type");
    }

    @Test
    public void getRestrictionForString() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        performDefaultGet(AttributeModelController.TYPE_MAPPING + RESTRICTION_MAPPING, expectations,
                          "STRING restriction should exists!",
                          RequestParamBuilder.build().param("type", AttributeType.STRING.toString()));
    }

    /**
     * Test attribute update. Only description is updatable.
     *
     * FIXME : in same transaction, this test doesn't work even if correct data is stored in database.
     */
    @Test
    @Ignore
    public void updateAttributeModel() {
        final String name = "UPDATABLE";
        final AttributeType type = AttributeType.URL;
        final AttributeModel attMod = AttributeModelBuilder.build(name, type).description("DESC").get();

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());

        final ResultActions resultActions = performDefaultPost(AttributeModelController.TYPE_MAPPING, attMod,
                                                               expectations, "Attribute should be created.");

        final String json = payload(resultActions);
        final Integer id = JsonPath.read(json, JSON_ID);

        // Try to alter attribute
        attMod.setId(Long.valueOf(id));
        attMod.setName("CHANGE");
        final String description = "NEW DESC";
        attMod.setDescription(description);
        attMod.setType(AttributeType.BOOLEAN);

        performDefaultPut(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", attMod, expectations,
                          "Update should be successful.", id);

        // Perform a get attribute to retrieved real database content and avoid false negative

        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.is(id)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(name)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(description)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.type", Matchers.is(type.toString())));

        performDefaultGet(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", expectations,
                          "Cannot retrieve attribute", id);
    }
}
