/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.FloatRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;

/**
 * Restriction test
 *
 * @author Marc Sordi
 *
 */
public class RestrictionTest extends AbstractModelTest {

    @Test
    public void enumRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("ENUM", AttributeType.STRING)
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
        final AttributeModel attModel = AttributeModelBuilder.build("FLOAT", AttributeType.FLOAT_INTERVAL)
                .withFloatRangeRestriction(Double.MIN_VALUE, Double.MAX_VALUE, false, false);
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.FLOAT_INTERVAL, att.getType());
        final FloatRangeRestriction frr = checkRestrictionType(att.getRestriction(), FloatRangeRestriction.class);
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
        final AttributeModel attModel = AttributeModelBuilder.build("INTEGER", AttributeType.INTEGER_ARRAY)
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
        final AttributeModel attModel = AttributeModelBuilder.build("STRING", AttributeType.STRING_ARRAY)
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
