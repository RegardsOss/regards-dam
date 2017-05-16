/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.plugin.MinDateComputePlugin;
import fr.cnes.regards.modules.entities.service.plugin.NonUsable;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(locations = { "classpath:test.properties" })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ServiceConfiguration.class })
@MultitenantTransactional
public class EntitiesServiceIT {

    private static final String datasetModelFileName = "datasetModel.xml";

    @Autowired
    private IEntitiesService entitiesService;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepos;

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IModelRepository modelRepository;

    private Dataset dataset;

    private PluginConfiguration confNonUsable;

    private PluginConfiguration confMin;

    @Before
    public void init() throws ModuleException {
        datasetRepository.deleteAll();
        modelRepository.deleteAll();
        pluginConfRepos.deleteAll();

        // first initialize the pluginConfiguration for the attributes
        jwtService.injectMockToken("PROJECT", "ADMIN");
        pluginService.addPluginPackage(NonUsable.class.getPackage().getName());
        // create a pluginConfiguration with a label for min
        List<PluginParameter> parametersMin = PluginParametersFactory.build()
                .addParameter("resultAttributeName", "minDate").getParameters();
        PluginMetaData metadataMin = new PluginMetaData();
        metadataMin.setPluginId("MinDateComputePlugin");
        metadataMin.setAuthor("toto");
        metadataMin.setDescription("titi");
        metadataMin.setVersion("tutu");
        metadataMin.getInterfaceNames().add(IComputedAttribute.class.getName());
        metadataMin.setPluginClassName(MinDateComputePlugin.class.getName());
        confMin = new PluginConfiguration(metadataMin, "MinDateTestConf");
        confMin.setParameters(parametersMin);
        confMin = pluginService.savePluginConfiguration(confMin);
        // create a pluginConfiguration with a label
        List<PluginParameter> parametersNonUsable = PluginParametersFactory.build()
                .addParameter("resultAttributeName", "maxDate").getParameters();
        PluginMetaData metadataNonUsable = new PluginMetaData();
        metadataNonUsable.setPluginId("NonUsable");
        metadataNonUsable.setAuthor("toto");
        metadataNonUsable.setDescription("titi");
        metadataNonUsable.setVersion("tutu");
        metadataNonUsable.getInterfaceNames().add(IComputedAttribute.class.getName());
        metadataNonUsable.setPluginClassName(NonUsable.class.getName());
        confNonUsable = new PluginConfiguration(metadataNonUsable, "ConfFromNonUsablePlugin");
        confNonUsable.setParameters(parametersNonUsable);
        confNonUsable = pluginService.savePluginConfiguration(confNonUsable);
        // then import the model of dataset
        importModel(datasetModelFileName);
        // instantiate the dataset
        Model datasetModel = modelService.getModelByName("datasetModel");
        dataset = new Dataset(datasetModel, "PROJECT", "Test pour le fun");
        dataset.setLicence("pLicence");
    }

    @Test
    public void testGetComputationPlugins() throws ModuleException {
        Set<IComputedAttribute<Dataset, ?>> results = entitiesService.getComputationPlugins(dataset);
        for (IComputedAttribute<Dataset, ?> plugin : results) {
            Assert.assertEquals(MinDateComputePlugin.class, plugin.getClass());
        }
    }

    /**
     * Import model definition file from resources directory
     *
     * @param pFilename filename
     * @throws ModuleException if error occurs
     */
    private void importModel(String pFilename) throws ModuleException {
        try {
            final InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", pFilename));
            modelService.importModel(input);
        } catch (IOException e) {
            String errorMessage = "Cannot import " + pFilename;
            throw new AssertionError(errorMessage);
        }
    }
}
