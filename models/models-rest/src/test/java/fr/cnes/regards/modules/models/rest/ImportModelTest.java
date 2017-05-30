/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

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
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.ComputationMode;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;

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

        performDefaultFileUploadPost(ModelController.TYPE_MAPPING + "/import", filePath, expectations,
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

        PluginMetaData metaData = new PluginMetaData();
        metaData.setPluginId("tata");
        metaData.setAuthor("toto");
        metaData.setDescription("titi");
        metaData.setVersion("tutu");
        metaData.getInterfaceNames().add(IComputedAttribute.class.getName());
        metaData.setPluginClassName(TestComputedAttribute.class.getName());
        PluginConfiguration conf = new PluginConfiguration(metaData, "ContactPluginConfTest");
        pluginService.addPluginPackage(TestComputedAttribute.class.getPackage().getName());
        pluginService.addPluginPackage(IComputedAttribute.class.getPackage().getName());
        conf = pluginService.savePluginConfiguration(conf);

        importModel("model_it.xml");

        // Get model from repository
        final Model model = modelRepository.findByName("sample");
        Assert.assertNotNull(model);

        // Get model attributes
        final List<ModelAttrAssoc> modAtts = modelAttributeService.getModelAttrAssocs(model.getId());
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

}
