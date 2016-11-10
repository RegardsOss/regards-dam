/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTest;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * Non transactional tests
 *
 * @author Marc Sordi
 *
 */
@Ignore
public class NonTransactionalTest extends AbstractDaoTest {

    /**
     * Attribute model repository
     */
    @Autowired
    private IAttributeModelRepository attModelRepository;

    /**
     * Restriction repository
     */
    @Autowired
    private IRestrictionRepository restrictionRepository;

    /**
     * Fragment repository
     */
    @Autowired
    private IFragmentRepository fragmentRepository;

    @Before
    public void beforeTest() {
        injectDefaultToken();
    }

    /**
     * Try to delete a not empty fragment
     */
    @Test
    public void deleteFragment() {

        final Fragment fragment = Fragment.buildFragment("fragment1", "description fragment 1");

        final AttributeModel attModel = AttributeModelBuilder.build("fragment_att1", AttributeType.STRING)
                .fragment(fragment).withoutRestriction();
        TestModelUtils.saveAttribute(attModel, restrictionRepository, fragmentRepository, attModelRepository);

        // Reload fragment
        final Fragment reloaded = fragmentRepository.findOne(fragment.getId());
        Assert.assertTrue(reloaded.getAttributeModels().size() > 0);

        // Try to remove anyway
        try {
            fragmentRepository.delete(fragment);
        } catch (DataAccessException e) {
            Assert.assertTrue(DataIntegrityViolationException.class.isInstance(e));
        }
    }
}
