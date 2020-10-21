/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.model.dao;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

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
        final AttributeModel attModel = AttributeModelBuilder
                .build("withRestrictionAttribute", PropertyType.STRING, "ForTests")
                .withPatternRestriction("MOCKPATTERN");
        final AttributeModel saved = saveAttribute(attModel);
        attModelRepository.delete(saved);

        final Iterable<AbstractRestriction> it = restrictionRepository.findAll();
        Assert.assertEquals(0, Iterables.size(it));
    }

    /**
     * Test attribute removal
     */
    @Test
    public void deleteAttributeWithNoRestriction() {

        final AttributeModel attModel = AttributeModelBuilder.build("TO_DELETE", PropertyType.STRING, "ForTests")
                .withoutRestriction();
        final AttributeModel saved = saveAttribute(attModel);

        final AttributeModel attModel2 = AttributeModelBuilder.build("TO_DELETE_TWO", PropertyType.STRING, "ForTests")
                .withoutRestriction();
        saveAttribute(attModel2);

        attModelRepository.delete(saved);

        // Only one attribute model is deleted
        // The default fragment is not removed
        final Fragment defaultFragment = fragmentRepository.findByName(Fragment.getDefaultName());
        Assert.assertNotNull(defaultFragment);
    }
}
