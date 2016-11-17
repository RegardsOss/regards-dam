/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.ModelType;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author Marc Sordi
 *
 */
public class ModelTest extends AbstractModelTest {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ModelTest.class);

    @Test
    public void createModel() {
        final Model model = createModel("MISSION", "Scientist mission collection", ModelType.COLLECTION);

        // Create attribute
        AttributeModel attModel = AttributeModelBuilder.build("NAME", AttributeType.STRING).get();
        attModel = saveAttribute(attModel);

        // Create model attribute
        final ModelAttribute modelAtt = new ModelAttribute(attModel, model);
        modelAttributeRepository.save(modelAtt);

        // Retrieve all model attributes
        final Iterable<ModelAttribute> directAtts = modelAttributeRepository.findByModelId(model.getId());
        LOG.debug(model.getName());

    }
}
