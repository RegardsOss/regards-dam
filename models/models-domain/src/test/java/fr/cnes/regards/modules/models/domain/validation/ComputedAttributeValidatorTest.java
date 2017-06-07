/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.validation;

import javax.validation.*;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.modules.models.domain.*;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author Sylvain Vissiere-Guerinet
 */
public class ComputedAttributeValidatorTest {

    /**
     * Validator
     */
    private static Validator validator;

    @BeforeClass
    public static void init() {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test(expected = ValidationException.class)
    public void testUnknownPlugin() {
        // initialize the association
        ModelAttrAssoc invalidAssoc = new ModelAttrAssoc(
                AttributeModelBuilder.build("forTest", AttributeType.INTEGER, "ForTests").get(),
                Model.build("testModel", "pDescription", EntityType.DATASET));
        // get a PluginConfiguration
        PluginMetaData metaData = new PluginMetaData();
        metaData.setPluginId("tata");
        metaData.setAuthor("toto");
        metaData.setDescription("titi");
        metaData.setVersion("tutu");
        metaData.getInterfaceNames().add(IComputedAttribute.class.getName());
        metaData.setPluginClassName("toto");
        PluginConfiguration conf = new PluginConfiguration(metaData, "testConf");
        invalidAssoc.setComputationConf(conf);
        final Set<ConstraintViolation<ModelAttrAssoc>> modelAttrAssocConstraintViolation = validator
                .validate(invalidAssoc);
    }
}
