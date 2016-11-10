/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.DateISO8601Restriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.FloatRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.GeometryRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.UrlRestriction;

/**
 * Restriction test
 *
 * @author Marc Sordi
 *
 */
@MultitenantTransactional
public class RestrictionTest extends AbstractAttributeModelTest {

    @Test
    public void dateRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("DATE_ARRAY", AttributeType.DATE_ARRAY)
                .withDateISO8601Restriction();
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.DATE_ARRAY, att.getType());
        checkRestrictionType(att.getRestriction(), DateISO8601Restriction.class);
    }

    @Test
    public void enumRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("ENUM", AttributeType.ENUMERATION)
                .withEnumerationRestriction("FIRST", "SECOND");
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.ENUMERATION, att.getType());
        final EnumerationRestriction er = checkRestrictionType(att.getRestriction(), EnumerationRestriction.class);
        Assert.assertEquals(2, er.getAcceptableValues().size());
    }

    @Test
    public void geoRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("GEO", AttributeType.GEOMETRY)
                .withGeometryRestriction();
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.GEOMETRY, att.getType());
        checkRestrictionType(att.getRestriction(), GeometryRestriction.class);
    }

    /**
     * Test float restriction
     */
    @Test
    public void floatRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("FLOAT", AttributeType.FLOAT_INTERVAL)
                .withFloatRangeRestriction(Float.MIN_VALUE, Float.MAX_VALUE, null, null);
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.FLOAT_INTERVAL, att.getType());
        final FloatRangeRestriction frr = checkRestrictionType(att.getRestriction(), FloatRangeRestriction.class);
        Assert.assertTrue(Float.MIN_VALUE == frr.getMinInclusive());
        Assert.assertTrue(Float.MAX_VALUE == frr.getMaxInclusive());
        Assert.assertNull(frr.getMinExclusive());
        Assert.assertNull(frr.getMaxExclusive());
    }

    /**
     * Test integer restriction
     */
    @Test
    public void integerRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("INTEGER", AttributeType.INTEGER_ARRAY)
                .withIntegerRangeRestriction(Integer.MIN_VALUE, Integer.MAX_VALUE, null, null);
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.INTEGER_ARRAY, att.getType());
        final IntegerRangeRestriction irr = checkRestrictionType(att.getRestriction(), IntegerRangeRestriction.class);
        Assert.assertTrue(Integer.MIN_VALUE == irr.getMinInclusive());
        Assert.assertTrue(Integer.MAX_VALUE == irr.getMaxInclusive());
        Assert.assertNull(irr.getMinExclusive());
        Assert.assertNull(irr.getMaxExclusive());
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

    @Test
    public void urlRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("URL", AttributeType.URL).withUrlRestriction();
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.URL, att.getType());
        checkRestrictionType(att.getRestriction(), UrlRestriction.class);
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
