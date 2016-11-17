/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelType;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Common attribute model test methods
 *
 * @author Marc Sordi
 *
 */
public abstract class AbstractModelTest extends AbstractDaoTransactionalTest {

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

    /**
     * Model repository
     */
    @Autowired
    private IModelRepository modelRepository;

    protected AttributeModel saveAttribute(final AttributeModel pAttributeModel) {
        return TestModelUtils.saveAttribute(pAttributeModel, restrictionRepository, fragmentRepository,
                                            attModelRepository);
    }

    protected AttributeModel findSingle() {
        return TestModelUtils.findSingle(attModelRepository);
    }

    /**
     * Create a model
     *
     * @param pName
     *            model name
     * @param pDescription
     *            description
     * @param pModelType
     *            model type
     * @return a model
     */
    protected Model createModel(String pName, String pDescription, ModelType pModelType) {

        final Model model = new Model();
        model.setName(pName);
        model.setType(pModelType);
        model.setDescription(pDescription);

        modelRepository.save(model);
        Assert.assertTrue(model.isIdentifiable());

        final Model retrieved = modelRepository.findOne(model.getId());
        Assert.assertEquals(pName, retrieved.getName());
        Assert.assertEquals(pDescription, retrieved.getDescription());
        Assert.assertEquals(pModelType, retrieved.getType());
        return retrieved;
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
