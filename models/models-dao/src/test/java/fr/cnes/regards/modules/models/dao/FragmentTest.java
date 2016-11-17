/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * Fragment test
 *
 * @author Marc Sordi
 *
 */
public class FragmentTest extends AbstractModelTest {

    /**
     * Try to delete a non empty fragment
     */
    @Test(expected = PersistenceException.class)
    public void deleteFragment() {

        final Fragment fragment = Fragment.buildFragment("fragment1", "description fragment 1");

        final AttributeModel attModel = AttributeModelBuilder.build("fragment_att1", AttributeType.STRING)
                .fragment(fragment).withoutRestriction();
        saveAttribute(attModel);

        final Iterable<AttributeModel> attModels = attModelRepository.findByFragmentId(fragment.getId());
        Assert.assertNotNull(attModels);
        Assert.assertEquals(1, Iterables.size(attModels));

        // Try to remove anyway
        fragmentRepository.delete(fragment);
        entityManager.flush();
    }
}
