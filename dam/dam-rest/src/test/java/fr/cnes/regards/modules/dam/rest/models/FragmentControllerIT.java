/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.models;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Strings;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.ConstrainedFields;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.dao.models.IFragmentRepository;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;

/**
 *
 * Test fragment
 *
 * @author Marc Sordi
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=fragments" })
@MultitenantTransactional
public class FragmentControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentControllerIT.class);

    /**
     * JSON path
     */
    private static final String JSON_ID = "$.content.id";

    /**
     * Fragment repository to populate database for testing
     */
    @Autowired
    private IFragmentRepository fragmentRepository;

    /**
     * Attribute model service
     */
    @Autowired
    private IAttributeModelService attributeModelService;

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Test
    public void createEmptyFragmentTest() {

        final Fragment fragment = new Fragment();

        // Define expectations
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isUnprocessableEntity());

        performDefaultPost(FragmentController.TYPE_MAPPING, fragment, requestBuilderCustomizer,
                           "Empty fragment shouldn't be created.");
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Create model fragment (an object containing simple attributes)")
    public void addGeoFragment() {
        final Fragment fragment = Fragment.buildFragment("GEO", "Geo description");

        // Define expectations
        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());
        requestBuilderCustomizer.expect(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));

        requestBuilderCustomizer.document(PayloadDocumentation.requestFields(documentBody(true, "")));
        requestBuilderCustomizer.document(PayloadDocumentation.responseFields(documentBody(false, "content")));

        performDefaultPost(FragmentController.TYPE_MAPPING, fragment, requestBuilderCustomizer,
                           "Fragment cannot be created.");
    }

    @Test
    public void getAllFragment() throws ModuleException {
        populateDatabase();

        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());
        int expectedSize = 3;
        requestBuilderCustomizer.expect(MockMvcResultMatchers.jsonPath("$..content", Matchers.hasSize(expectedSize)));

        performDefaultGet(FragmentController.TYPE_MAPPING, requestBuilderCustomizer, "Should return all fragments");
    }

    /**
     * Export fragment
     *
     * @throws ModuleException
     *             module exception
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Export fragment - Allows to share model fragment")
    public void exportFragment() throws ModuleException {
        populateDatabase();

        final Fragment defaultFragment = fragmentRepository.findByName(Fragment.getDefaultName());

        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isOk());

        requestBuilderCustomizer.document(RequestDocumentation
                .pathParameters(RequestDocumentation.parameterWithName("fragmentId").description("Fragment identifier")
                        .attributes(Attributes.key(RequestBuilderCustomizer.PARAM_TYPE).value("Number"), Attributes
                                .key(RequestBuilderCustomizer.PARAM_CONSTRAINTS).value("Should be a whole number"))));

        final ResultActions resultActions = performDefaultGet(FragmentController.TYPE_MAPPING + "/{fragmentId}/export",
                                                              requestBuilderCustomizer, "Should return result",
                                                              defaultFragment.getId());

        assertMediaType(resultActions, MediaType.APPLICATION_OCTET_STREAM);
        Assert.assertNotNull(payload(resultActions));
    }

    /**
     * Import fragment
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Import fragment - Allows to share model fragment")
    public void importFragment() throws ModuleException {

        final Path filePath = Paths.get("src", "test", "resources", "fragment_it.xml");

        RequestBuilderCustomizer requestBuilderCustomizer = customizer();
        requestBuilderCustomizer.expect(MockMvcResultMatchers.status().isCreated());

        performDefaultFileUpload(FragmentController.TYPE_MAPPING + "/import", filePath, requestBuilderCustomizer,
                                 "Should be able to import a fragment");

        // Get fragment from repository
        final String fragmentName = "IMPORT_FRAGMENT";
        final Fragment importedFragment = fragmentRepository.findByName(fragmentName);

        // Get fragment attributes
        final List<AttributeModel> attModels = attributeModelService.findByFragmentId(importedFragment.getId());
        Assert.assertEquals(2, attModels.size());

        for (AttributeModel attModel : attModels) {

            // Check fragment
            Assert.assertEquals(fragmentName, attModel.getFragment().getName());
            Assert.assertEquals("Imported fragment from integration test", attModel.getFragment().getDescription());
            Assert.assertEquals("forTests", attModel.getLabel());

            if ("IT_BOOLEAN".equals(attModel.getName())) {
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.BOOLEAN, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertTrue(attModel.isOptional());
                Assert.assertNull(attModel.getRestriction());
            }

            if ("IT_STRING".equals(attModel.getName())) {
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.STRING, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertFalse(attModel.isOptional());

                Assert.assertNotNull(attModel.getRestriction());
                Assert.assertTrue(attModel.getRestriction() instanceof EnumerationRestriction);
                final EnumerationRestriction er = (EnumerationRestriction) attModel.getRestriction();
                final int expectedValSize = 3;
                Assert.assertEquals(expectedValSize, er.getAcceptableValues().size());
                Assert.assertTrue(er.getAcceptableValues().contains("junit"));
                Assert.assertTrue(er.getAcceptableValues().contains("testng"));
                Assert.assertTrue(er.getAcceptableValues().contains("selenium"));
            }
        }
    }

    public static List<FieldDescriptor> documentBody(boolean creation, String prefix) {
        String prefixPath = Strings.isNullOrEmpty(prefix) ? "" : prefix + ".";
        ConstrainedFields constrainedFields = new ConstrainedFields(Fragment.class);
        List<FieldDescriptor> descriptors = new ArrayList<>();
        if (!creation) {
            descriptors.add(constrainedFields.withPath(prefixPath + "id", "id", "Fragment identifier",
                                                       "Must be a whole number"));
        }
        descriptors.add(constrainedFields.withPath(prefixPath + "name", "name", "Fragment Name"));
        descriptors.add(constrainedFields
                .withPath(prefixPath + "description", "description", "Fragment description", "Optional")
                .type(JSON_STRING_TYPE).optional());
        descriptors.add(constrainedFields.withPath(prefixPath + "version", "version", "Fragment Version", "Optional")
                .type(JSON_STRING_TYPE).optional());
        // ignore links
        ConstrainedFields ignoreFields = new ConstrainedFields(Resource.class);
        descriptors.add(ignoreFields.withPath("links", "links", "hateoas links").optional().ignored());
        ignoreFields = new ConstrainedFields(Link.class);
        descriptors.add(ignoreFields.withPath("links[].rel", "rel", "hateoas links rel").optional().ignored());
        descriptors.add(ignoreFields.withPath("links[].href", "href", "hateoas links href").optional().ignored());
        return descriptors;
    }

    private void populateDatabase() throws ModuleException {
        fragmentRepository.save(Fragment.buildDefault());
        fragmentRepository.save(Fragment.buildFragment("Geo", "Geographic information"));
        fragmentRepository.save(Fragment.buildFragment("Contact", "Contact card"));

        final AttributeModel attModel = AttributeModelBuilder.build("VFIRST", AttributeType.BOOLEAN, "ForTests")
                .withoutRestriction();
        attributeModelService.addAttribute(attModel, false);
    }
}
