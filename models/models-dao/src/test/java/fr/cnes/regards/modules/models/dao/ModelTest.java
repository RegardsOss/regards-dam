/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
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
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ModelTest.class);

    /**
     * Create a model, attach an attribute model and try to retrieve
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Purpose("Create a model of type COLLECTION - DOCUMENT, DATA or DATASET are also available -")
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
        Assert.assertEquals(1, Iterables.size(directAtts));
    }

    // TODO try to delete a non empty model
}
