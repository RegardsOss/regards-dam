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
package fr.cnes.regards.modules.dam.dao.models;

import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.DoubleRangeRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.PatternRestriction;

/**
 * Restriction test
 *
 * @author Marc Sordi
 *
 */
public class RestrictionTest extends AbstractModelTest {

    @Test
    public void enumRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("ENUM", AttributeType.STRING, "ForTests")
                .withEnumerationRestriction("FIRST", "SECOND");
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.STRING, att.getType());
        final EnumerationRestriction er = checkRestrictionType(att.getRestriction(), EnumerationRestriction.class);
        Assert.assertEquals(2, er.getAcceptableValues().size());
    }

    /**
     * Test float restriction
     */
    @Test
    public void floatRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("VFLOAT", AttributeType.DOUBLE_INTERVAL, "ForTests")
                .withFloatRangeRestriction(Double.MIN_VALUE, Double.MAX_VALUE, false, false);
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.DOUBLE_INTERVAL, att.getType());
        final DoubleRangeRestriction frr = checkRestrictionType(att.getRestriction(), DoubleRangeRestriction.class);
        Assert.assertTrue(Double.MIN_VALUE == frr.getMin());
        Assert.assertTrue(Double.MAX_VALUE == frr.getMax());
        Assert.assertFalse(frr.isMinExcluded());
        Assert.assertFalse(frr.isMaxExcluded());
    }

    /**
     * Test integer restriction
     */
    @Test
    public void integerRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("VINTEGER", AttributeType.INTEGER_ARRAY, "ForTests")
                .withIntegerRangeRestriction(Integer.MIN_VALUE, Integer.MAX_VALUE, false, true);
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.INTEGER_ARRAY, att.getType());
        final IntegerRangeRestriction irr = checkRestrictionType(att.getRestriction(), IntegerRangeRestriction.class);
        Assert.assertTrue(Integer.MIN_VALUE == irr.getMin());
        Assert.assertTrue(Integer.MAX_VALUE == irr.getMax());
        Assert.assertFalse(irr.isMinExcluded());
        Assert.assertTrue(irr.isMaxExcluded());
    }

    @Test
    public void patternRestriction() {
        final String pattern = "pattern";
        final AttributeModel attModel = AttributeModelBuilder.build("STRING", AttributeType.STRING_ARRAY, "ForTests")
                .withPatternRestriction(pattern);
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.STRING_ARRAY, att.getType());
        final PatternRestriction pr = checkRestrictionType(att.getRestriction(), PatternRestriction.class);
        Assert.assertEquals(pattern, pr.getPattern());
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractRestriction> T checkRestrictionType(AbstractRestriction pRestriction, Class<T> pClass) {
        if (pRestriction != null) {
            Assert.assertTrue(pClass.isInstance(pRestriction));
            return (T) pRestriction;
        } else {
            throw new AssertionError("Missing restriction!");
        }
    }
}
