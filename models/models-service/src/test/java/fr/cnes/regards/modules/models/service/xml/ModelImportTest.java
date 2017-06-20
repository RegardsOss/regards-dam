/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.domain.ComputationMode;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;

/**
 * @author Marc Sordi
 */
public class ModelImportTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelImportTest.class);

    /**
     * Import model definition file from resources directory
     *
     * @param pFilename filename
     * @return list of created model attributes
     * @throws ModuleException if error occurs
     */
    private Iterable<ModelAttrAssoc> importModel(String pFilename) throws ModuleException {
        try {
            final InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));
            return XmlImportHelper.importModel(input, new ArrayList<>());
        } catch (IOException e) {
            String errorMessage = "Cannot import minimal model";
            LOGGER.debug(errorMessage);
            throw new AssertionError(errorMessage);
        }
    }

    @Test
    public void importModelTest() throws ModuleException {
        Iterable<ModelAttrAssoc> modelAtts = importModel("sample-model.xml");
        checkImportedModel(modelAtts);
    }

    /**
     * Same test as before but XML has no default value
     *
     * @throws ModuleException if problem occurs!
     */
    @Test
    public void importMinimalModelTest() throws ModuleException {
        Iterable<ModelAttrAssoc> modelAtts = importModel("sample-model-minimal.xml");
        checkImportedModel(modelAtts);
    }

    /**
     * Check imported model
     *
     * @param pModelAtts list of {@link ModelAttrAssoc}
     */
    private void checkImportedModel(Iterable<ModelAttrAssoc> pModelAtts) {
        final int expectedSize = 3;
        Assert.assertEquals(expectedSize, Iterables.size(pModelAtts));

        for (ModelAttrAssoc modAtt : pModelAtts) {

            // Check model info
            Assert.assertEquals("sample", modAtt.getModel().getName());
            Assert.assertEquals("Sample mission", modAtt.getModel().getDescription());
            Assert.assertEquals(EntityType.COLLECTION, modAtt.getModel().getType());

            // Check attributes
            final AttributeModel attModel = modAtt.getAttribute();
            Assert.assertNotNull(attModel);
            Assert.assertEquals("forTests", attModel.getLabel());

            if ("att_string".equals(attModel.getName())) {
                Assert.assertNotNull(attModel.getFragment());
                Assert.assertTrue(attModel.getFragment().isDefaultFragment());
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.STRING, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertTrue(attModel.isOptional());
                Assert.assertNull(attModel.getRestriction());
                Assert.assertEquals(ComputationMode.GIVEN, modAtt.getMode());
            }

            if ("att_boolean".equals(attModel.getName())) {
                Assert.assertNotNull(attModel.getFragment());
                Assert.assertTrue(attModel.getFragment().isDefaultFragment());
                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.BOOLEAN, attModel.getType());
                Assert.assertTrue(attModel.isAlterable());
                Assert.assertFalse(attModel.isOptional());
                Assert.assertNull(attModel.getRestriction());
                Assert.assertEquals(ComputationMode.GIVEN, modAtt.getMode());
            }

            if ("CRS".equals(attModel.getName())) {
                Assert.assertNotNull(attModel.getFragment());
                Assert.assertEquals("GEO", attModel.getFragment().getName());
                Assert.assertEquals("Geographic information", attModel.getFragment().getDescription());

                Assert.assertNull(attModel.getDescription());
                Assert.assertEquals(AttributeType.STRING, attModel.getType());
                Assert.assertFalse(attModel.isAlterable());
                Assert.assertFalse(attModel.isOptional());

                Assert.assertNotNull(attModel.getRestriction());
                Assert.assertTrue(attModel.getRestriction() instanceof EnumerationRestriction);
                final EnumerationRestriction er = (EnumerationRestriction) attModel.getRestriction();
                Assert.assertTrue(er.getAcceptableValues().contains("Earth"));
                Assert.assertTrue(er.getAcceptableValues().contains("Mars"));
                Assert.assertTrue(er.getAcceptableValues().contains("Venus"));

                Assert.assertEquals(ComputationMode.COMPUTED, modAtt.getMode());
            }

        }
    }
}
