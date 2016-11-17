/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelType;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Test model persistance
 *
 * @author Marc Sordi
 *
 */
public class ModelTest extends AbstractModelTest {

    @Autowired
    private IModelRepository modelRepository;

    @Test
    public void createModel() {
        Model model = createModel("MISSION", "Scientist mission collection", ModelType.COLLECTION);

        // Create attribute
        AttributeModel attModel = AttributeModelBuilder.build("NAME", AttributeType.STRING).get();
        attModel = saveAttribute(attModel);

        // Create model attribute
        // ModelAttribute modelAtt = new ModelAttribute();

        // Retrieve all model attributes
    }
}
