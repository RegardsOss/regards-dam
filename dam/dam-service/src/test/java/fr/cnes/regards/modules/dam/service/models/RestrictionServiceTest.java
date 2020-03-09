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
package fr.cnes.regards.modules.dam.service.models;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.RestrictionType;
import fr.cnes.regards.modules.dam.service.models.RestrictionService;

/**
 * Restriction service test
 *
 * @author Marc Sordi
 */
public class RestrictionServiceTest {

    /**
     * Real restriction service
     */
    private RestrictionService restrictionService;

    @Before
    public void before() {
        restrictionService = new RestrictionService();
        restrictionService.init();
    }

    private void testRestriction(AttributeType pAttributeType, int pMatchedRestrictions,
            RestrictionType... pAcceptableRestrictions) {
        final List<String> restrictions = restrictionService.getRestrictions(pAttributeType);
        Assert.assertEquals(pMatchedRestrictions, restrictions.size());
        if (pAcceptableRestrictions != null) {
            for (RestrictionType acceptable : pAcceptableRestrictions) {
                Assert.assertTrue(restrictions.contains(acceptable.toString()));
            }
        }
    }

    @Test
    public void getBooleanRestriction() {
        testRestriction(AttributeType.BOOLEAN, 0);
    }

    @Test
    public void getDateRestriction() {
        // Private restriction
        testRestriction(AttributeType.DATE_ARRAY, 0);
        testRestriction(AttributeType.DATE_INTERVAL, 0);
        testRestriction(AttributeType.DATE_ISO8601, 0);
    }

    @Test
    public void getFloatRestriction() {
        testRestriction(AttributeType.DOUBLE, 1, RestrictionType.DOUBLE_RANGE);
        testRestriction(AttributeType.DOUBLE_ARRAY, 1, RestrictionType.DOUBLE_RANGE);
        testRestriction(AttributeType.DOUBLE_INTERVAL, 1, RestrictionType.DOUBLE_RANGE);
    }

    @Test
    public void getIntegerRestriction() {
        testRestriction(AttributeType.INTEGER, 1, RestrictionType.INTEGER_RANGE);
        testRestriction(AttributeType.INTEGER_ARRAY, 1, RestrictionType.INTEGER_RANGE);
        testRestriction(AttributeType.INTEGER_INTERVAL, 1, RestrictionType.INTEGER_RANGE);
    }

    @Test
    public void getStringRestriction() {
        testRestriction(AttributeType.STRING, 2, RestrictionType.PATTERN, RestrictionType.ENUMERATION);
        testRestriction(AttributeType.STRING_ARRAY, 2, RestrictionType.PATTERN, RestrictionType.ENUMERATION);
    }

    @Test
    public void getUrlRestriction() {
        testRestriction(AttributeType.URL, 0);
    }

}
