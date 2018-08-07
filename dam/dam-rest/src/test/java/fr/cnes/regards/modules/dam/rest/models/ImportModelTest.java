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
package fr.cnes.regards.modules.dam.rest.models;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.dao.models.IModelRepository;
import fr.cnes.regards.modules.dam.domain.models.ComputationMode;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.dam.rest.models.ModelController;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;

/**
 * @author Marc Sordi
 */
@MultitenantTransactional
public class ImportModelTest extends AbstractRegardsTransactionalIT {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportModelTest.class);

    /**
     * Reference test model
     */
    private static final String REFERENCE_MODEL = "model1.xml";

    /**
     * Model repository
     */
    @Autowired
    private IModelRepository modelRepository;

    /**
     * Model attribute service
     */
    @Autowired
    private IModelAttrAssocService modelAttributeService;

    @Autowired
    private IPluginService pluginService;

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    private void importModel(String pFilename) {
        importModel(pFilename, MockMvcResultMatchers.status().isCreated());
    }

    private void importModel(String pFilename, ResultMatcher pMatcher) {
        final Path filePath = Paths.get("src", "test", "resources", pFilename);

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(pMatcher);

        performDefaultFileUpload(ModelController.TYPE_MAPPING + "/import", filePath, expectations,
                                 "Should be able to import a model");
    }

    /**
     * Import model
     *
     * @throws ModuleException if error occurs!
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Import model - Allows to share model or add predefined model ")
    public void importSingleModel() throws ModuleException {

        importModel("model_it.xml");

        // Get model from repository
        String modelName = "sample";
        final Model model = modelRepository.findByName(modelName);
        Assert.assertNotNull(model);

        // Get model attributes
        final List<ModelAttrAssoc> modAtts = modelAttributeService.getModelAttrAssocs(modelName);
        Assert.assertNotNull(modAtts);
        final int expectedSize = 3;
        Assert.assertEquals(expectedSize, modAtts.size());

        for (ModelAttrAssoc modAtt : modAtts) {

            final AttributeModel attModel = modAtt.getAttribute();
            Assert.assertEquals("forTests", attModel.getLabel());

            if ("att_string".equals(attModel.getName())) {
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.STRING, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertTrue(attModel.isOptional());
                Assert.assertNull(attModel.getRestriction());
                Assert.assertEquals(Fragment.buildDefault(), attModel.getFragment());
                Assert.assertEquals(ComputationMode.GIVEN, modAtt.getMode());
            }

            if ("CRS".equals(attModel.getName())) {
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.STRING, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertFalse(attModel.isOptional());
                Assert.assertNotNull(attModel.getRestriction());
                Assert.assertTrue(attModel.getRestriction() instanceof EnumerationRestriction);
                final EnumerationRestriction er = (EnumerationRestriction) attModel.getRestriction();
                final int expectedErSize = 3;
                Assert.assertEquals(expectedErSize, er.getAcceptableValues().size());
                Assert.assertTrue(er.getAcceptableValues().contains("Earth"));
                Assert.assertTrue(er.getAcceptableValues().contains("Mars"));
                Assert.assertTrue(er.getAcceptableValues().contains("Venus"));

                Assert.assertEquals("GEO", attModel.getFragment().getName());

                Assert.assertEquals(ComputationMode.GIVEN, modAtt.getMode());
            }

        }
    }

    @Test
    public void importCompatibleModels() {
        importModel(REFERENCE_MODEL);
        importModel("model2.xml");
    }

    @Test
    public void importCompatibleModels2() {
        importModel(REFERENCE_MODEL);
        importModel("model3.xml");
    }

    @Test
    public void importIncompatibleModels() {
        importModel(REFERENCE_MODEL);
        importModel("model4.xml", MockMvcResultMatchers.status().isBadRequest());
    }

    // @Test(expected = ImportException.class)
    // public void importWrongModel() {
    // importModel("wrong_model.xml");
    // }
}