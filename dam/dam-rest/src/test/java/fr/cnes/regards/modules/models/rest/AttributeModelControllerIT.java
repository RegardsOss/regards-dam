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
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.assertj.core.util.Strings;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonpath.JsonPath;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.ConstrainedFields;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeProperty;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.LongRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;

/**
 * Test module API
 *
 * @author msordi
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=att_model_it" })
@MultitenantTransactional
public class AttributeModelControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeModelControllerIT.class);

    /**
     * Restriction method mapping
     */
    private static final String RESTRICTION_MAPPING = "/restrictions";

    /**
     * JSON path
     */
    private static final String JSON_ID = "$.content.id";

    /**
     * JPA entity manager : use it to flush context to prevent false positive
     */
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private IModelRepository modelRepository;

    @Autowired
    private IFragmentRepository FragmentRepository;

    /**
     * AttributeModel Repository
     */
    @Autowired
    private IAttributeModelRepository attributeModelRepository;

    /**
     * ModelAttribute Repository
     */
    @Autowired
    private IModelAttrAssocRepository modelAttributeRepository;

    private Model modelTest;

    public static List<FieldDescriptor> documentBody(boolean creation, String prefix) {
        String prefixPath = Strings.isNullOrEmpty(prefix) ? "" : prefix + ".";
        ConstrainedFields constrainedFields = new ConstrainedFields(AttributeModel.class);
        List<FieldDescriptor> descriptors = new ArrayList<>();
        if (!creation) {
            descriptors.add(constrainedFields.withPath(prefixPath + "id", "id", "Attribute Model identifier",
                                                       "Must be positive"));
        }
        descriptors.add(constrainedFields.withPath(prefixPath + "name", "name", "Attribute name"));
        descriptors.add(constrainedFields.withPath(prefixPath + "description", "description", "Attribute description")
                .type(JSON_STRING_TYPE).optional());
        descriptors
                .add(constrainedFields.withPath(prefixPath + "defaultValue", "defaultValue", "Attribute default value")
                        .type(JSON_STRING_TYPE).optional());
        descriptors.add(constrainedFields.withPath(prefixPath + "type", "type", "Attribute type",
                                                   "Available values: " + Arrays.stream(AttributeType.values())
                                                           .map(type -> type.name())
                                                           .reduce((first, second) -> first + ", " + second).get()));
        descriptors.add(constrainedFields
                .withPath(prefixPath + "unit", "unit", "Attribute unit useful for number based attributes",
                          "Max length: 16 characters")
                .type(JSON_STRING_TYPE).optional());
        descriptors
                .add(constrainedFields
                        .withPath(prefixPath + "precision", "precision",
                                  "Attribute precision useful for double based attributes")
                        .type(JSON_NUMBER_TYPE).optional());
        descriptors
                .add(constrainedFields
                        .withPath(prefixPath + "arraySize", "arraySize",
                                  "Attribute array size useful for array based attributes")
                        .type(JSON_NUMBER_TYPE).optional());
        descriptors
                .add(constrainedFields.withPath(prefixPath + "fragment", "fragment", "Attribute Fragment",
                                                "Should respect Fragment structure")
                        .type(JSON_OBJECT_TYPE).optional());
        descriptors.addAll(documentFragment(false, prefixPath + "fragment"));
        descriptors
                .add(constrainedFields
                        .withPath(prefixPath + "alterable", "alterable",
                                  "Whether this attribute can be altered by users", "Defaults to true")
                        .type(JSON_BOOLEAN_TYPE).optional());
        descriptors.add(constrainedFields.withPath(prefixPath + "optional", "optional",
                                                   "Whether this attribute is optional", "defaults to false")
                .type(JSON_BOOLEAN_TYPE).optional());
        descriptors.add(constrainedFields.withPath(prefixPath + "label", "label", "Attribute label"));
        descriptors.add(constrainedFields
                .withPath(prefixPath + "restriction", "restriction", "Attribute applicable restriction")
                .type(JSON_OBJECT_TYPE).optional());
        descriptors.addAll(documentAttributeRestriction(creation, prefixPath + "restriction"));
        descriptors
                .add(constrainedFields.withPath(prefixPath + "group", "group", "Attribute group for displaying purpose")
                        .type(JSON_STRING_TYPE).optional());
        descriptors
                .add(constrainedFields.withPath(prefixPath + "properties", "properties", "Custom attribute properties")
                        .type(JSON_ARRAY_TYPE).optional());
        descriptors.addAll(documentAttributeProperties(creation, prefixPath + "properties[]"));
        descriptors.add(constrainedFields
                .withPath(prefixPath + "dynamic", "dynamic", "Used in search request parsing only", "Defaults to true")
                .type(JSON_BOOLEAN_TYPE).optional());
        descriptors.add(constrainedFields
                .withPath(prefixPath + "jsonPath", "jsonPath",
                          "Used in search request. Define the JSON path to the related values in entities")
                .type(JSON_STRING_TYPE).optional());
        // ignore links
        ConstrainedFields ignoreFields = new ConstrainedFields(Resource.class);
        descriptors.add(ignoreFields.withPath("links", "links", "hateoas links").optional().ignored());
        ignoreFields = new ConstrainedFields(Link.class);
        descriptors.add(ignoreFields.withPath("links[].rel", "rel", "hateoas links rel").optional().ignored());
        descriptors.add(ignoreFields.withPath("links[].href", "href", "hateoas links href").optional().ignored());
        return descriptors;
    }

    public static List<FieldDescriptor> documentAttributeProperties(boolean creation, String prefix) {
        String prefixPath = Strings.isNullOrEmpty(prefix) ? "" : prefix + ".";
        ConstrainedFields constrainedFields = new ConstrainedFields(AttributeProperty.class);
        List<FieldDescriptor> descriptors = new ArrayList<>();
        // as attribute property list can be empty, we have to set everything as optional
        if (!creation) {
            descriptors.add(constrainedFields
                    .withPath(prefixPath + "id", prefixPath + "id", "Attribute property identifier")
                    .type(JSON_NUMBER_TYPE).optional());
        }
        descriptors.add(constrainedFields.withPath(prefixPath + "key", prefixPath + "key", "Custom key")
                .type(JSON_STRING_TYPE).optional());
        descriptors.add(constrainedFields.withPath(prefixPath + "value", prefixPath + "value", "Custom value")
                .type(JSON_STRING_TYPE).optional());
        return descriptors;
    }

    public static List<FieldDescriptor> documentFragment(boolean creation, String prefix) {
        String prefixPath = Strings.isNullOrEmpty(prefix) ? "" : prefix + ".";
        ConstrainedFields constrainedFields = new ConstrainedFields(Fragment.class);
        List<FieldDescriptor> descriptors = new ArrayList<>();
        // fragment being optional, we have to set all of its attribute as optional
        if (!creation) {
            descriptors.add(constrainedFields
                    .withPath(prefixPath + "id", "id", "Fragment identifier", "Must be a whole number")
                    .type(JSON_NUMBER_TYPE).optional());
        }
        descriptors.add(constrainedFields.withPath(prefixPath + "name", "name", "Fragment Name").type(JSON_STRING_TYPE)
                .optional());
        descriptors.add(constrainedFields
                .withPath(prefixPath + "description", "description", "Fragment description", "Optional")
                .type(JSON_STRING_TYPE).optional());
        descriptors.add(constrainedFields.withPath(prefixPath + "version", "version", "Fragment Version", "Optional")
                .type(JSON_STRING_TYPE).optional());
        return descriptors;
    }

    public static List<FieldDescriptor> documentAttributeRestriction(boolean creation, String prefix) {
        String prefixPath = Strings.isNullOrEmpty(prefix) ? "" : prefix + ".";
        ConstrainedFields constrainedFields = new ConstrainedFields(AttributeModel.class);
        List<FieldDescriptor> descriptors = new ArrayList<>();
        // as attribute restriction is not mandatory, we have to set everything as optional
        if (!creation) {
            descriptors.add(constrainedFields.withPath(prefixPath + "id", prefixPath + "id", "Restriction identifier")
                    .type(JSON_NUMBER_TYPE).optional());
        }
        descriptors.add(constrainedFields
                .withPath(prefixPath + "type", prefixPath + "type", "Restriction type",
                          "Available values: " + Arrays.stream(RestrictionType.values())
                                  .map(restrictionType -> restrictionType.name()).collect(Collectors.joining(", ")))
                .type(JSON_STRING_TYPE).optional());
        // document fields for RangeRestriction
        descriptors.add(constrainedFields
                .withPath(prefixPath + "min", prefixPath + "min", "Minimum possible value",
                          "Apply to restriction type LONG_RANGE & INTEGER_RANGE & DOUBLE_RANGE")
                .type(JSON_NUMBER_TYPE).optional());
        descriptors.add(constrainedFields
                .withPath(prefixPath + "max", prefixPath + "max", "Maximum possible value",
                          "Apply to restriction type LONG_RANGE & INTEGER_RANGE & DOUBLE_RANGE")
                .type(JSON_NUMBER_TYPE).optional());
        descriptors.add(constrainedFields
                .withPath(prefixPath + "minExcluded", prefixPath + "minExcluded",
                          "Whether the minimum values is to be excluded from the range",
                          "Defaults to false. Apply to restriction type LONG_RANGE & INTEGER_RANGE & DOUBLE_RANGE")
                .type(JSON_BOOLEAN_TYPE).optional());
        descriptors.add(constrainedFields
                .withPath(prefixPath + "maxExcluded", prefixPath + "maxExcluded",
                          "Whether the maximum values is to be excluded from the range",
                          "Defaults to false. Apply to restriction type LONG_RANGE & INTEGER_RANGE & DOUBLE_RANGE")
                .type(JSON_BOOLEAN_TYPE).optional());
        // document fields for EnumerationRestriction
        descriptors.add(
                        constrainedFields
                                .withPath(prefixPath + "acceptableValues", prefixPath + "acceptableValues",
                                          "Acceptable values", "Apply to restriction type ENUMERATION")
                                .type(JSON_ARRAY_TYPE).optional());

        // document fields for PatternRestriction
        descriptors
                .add(constrainedFields.withPath(prefixPath + "pattern", prefixPath + "pattern", "Validation pattern",
                                                "Apply to restriction type PATTERN")
                        .type(JSON_STRING_TYPE).optional());

        return descriptors;
    }

    @Before
    public void setUp() throws ModuleException {

        Model model = new Model();
        model.setName("DataModel");
        model.setType(EntityType.DATA);
        model.setDescription("Test");
        model.setVersion("1.0");
        modelTest = modelRepository.save(model);

        Model model2 = new Model();
        model2.setName("DataSetModel");
        model2.setType(EntityType.DATASET);
        model2.setDescription("Test");
        model2.setVersion("1.0");
        modelRepository.save(model2);

        Fragment fragment = new Fragment();
        fragment.setDescription("Test");
        fragment.setName("test");
        fragment.setVersion("1.0");
        FragmentRepository.save(fragment);

        AttributeModel attribute = new AttributeModel();
        attribute.setFragment(fragment);
        attribute.setLabel("Attribute1");
        attribute.setName("Attribute1");
        attribute.setType(AttributeType.STRING);
        attributeModelRepository.save(attribute);

        AttributeModel attribute2 = new AttributeModel();
        attribute2.setFragment(fragment);
        attribute2.setLabel("Attribute2");
        attribute2.setName("Attribute2");
        attribute2.setType(AttributeType.INTEGER);
        attributeModelRepository.save(attribute2);

        AttributeModel attribute3 = new AttributeModel();
        attribute3.setFragment(fragment);
        attribute3.setLabel("Attribute3");
        attribute3.setName("Attribute3");
        attribute3.setType(AttributeType.DATE_ISO8601);
        attributeModelRepository.save(attribute3);

        ModelAttrAssoc modelAttr = new ModelAttrAssoc();
        modelAttr.setAttribute(attribute);
        modelAttr.setModel(model);
        modelAttributeRepository.save(modelAttr);

        modelAttr = new ModelAttrAssoc();
        modelAttr.setAttribute(attribute2);
        modelAttr.setModel(model2);
        modelAttributeRepository.save(modelAttr);

        modelAttr = new ModelAttrAssoc();
        modelAttr.setAttribute(attribute3);
        modelAttr.setModel(model2);
        modelAttributeRepository.save(modelAttr);

    }

    @Test
    public void testGetModelAttrAssoc() {
        final Integer expectedSize = 1;
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer
                .addExpectation(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(expectedSize)));
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers
                .jsonPath("$[0].content.model.id", Matchers.equalTo(modelTest.getId().intValue())));
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers
                .jsonPath("$[0].content.attribute.jsonPath", Matchers.equalTo("properties.test.Attribute1")));

        requestBuilderCustomizer.addDocumentationSnippet(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("modelName").description("model name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE))));

        performDefaultGet(ModelAttrAssocController.BASE_MAPPING + ModelAttrAssocController.TYPE_MAPPING,
                          requestBuilderCustomizer, "Cannot get all attributes assoc", modelTest.getName());

    }

    /**
     * Test get attributes
     */
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Get model attributes to manage data models")
    @Test
    public void testGetAttributes() {

        // Define expectations
        final Integer expectedSize = 3;
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer
                .addExpectation(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(expectedSize)));

        // Perform test
        performDefaultGet(AttributeModelController.TYPE_MAPPING, requestBuilderCustomizer, "Cannot get all attributes");
    }

    @Test
    public void testGetAttributesAssocToModelType() {

        // Define expectations
        // There must be only one attribute associated to models of type DATA
        Integer expectedSize = 1;
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer
                .addExpectation(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(expectedSize)));

        // Perform test
        performDefaultGet(AttributeModelController.TYPE_MAPPING + AttributeModelController.ENTITY_TYPE_MAPPING,
                          requestBuilderCustomizer, "Cannot get all attributes", EntityType.DATA.toString());

        // There must be only two attributes associated to models of type DATASET
        expectedSize = 2;
        requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer
                .addExpectation(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(expectedSize)));

        performDefaultGet(AttributeModelController.TYPE_MAPPING + AttributeModelController.ENTITY_TYPE_MAPPING,
                          requestBuilderCustomizer, "Cannot get all attributes", EntityType.DATASET.toString());

        // There must be no attribute associated to collection models
        expectedSize = 0;
        requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer
                .addExpectation(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(expectedSize)));

        requestBuilderCustomizer
                .addDocumentationSnippet(RequestDocumentation
                        .pathParameters(RequestDocumentation.parameterWithName("pModelType").description("model type")
                                .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE),
                                            Attributes.key(RequestBuilderCustomizer.PARAM_CONSTRAINTS)
                                                    .value("Available values: " + Arrays.stream(EntityType.values())
                                                            .map(type -> type.name())
                                                            .collect(Collectors.joining(", "))))));

        performDefaultGet(AttributeModelController.TYPE_MAPPING + AttributeModelController.ENTITY_TYPE_MAPPING,
                          requestBuilderCustomizer, "Cannot get all attributes", EntityType.COLLECTION.toString());
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
    public void createFloatAttribute() {
        createAttribute("FLOAT_ATT", "float description", AttributeType.DOUBLE);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createFloatArrayAttribute() {
        createAttribute("FLOAT_ARRAY_ATT", "float array description", AttributeType.DOUBLE_ARRAY);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    public void createFloatIntervalAttribute() {
        createAttribute("FLOAT_INTERVAL_ATT", "float interval description", AttributeType.DOUBLE_INTERVAL);
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
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer
                .addExpectation(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(expectedSize)));

        requestBuilderCustomizer.customizeRequestParam().param(AttributeModelController.PARAM_FRAGMENT_NAME,
                                                               fragmentName);

        requestBuilderCustomizer.addDocumentationSnippet(RequestDocumentation
                .requestParameters(RequestDocumentation.parameterWithName(AttributeModelController.PARAM_FRAGMENT_NAME)
                        .description("fragment name")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE)
                                .value(JSON_STRING_TYPE))
                        .optional(), RequestDocumentation.parameterWithName("modelIds").description("model id").attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_ARRAY_TYPE), Attributes.key(RequestBuilderCustomizer.PARAM_CONSTRAINTS).value("Values must be whole numbers")).optional(), RequestDocumentation.parameterWithName(AttributeModelController.PARAM_TYPE).description("attribute type").attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE), Attributes.key(RequestBuilderCustomizer.PARAM_CONSTRAINTS).value("Available values: " + Arrays.stream(AttributeType.values()).map(type -> type.name()).collect(Collectors.joining(", ")))).optional()));

        performDefaultGet(AttributeModelController.TYPE_MAPPING, requestBuilderCustomizer,
                          "Should return result " + expectedSize + " attributes.");

    }

    /**
     * POST a new attribute
     *
     * @param pAttributeModel
     *            the attribute
     * @return {@link ResultActions}
     */
    private ResultActions createAttribute(AttributeModel pAttributeModel) {

        // Define expectations
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers
                .jsonPath("$.content.name", Matchers.equalTo(pAttributeModel.getName())));
        if (pAttributeModel.getDescription() != null) {
            requestBuilderCustomizer.addExpectation(MockMvcResultMatchers
                    .jsonPath("$.content.description", Matchers.equalTo(pAttributeModel.getDescription())));
        }
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers
                .jsonPath("$.content.type", Matchers.equalTo(pAttributeModel.getType().toString())));

        requestBuilderCustomizer.addDocumentationSnippet(PayloadDocumentation.requestFields(documentBody(true, "")));
        requestBuilderCustomizer
                .addDocumentationSnippet(PayloadDocumentation.responseFields(documentBody(false, "content")));

        return performDefaultPost(AttributeModelController.TYPE_MAPPING, pAttributeModel, requestBuilderCustomizer,
                                  "Consistent attribute should be created.");
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
     * @param pRestriction
     *            restriction
     * @return {@link ResultActions}
     */
    private ResultActions createAttribute(String pName, String pDescription, AttributeType pType, Fragment pFragment,
            AbstractRestriction pRestriction) {

        final AttributeModel attModel = AttributeModelBuilder.build(pName, pType, "ForTests").description(pDescription)
                .fragment(pFragment).get();
        attModel.setRestriction(pRestriction);
        return createAttribute(attModel);
    }

    private ResultActions createAttribute(String pName, String pDescription, AttributeType pType, Fragment pFragment) {
        return createAttribute(pName, pDescription, pType, pFragment, null);
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
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));
        requestBuilderCustomizer
                .addDocumentationSnippet(PayloadDocumentation.responseFields(documentBody(false, "content")));

        requestBuilderCustomizer.addDocumentationSnippet(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("pAttributeId").description("attribute id")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_NUMBER_TYPE))));

        performDefaultGet(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", requestBuilderCustomizer,
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
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());

        // Content
        final AttributeModel attModel = AttributeModelBuilder.build(attName, AttributeType.STRING, "ForTests")
                .withEnumerationRestriction(acceptableValues);

        performDefaultPost(AttributeModelController.TYPE_MAPPING, attModel, requestBuilderCustomizer,
                           "Cannot add attribute with enum restriction");

        // Define conflict expectations
        requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isConflict());

        // Same clone model ... replay
        final AttributeModel conflictAttModel = AttributeModelBuilder.build(attName, AttributeType.STRING, "ForTests")
                .withEnumerationRestriction(acceptableValues);

        performDefaultPost(AttributeModelController.TYPE_MAPPING, conflictAttModel, requestBuilderCustomizer,
                           "Conflict not detected");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Test
    public void getAllRestrictions() {
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isBadRequest());
        performDefaultGet(AttributeModelController.TYPE_MAPPING + RESTRICTION_MAPPING, requestBuilderCustomizer,
                          "Restriction must be retrieve by type");
    }

    @Test
    public void getRestrictionForString() {
        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());

        requestBuilderCustomizer.customizeRequestParam().param("type", AttributeType.STRING.toString());

        requestBuilderCustomizer
                .addDocumentationSnippet(RequestDocumentation
                        .requestParameters(RequestDocumentation.parameterWithName("type").description("Attribute type")
                                .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_STRING_TYPE),
                                            Attributes.key(RequestBuilderCustomizer.PARAM_CONSTRAINTS)
                                                    .value("Available values: " + Arrays.stream(AttributeType.values())
                                                            .map(type -> type.name())
                                                            .collect(Collectors.joining(", "))))));

        performDefaultGet(AttributeModelController.TYPE_MAPPING + RESTRICTION_MAPPING, requestBuilderCustomizer,
                          "STRING restriction should exists!");
    }

    /**
     * Test attribute update. Only description is updatable. FIXME : in same transaction, this test doesn't work even if
     * correct data is stored in database.
     */
    @Test
    @Ignore
    public void updateAttributeModel() {
        final String name = "UPDATABLE";
        final AttributeType type = AttributeType.URL;
        final AttributeModel attMod = AttributeModelBuilder.build(name, type, "ForTests").description("DESC").get();

        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());

        final ResultActions resultActions = performDefaultPost(AttributeModelController.TYPE_MAPPING, attMod,
                                                               requestBuilderCustomizer,
                                                               "Attribute should be created.");

        final String json = payload(resultActions);
        final Integer id = JsonPath.read(json, JSON_ID);

        // Try to alter attribute
        attMod.setId(Long.valueOf(id));
        attMod.setName("CHANGE");
        final String description = "NEW DESC";
        attMod.setDescription(description);
        attMod.setType(AttributeType.BOOLEAN);

        performDefaultPut(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", attMod, requestBuilderCustomizer,
                          "Update should be successful.", id);

        // Perform a get attribute to retrieved real database content and avoid false negative

        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.is(id)));
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(name)));
        requestBuilderCustomizer
                .addExpectation(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(description)));
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.jsonPath("$.type", Matchers.is(type.toString())));

        requestBuilderCustomizer
                .addDocumentationSnippet(PayloadDocumentation.responseFields(documentBody(false, "content")));

        requestBuilderCustomizer.addDocumentationSnippet(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("pAttributeId").description("attribute id")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_NUMBER_TYPE))));

        performDefaultGet(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", requestBuilderCustomizer,
                          "Cannot retrieve attribute", id);
    }

    /**
     * Check if removing a restriction works
     */
    @Test
    @Ignore
    public void removeRestriction() {
        final AttributeModel attMod = AttributeModelBuilder.build("attModRestr", AttributeType.STRING, "ForTests")
                .description("desc").withPatternRestriction("pattern");

        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());

        final ResultActions resultActions = performDefaultPost(AttributeModelController.TYPE_MAPPING, attMod,
                                                               requestBuilderCustomizer,
                                                               "Attribute should be created.");

        final String json = payload(resultActions);
        final Integer id = JsonPath.read(json, JSON_ID);

        attMod.setId(Long.valueOf(id));
        attMod.setRestriction(null);

        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.is(id)));
        // expectations.add(MockMvcResultMatchers.jsonPath("$", Matchers.hasProperty("restrictions")));

        performDefaultPut(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", attMod, requestBuilderCustomizer,
                          "Restriction should be deleted.", id);

    }

    /**
     * Test restriction creation and update
     */
    @Test
    public void createAndUpdateAttributeWithRestriction() {
        AttributeModel attModel = AttributeModelBuilder.build("NB_OBJECTS", AttributeType.INTEGER, "ForTests")
                .withIntegerRangeRestriction(1, 3, false, false);
        ResultActions resultActions = createAttribute(attModel);

        String json = payload(resultActions);
        Integer id = JsonPath.read(json, JSON_ID);

        // Set a new restriction
        attModel.setId(Long.valueOf(id));
        IntegerRangeRestriction irr = new IntegerRangeRestriction();
        irr.setMin(10);
        irr.setMax(100);
        attModel.setRestriction(irr);

        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());

        requestBuilderCustomizer
                .addDocumentationSnippet(PayloadDocumentation.responseFields(documentBody(false, "content")));

        requestBuilderCustomizer.addDocumentationSnippet(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("pAttributeId").description("attribute id")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_NUMBER_TYPE))));

        resultActions = performDefaultPut(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", attModel,
                                          requestBuilderCustomizer, "Update should be successful.", id);
    }

    /**
     * Test restriction creation and update
     */
    @Test
    public void createAndUpdateAttributeWithRestriction2() {
        AttributeModel attModel = AttributeModelBuilder.build("NB_OBJECTS", AttributeType.LONG, "ForTests")
                .withLongRangeRestriction(Long.MIN_VALUE, Long.MAX_VALUE, false, false);
        ResultActions resultActions = createAttribute(attModel);

        String json = payload(resultActions);
        Integer id = JsonPath.read(json, JSON_ID);

        // Set a new restriction
        attModel.setId(Long.valueOf(id));
        LongRangeRestriction irr = new LongRangeRestriction();
        irr.setMin(10L);
        irr.setMax(100L);
        attModel.setRestriction(irr);

        RequestBuilderCustomizer requestBuilderCustomizer = getNewRequestBuilderCustomizer();
        requestBuilderCustomizer.addExpectation(MockMvcResultMatchers.status().isOk());

        requestBuilderCustomizer
                .addDocumentationSnippet(PayloadDocumentation.responseFields(documentBody(false, "content")));

        requestBuilderCustomizer.addDocumentationSnippet(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("pAttributeId").description("attribute id")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value(JSON_NUMBER_TYPE))));

        resultActions = performDefaultPut(AttributeModelController.TYPE_MAPPING + "/{pAttributeId}", attModel,
                                          requestBuilderCustomizer, "Update should be successful.", id);
    }
}
