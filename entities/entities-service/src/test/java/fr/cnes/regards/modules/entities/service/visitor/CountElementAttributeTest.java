/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.visitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.LongAttribute;
import fr.cnes.regards.modules.entities.plugin.CountElementAttribute;
import fr.cnes.regards.modules.entities.service.ServiceConfiguration;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(locations = { "classpath:test.properties" })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ServiceConfiguration.class })
@MultitenantTransactional
public class CountElementAttributeTest {

    private static final String datasetModelFileName = "datasetModelCount.xml";

    private static final Logger LOG = LoggerFactory.getLogger(CountElementAttributeTest.class);

    @Autowired
    private IModelService modelService;

    @Autowired
    private IAttributeModelService attModelService;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private JWTService jwtService;

    private Model dataModel;

    private CountElementAttribute countPlugin;

    private AttributeModel attributeToCompute;

    @Before
    public void init() throws ModuleException, NoSuchMethodException, SecurityException {
        jwtService.injectMockToken("PROJECT", "ADMIN");
        pluginService.addPluginPackage(IComputedAttribute.class.getPackage().getName());
        pluginService.addPluginPackage(CountElementAttribute.class.getPackage().getName());
        // create a pluginConfiguration with a label
        List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter("attributeToComputeName", "count").getParameters();
        PluginMetaData metadata = new PluginMetaData();
        metadata.setPluginId("CountElementAttribute");
        metadata.setAuthor("toto");
        metadata.setDescription("titi");
        metadata.setVersion("tutu");
        metadata.setInterfaceName(IComputedAttribute.class.getName());
        metadata.setPluginClassName(CountElementAttribute.class.getName());
        PluginConfiguration conf = new PluginConfiguration(metadata, "CountElementTestConf");
        conf.setParameters(parameters);
        conf = pluginService.savePluginConfiguration(conf);
        // get a model for Dataset
        importModel(datasetModelFileName);
        // get a model for DataObject
        dataModel = Model.build("dataModel", "pDescription", EntityType.DATA);
        // instanciate the plugin
        countPlugin = pluginService.getPlugin(conf.getId());
        attributeToCompute = attModelService.findByNameAndFragmentName("count", null);
    }

    @Test
    @Ignore
    public void testCountBuild() {
        DataObject obj = new DataObject();
        obj.setModel(dataModel);
        obj.setIpId(new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, "tenant", UUID.randomUUID(), 1));
        obj.setLabel("data for test");
        List<DataObject> objs = Lists.newArrayList(obj, obj, obj, obj, obj, obj, obj, obj);
        // countPlugin.compute(objs);
        // countPlugin.compute(objs);
        Long result = countPlugin.getResult();
        Assert.assertEquals(new Long(objs.size() * 2), result);
        AttributeBuilderVisitor visitor = new AttributeBuilderVisitor();
        AbstractAttribute<?> attribute = countPlugin.accept(visitor);
        Assert.assertTrue(attribute instanceof LongAttribute);
        Assert.assertEquals(attributeToCompute.getName(), attribute.getName());
        Assert.assertEquals(result, attribute.getValue());
    }

    /**
     * Import model definition file from resources directory
     *
     * @param pFilename filename
     * @return list of created model attributes
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
