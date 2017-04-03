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
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.LongAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.plugin.SumIntegerAttribute;
import fr.cnes.regards.modules.entities.plugin.SumLongAttribute;
import fr.cnes.regards.modules.entities.service.ServiceConfiguration;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IAttributeModelService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(locations = { "classpath:test.properties" })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ServiceConfiguration.class })
@MultitenantTransactional
public class SumAttributeTest {

    private static final String dataModelFileName = "dataModelSize.xml";

    private static final String datasetModelFileName = "datasetModelSize.xml";

    private static final String intSizeAttName = "sizeInteger";

    private static final String longSizeAttName = "sizeLong";

    private static final Logger LOG = LoggerFactory.getLogger(SumAttributeTest.class);

    @Autowired
    private IModelService modelService;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private IAttributeModelService attModelService;

    private Model dataModel;

    private SumIntegerAttribute sumIntPlugin;

    private SumLongAttribute sumLongPlugin;

    private AttributeModel intAttributeToCompute;

    private AttributeModel longAttributeToCompute;

    @Before
    public void init() throws ModuleException, NoSuchMethodException, SecurityException {
        jwtService.injectMockToken("PROJECT", "ADMIN");
        pluginService.addPluginPackage(IComputedAttribute.class.getPackage().getName());
        pluginService.addPluginPackage(SumIntegerAttribute.class.getPackage().getName());
        // create a pluginConfiguration with a label for min
        List<PluginParameter> parametersInteger = PluginParametersFactory.build()
                .addParameter("attributeToComputeName", "sizeInteger").getParameters();
        PluginMetaData metadataInteger = new PluginMetaData();
        metadataInteger.setPluginId("SumIntegerAttribute");
        metadataInteger.setAuthor("toto");
        metadataInteger.setDescription("titi");
        metadataInteger.setVersion("tutu");
        metadataInteger.setInterfaceName(IComputedAttribute.class.getName());
        metadataInteger.setPluginClassName(SumIntegerAttribute.class.getName());
        PluginConfiguration confInteger = new PluginConfiguration(metadataInteger, "SumIntegerTestConf");
        confInteger.setParameters(parametersInteger);
        confInteger = pluginService.savePluginConfiguration(confInteger);
        // create a pluginConfiguration with a label
        List<PluginParameter> parametersLong = PluginParametersFactory.build()
                .addParameter("attributeToComputeName", "sizeLong").getParameters();
        PluginMetaData metadataLong = new PluginMetaData();
        metadataLong.setPluginId("SumLongAttribute");
        metadataLong.setAuthor("toto");
        metadataLong.setDescription("titi");
        metadataLong.setVersion("tutu");
        metadataLong.setInterfaceName(IComputedAttribute.class.getName());
        metadataLong.setPluginClassName(SumLongAttribute.class.getName());
        PluginConfiguration confLong = new PluginConfiguration(metadataLong, "SumLongTestConf");
        confLong.setParameters(parametersLong);
        confLong = pluginService.savePluginConfiguration(confLong);
        // get a model for Dataset
        importModel(datasetModelFileName);
        // get a model for DataObject
        importModel(dataModelFileName);
        dataModel = modelService.getModelByName("dataModel");
        // instanciate the plugin
        sumIntPlugin = pluginService.getPlugin(confInteger.getId());
        sumLongPlugin = pluginService.getPlugin(confLong.getId());
        intAttributeToCompute = attModelService.findByNameAndFragmentName("sizeInteger", null);
        longAttributeToCompute = attModelService.findByNameAndFragmentName("sizeLong", null);
    }

    @Test
    public void testSum() {
        // create the objects
        DataObject obj1 = new DataObject();
        obj1.setModel(dataModel);
        obj1.setIpId(new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, "tenant", UUID.randomUUID(), 1));
        obj1.setLabel("data for test");
        DataObject obj2 = new DataObject();
        obj2.setModel(dataModel);
        obj2.setIpId(new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, "tenant", UUID.randomUUID(), 1));
        obj2.setLabel("data for test");
        DataObject obj3 = new DataObject();
        obj3.setModel(dataModel);
        obj3.setIpId(new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, "tenant", UUID.randomUUID(), 1));
        obj3.setLabel("data for test");
        DataObject obj4 = new DataObject();
        obj4.setModel(dataModel);
        obj4.setIpId(new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, "tenant", UUID.randomUUID(), 1));
        obj4.setLabel("data for test");
        // add the min date attribute
        obj1.getProperties().add(AttributeBuilder.forType(AttributeType.INTEGER, intSizeAttName, 1));
        obj2.getProperties().add(AttributeBuilder.forType(AttributeType.INTEGER, intSizeAttName, 1));
        obj3.getProperties().add(AttributeBuilder.forType(AttributeType.INTEGER, intSizeAttName, 1));
        // add the max date attribute
        obj1.getProperties().add(AttributeBuilder.forType(AttributeType.LONG, longSizeAttName, 1L));
        obj2.getProperties().add(AttributeBuilder.forType(AttributeType.LONG, longSizeAttName, 1L));
        obj3.getProperties().add(AttributeBuilder.forType(AttributeType.LONG, longSizeAttName, 1L));
        List<DataObject> objs = Lists.newArrayList(obj1, obj2, obj3, obj4);
        sumIntPlugin.compute(objs);
        sumIntPlugin.compute(objs);
        sumLongPlugin.compute(objs);
        sumLongPlugin.compute(objs);
        Integer intSize = sumIntPlugin.getResult();
        Long longSize = sumLongPlugin.getResult();
        Assert.assertEquals(new Integer(3 * 2), intSize);
        Assert.assertEquals(new Long(3 * 2), longSize);
        AttributeBuilderVisitor visitor = new AttributeBuilderVisitor();
        AbstractAttribute<?> sumIntAttribute = sumIntPlugin.accept(visitor);
        Assert.assertTrue(sumIntAttribute instanceof IntegerAttribute);
        Assert.assertEquals(intAttributeToCompute.getName(), sumIntAttribute.getName());
        Assert.assertEquals(intSize, sumIntAttribute.getValue());
        AbstractAttribute<?> sumLongAttribute = sumLongPlugin.accept(visitor);
        Assert.assertTrue(sumLongAttribute instanceof LongAttribute);
        Assert.assertEquals(longAttributeToCompute.getName(), sumLongAttribute.getName());
        Assert.assertEquals(longSize, sumLongAttribute.getValue());
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