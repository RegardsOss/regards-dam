/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.google.common.collect.Iterables;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;

/**
 * Common attribute model test methods
 *
 * @author Marc Sordi
 */
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class AbstractModelTest extends AbstractDaoTransactionalTest {

    /**
     * Attribute model repository
     */
    @Autowired
    protected IAttributeModelRepository attModelRepository;

    /**
     * Restriction repository
     */
    @Autowired
    protected IRestrictionRepository restrictionRepository;

    /**
     * Fragment repository
     */
    @Autowired
    protected IFragmentRepository fragmentRepository;

    /**
     * Model repository
     */
    @Autowired
    protected IModelRepository modelRepository;

    /**
     * Model attribute repository
     */
    @Autowired
    protected IModelAttrAssocRepository modelAttributeRepository;

    @Before
    public void cleanBefore() {
        modelAttributeRepository.deleteAll();
        attModelRepository.deleteAll();
        modelRepository.deleteAll();
        fragmentRepository.deleteAll();
    }

    /**
     * Save an attribute model
     *
     * @param pAttributeModel entity to save
     * @return the saved attribute model
     */
    protected AttributeModel saveAttribute(final AttributeModel pAttributeModel) {
        Assert.assertNotNull(pAttributeModel);
        // Save restriction if any
        if (pAttributeModel.getRestriction() != null) {
            final AbstractRestriction restriction = pAttributeModel.getRestriction();
            restrictionRepository.save(restriction);
        }
        // Save fragment if any
        if (pAttributeModel.getFragment() != null) {
            final Fragment fragment = pAttributeModel.getFragment();
            fragmentRepository.save(fragment);
        } else {
            Fragment defaultF = fragmentRepository.findByName(Fragment.getDefaultName());
            if (defaultF == null) {
                defaultF = fragmentRepository.save(Fragment.buildDefault());
            }
            pAttributeModel.setFragment(defaultF);
        }
        // Save attribute model
        return attModelRepository.save(pAttributeModel);
    }

    protected AttributeModel findSingle() {
        final Iterable<AttributeModel> atts = attModelRepository.findAll();
        if (Iterables.size(atts) != 1) {
            Assert.fail("Only single result is expected!");
        }
        return Iterables.get(atts, 0);
    }

    /**
     * Create a model
     *
     * @param pName model name
     * @param pDescription description
     * @param pModelType model type
     * @return a model
     */
    protected Model createModel(String pName, String pDescription, EntityType pModelType) {

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
}
