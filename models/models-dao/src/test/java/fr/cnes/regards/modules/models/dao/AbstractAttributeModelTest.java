/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Common attribute model test methods
 *
 * @author Marc Sordi
 *
 */
public abstract class AbstractAttributeModelTest extends AbstractDaoTransactionalTest {

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

    public AttributeModel saveAttribute(final AttributeModel pAttributeModel) {
        return TestModelUtils.saveAttribute(pAttributeModel, restrictionRepository, fragmentRepository,
                                            attModelRepository);
    }

    public AttributeModel findSingle() {
        return TestModelUtils.findSingle(attModelRepository);
    }

    public IAttributeModelRepository getAttModelRepository() {
        return attModelRepository;
    }

    public IRestrictionRepository getRestrictionRepository() {
        return restrictionRepository;
    }

    public IFragmentRepository getFragmentRepository() {
        return fragmentRepository;
    }

}
