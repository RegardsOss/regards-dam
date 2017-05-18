/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

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

        final AttributeModel attModel = AttributeModelBuilder.build(attName, AttributeType.STRING, "ForTests")
                .description(description).get();
        saveAttribute(attModel);

        // Try to retrieve attribute
        final AttributeModel att = findSingle();
        Assert.assertEquals(attName, att.getName());
        Assert.assertEquals(AttributeType.STRING, att.getType());
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

        final AttributeModel attModel1 = AttributeModelBuilder.build("GEOMETRY", AttributeType.INTEGER,
                                                                     "ForTests").fragment(f)
                .withoutRestriction();
        saveAttribute(attModel1);

        final AttributeModel attModel2 = AttributeModelBuilder.build("CRS", AttributeType.STRING, "ForTests").fragment(f)
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
        final AttributeModel attModel = AttributeModelBuilder.build("TO_DELETE", AttributeType.STRING, "ForTests")
                .withoutRestriction();
        final AttributeModel saved = saveAttribute(attModel);
        attModelRepository.delete(saved);
    }
}
