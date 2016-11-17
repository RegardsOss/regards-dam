/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;

/**
 *
 * Some cleaning tests
 *
 * @author Marc Sordi
 *
 */
@MultitenantTransactional
public class CleanDatabaseTest extends AbstractModelTest {

    /**
     * Logger
     */
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(CleanDatabaseTest.class);

    @Test
    public void deleteAttributeWithRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("withRestrictionAttribute", AttributeType.STRING)
                .withPatternRestriction("MOCKPATTERN");
        final AttributeModel saved = saveAttribute(attModel);
        getAttModelRepository().delete(saved);

        final Iterable<AbstractRestriction> it = getRestrictionRepository().findAll();
        Assert.assertEquals(0, Iterables.size(it));
    }

    /**
     * Test attribute removal
     */
    @Test
    public void deleteAttributeWithNoRestriction() {

        final AttributeModel attModel = AttributeModelBuilder.build("TO_DELETE", AttributeType.STRING)
                .withoutRestriction();
        final AttributeModel saved = saveAttribute(attModel);

        final AttributeModel attModel2 = AttributeModelBuilder.build("TO_DELETE_TWO", AttributeType.STRING)
                .withoutRestriction();
        saveAttribute(attModel2);

        getAttModelRepository().delete(saved);

        // Only one attribute model is deleted
        // The default fragment is not removed
        final Fragment defaultFragment = getFragmentRepository().findByName(Fragment.getDefaultName());
        Assert.assertNotNull(defaultFragment);
    }
}
