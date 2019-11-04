/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Repository tests
 *
 * @author msordi
 */
public class AttributeModelTest extends AbstractModelTest {

    /**
     * Save and retrieve a single and simple attribute
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Requirement("REGARDS_DSL_DAM_MOD_100")
    @Requirement("REGARDS_DSL_DAM_MOD_110")
    @Requirement("REGARDS_DSL_DAM_MOD_120")
    @Purpose("Create a single attribute of type STRING not queryable and not facetable")
    public void singleAttribute() {

        // Add string attribute
        final String attName = "FAKE";
        final String description = "DESCRIPTION OF THE FAKE ATTRIBUTE";

        final AttributeModel attModel = AttributeModelBuilder.build(attName, PropertyType.STRING, "ForTests")
                .description(description).get();
        saveAttribute(attModel);

        // Try to retrieve attribute
        final AttributeModel att = findSingle();
        Assert.assertEquals(attName, att.getName());
        Assert.assertEquals(PropertyType.STRING, att.getType());
        Assert.assertEquals(description, att.getDescription());
        Assert.assertEquals(Boolean.FALSE, att.isAlterable());
        Assert.assertEquals(Boolean.FALSE, att.isOptional());
    }

    /**
     * Test fragment
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Test fragment management")
    public void fragmentTest() {
        final String name = "GEO_FRAGMENT";
        final String description = "All consistent geo attributes";
        final Fragment f = Fragment.buildFragment(name, description);

        final AttributeModel attModel1 = AttributeModelBuilder.build("GEOMETRY", PropertyType.INTEGER, "ForTests")
                .fragment(f).withoutRestriction();
        saveAttribute(attModel1);

        final AttributeModel attModel2 = AttributeModelBuilder.build("CRS", PropertyType.STRING, "ForTests").fragment(f)
                .withEnumerationRestriction("EARTH", "ASTRO", "MARS");
        saveAttribute(attModel2);

        final Iterable<AttributeModel> atts = attModelRepository.findAll();
        Assert.assertEquals(2, Iterables.size(atts));
        for (final AttributeModel att : atts) {
            Assert.assertNotNull(att.getFragment());
            Assert.assertEquals(name, att.getFragment().getName());
            Assert.assertEquals(description, att.getFragment().getDescription());
        }
    }

    @Test
    public void deleteAttributeTest() {
        final AttributeModel attModel = AttributeModelBuilder.build("TO_DELETE", PropertyType.STRING, "ForTests")
                .withoutRestriction();
        final AttributeModel saved = saveAttribute(attModel);
        attModelRepository.delete(saved);
    }
}
