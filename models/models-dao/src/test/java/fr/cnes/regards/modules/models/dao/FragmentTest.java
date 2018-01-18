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

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
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
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Try to delete a non empty fragment")
    @Test(expected = PersistenceException.class)
    public void deleteFragment() {

        final Fragment fragment = Fragment.buildFragment("fragment1", "description fragment 1");

        final AttributeModel attModel = AttributeModelBuilder.build("fragment_att1", AttributeType.STRING, "ForTests")
                .fragment(fragment).withoutRestriction();
        saveAttribute(attModel);

        final Iterable<AttributeModel> attModels = attModelRepository.findByFragmentId(fragment.getId());
        Assert.assertNotNull(attModels);
        Assert.assertEquals(1, Iterables.size(attModels));

        // Try to remove anyway
        fragmentRepository.delete(fragment);
        entityManager.flush();
        entityManager.clear();
    }
}
