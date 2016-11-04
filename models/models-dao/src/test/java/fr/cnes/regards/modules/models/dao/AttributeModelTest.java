/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.BeforeTransaction;

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.jpa.multitenant.autoconfigure.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.DateISO8601Restriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.EnumerationRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.FloatRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.GeometryRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.IntegerRangeRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.NoRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.PatternRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.UrlRestriction;

/**
 *
 * Repository tests
 *
 * @author msordi
 *
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AttributeModelTestConfiguration.class })
@MultitenantTransactional
public class AttributeModelTest {

    /**
     * Mock tenant
     */
    private static final String TENANT = "PROJECT";

    /**
     * Mock role
     */
    private static final String ROLE = "ROLE_USER";

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
     * JWT service
     */
    @Autowired
    private JWTService jwtService;

    @BeforeTransaction
    public void init() {
        jwtService.injectMockToken(TENANT, ROLE);
    }

    /**
     * Save and retrieve a single and simple attribute
     */
    @Test
    public void singleAttribute() {

        // Add string attribute
        final String attName = "FAKE";
        final String description = "DESCRIPTION OF THE FAKE ATTRIBUTE";

        final AttributeModel attModel = AttributeModelBuilder.build(attName, AttributeType.STRING)
                .description(description).get();
        saveAttribute(attModel);

        // Try to retrieve attribute
        final AttributeModel att = findSingle();
        Assert.assertEquals(attName, att.getName());
        Assert.assertEquals(AttributeType.STRING, att.getType());
        Assert.assertEquals(description, att.getDescription());
        Assert.assertEquals(Boolean.FALSE, att.isAlterable());
        Assert.assertEquals(Boolean.FALSE, att.isQueryable());
        Assert.assertEquals(Boolean.FALSE, att.isFacetable());
        Assert.assertEquals(Boolean.FALSE, att.isOptional());
    }

    @Test
    public void noRestriction() {
        final AttributeModel attModel = AttributeModelBuilder.build("NO_RESTRICTION", AttributeType.STRING)
                .withoutRestriction();
        saveAttribute(attModel);

        final AttributeModel att = findSingle();
        Assert.assertEquals(AttributeType.STRING, att.getType());
        checkRestrictionType(att.getRestriction(), NoRestriction.class);
    }

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

    /**
     * Test fragment
     */
    @Test
    public void fragmentTest() {
        final Fragment f = new Fragment();
        final String name = "GEO_FRAGMENT";
        final String description = "All consistent geo attributes";
        f.setName(name);
        f.setDescription(description);

        final AttributeModel attModel1 = AttributeModelBuilder.build("GEOMETRY", AttributeType.GEOMETRY).fragment(f)
                .withGeometryRestriction();
        saveAttribute(attModel1);

        final AttributeModel attModel2 = AttributeModelBuilder.build("CRS", AttributeType.ENUMERATION).fragment(f)
                .withEnumerationRestriction("EARTH", "ASTRO", "MARS");
        saveAttribute(attModel2);

        final Iterable<AttributeModel> atts = attModelRepository.findAll();
        Assert.assertEquals(2, Iterables.size(atts));
        for (AttributeModel att : atts) {
            Assert.assertNotNull(att.getFragment());
            Assert.assertEquals(name, att.getFragment().getName());
            Assert.assertEquals(description, att.getFragment().getDescription().get());
        }

    }

    /**
     * Save an attribute model
     *
     * @param pAttributeModel
     *            entity to save
     */
    private void saveAttribute(AttributeModel pAttributeModel) {
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
        }
        // Save attribute model
        attModelRepository.save(pAttributeModel);
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

    private AttributeModel findSingle() {
        final Iterable<AttributeModel> atts = attModelRepository.findAll();
        if (Iterables.size(atts) != 1) {
            Assert.fail("Only single result is expected!");
        }
        return Iterables.get(atts, 0);
    }
}
