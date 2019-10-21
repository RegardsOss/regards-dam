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
package fr.cnes.regards.modules.feature.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.validation.Errors;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.validation.ErrorTranslator;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.model.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.service.validation.ValidationMode;
import fr.cnes.regards.modules.model.service.xml.IComputationPluginService;
import fr.cnes.regards.modules.model.service.xml.XmlImportHelper;

/**
 * Test feature validation
 *
 * @author Marc SORDI
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature_validation" })
@ActiveProfiles("noscheduler")
public class FeatureValidationTest extends AbstractFeatureMultitenantServiceTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureValidationTest.class);

    @Autowired
    private IComputationPluginService cps;

    @Autowired
    private IModelAttrAssocClient modelAttrAssocClientMock;

    @Autowired
    private IFeatureValidationService validationService;

    /**
     * Import model definition file from resources directory
     */
    private Iterable<ModelAttrAssoc> importModel(String filename) throws ModuleException {
        try (InputStream input = this.getClass().getResourceAsStream(filename)) {
            return XmlImportHelper.importModel(input, cps);
        } catch (IOException e) {
            String errorMessage = "Cannot import model";
            LOGGER.debug(errorMessage);
            throw new AssertionError(errorMessage);
        }
    }

    @Test
    public void validationTest() throws ModuleException {

        String modelName = "FEATURE01";

        // Import sample model
        Iterable<ModelAttrAssoc> assocs = importModel("feature_model_01.xml");

        // Set model client mock
        List<Resource<ModelAttrAssoc>> resources = new ArrayList<>();
        for (ModelAttrAssoc assoc : assocs) {
            resources.add(new Resource<ModelAttrAssoc>(assoc));
        }
        Mockito.when(modelAttrAssocClientMock.getModelAttrAssocs(modelName)).thenReturn(ResponseEntity.ok(resources));

        // Init feature without files and properties
        Feature feature = Feature.build("id01", null, IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA,
                                        modelName);

        // Validate feature
        Errors errors = validationService.validate(feature, ValidationMode.CREATION);

        if (errors.hasErrors()) {
            // Missing required properties
            Assert.assertEquals(2, errors.getErrorCount());
            LOGGER.error(ErrorTranslator.getErrorsAsString(errors));
        } else {
            Assert.fail();
        }

        // Add required properties and validate
        feature.withProperties(IProperty
                .set(IProperty.buildString("data_type", "TYPE01"),
                     IProperty.buildObject("file_characterization", IProperty.buildBoolean("valid", Boolean.TRUE))));

        errors = validationService.validate(feature, ValidationMode.CREATION);

        if (errors.hasErrors()) {
            LOGGER.error(ErrorTranslator.getErrorsAsString(errors));
            Assert.fail();
        }

        // Update feature with non alterable properties
        feature.setUrn(FeatureUniformResourceName.pseudoRandomUrn(FeatureIdentifier.FEATURE, EntityType.DATA,
                                                                  getDefaultTenant(), 1));

        errors = validationService.validate(feature, ValidationMode.PATCH);

        if (errors.hasErrors()) {
            // Unexpected non alterable properties
            Assert.assertEquals(1, errors.getErrorCount());
        } else {
            Assert.fail();
        }

        // Update feature with authorized properties
        //        feature.
    }
}
