/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.modules.models.domain.ComputationMode;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.ICalculationModel;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
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
                AttributeModelBuilder.build("forTest", AttributeType.INTEGER).get(),
                Model.build("testModel", "pDescription", EntityType.DATASET));
        // get a PluginConfiguration
        PluginMetaData metaData = new PluginMetaData();
        metaData.setPluginId("tata");
        metaData.setAuthor("toto");
        metaData.setDescription("titi");
        metaData.setVersion("tutu");
        metaData.setInterfaceName(ICalculationModel.class.getName());
        metaData.setPluginClassName("toto");
        PluginConfiguration conf = new PluginConfiguration(metaData, "testConf");
        invalidAssoc.setMode(ComputationMode.CUSTOM);
        invalidAssoc.setComputationConf(conf);
        final Set<ConstraintViolation<ModelAttrAssoc>> modelAttrAssocConstraintViolation = validator
                .validate(invalidAssoc);
    }
}
