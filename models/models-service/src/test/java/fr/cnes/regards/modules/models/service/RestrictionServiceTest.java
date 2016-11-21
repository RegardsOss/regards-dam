/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionType;

/**
 *
 * Restriction service test
 *
 * @author Marc Sordi
 *
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
    public void getEnumRestriction() {
        testRestriction(AttributeType.ENUMERATION, 1, RestrictionType.ENUMERATION);
    }

    @Test
    public void getFloatRestriction() {
        testRestriction(AttributeType.FLOAT, 1, RestrictionType.FLOAT_RANGE);
        testRestriction(AttributeType.FLOAT_ARRAY, 1, RestrictionType.FLOAT_RANGE);
        testRestriction(AttributeType.FLOAT_INTERVAL, 1, RestrictionType.FLOAT_RANGE);
    }

    @Test
    public void getGeoRestriction() {
        testRestriction(AttributeType.GEOMETRY, 0);
    }

    @Test
    public void getIntegerRestriction() {
        testRestriction(AttributeType.INTEGER, 1, RestrictionType.INTEGER_RANGE);
        testRestriction(AttributeType.INTEGER_ARRAY, 1, RestrictionType.INTEGER_RANGE);
        testRestriction(AttributeType.INTEGER_INTERVAL, 1, RestrictionType.INTEGER_RANGE);
    }

    @Test
    public void getStringRestriction() {
        testRestriction(AttributeType.STRING, 1, RestrictionType.PATTERN);
        testRestriction(AttributeType.STRING_ARRAY, 1, RestrictionType.PATTERN);
    }

    @Test
    public void getUrlRestriction() {
        testRestriction(AttributeType.URL, 0);
    }

}
