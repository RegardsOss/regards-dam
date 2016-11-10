/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;

import com.google.common.collect.Iterables;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;

/**
 * Utility methods
 *
 * @author Marc Sordi
 *
 */
public final class TestModelUtils {

    private TestModelUtils() {
    }

    /**
     *
     * Save an attribute model
     *
     * @param pAttributeModel
     *            entity to save
     * @param pRestrictionRepository
     *            restriction repo
     * @param pFragmentRepository
     *            fragment repo
     * @param pAttributeModelRepository
     *            attribute model repo
     * @return the saved attribute model
     */
    public static AttributeModel saveAttribute(final AttributeModel pAttributeModel,
            IRestrictionRepository pRestrictionRepository, IFragmentRepository pFragmentRepository,
            IAttributeModelRepository pAttributeModelRepository) {
        Assert.assertNotNull(pAttributeModel);
        // Save restriction if any
        if (pAttributeModel.getRestriction() != null) {
            final AbstractRestriction restriction = pAttributeModel.getRestriction();
            pRestrictionRepository.save(restriction);
        }
        // Save fragment if any
        if (pAttributeModel.getFragment() != null) {
            final Fragment fragment = pAttributeModel.getFragment();
            pFragmentRepository.save(fragment);
        } else {
            Fragment defaultF = pFragmentRepository.findByName(Fragment.getDefaultName());
            if (defaultF == null) {
                defaultF = pFragmentRepository.save(Fragment.buildDefault());
            }
            pAttributeModel.setFragment(defaultF);
        }
        // Save attribute model
        return pAttributeModelRepository.save(pAttributeModel);
    }

    public static AttributeModel findSingle(IAttributeModelRepository pAttributeModelRepository) {
        final Iterable<AttributeModel> atts = pAttributeModelRepository.findAll();
        if (Iterables.size(atts) != 1) {
            Assert.fail("Only single result is expected!");
        }
        return Iterables.get(atts, 0);
    }
}
