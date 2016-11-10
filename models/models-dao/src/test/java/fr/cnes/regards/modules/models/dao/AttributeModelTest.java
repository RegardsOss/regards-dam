/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.dao;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 *
 * Repository tests
 *
 * @author msordi
 *
 */
@MultitenantTransactional
public class AttributeModelTest extends AbstractAttributeModelTest {

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

        final Iterable<AttributeModel> atts = getAttModelRepository().findAll();
        Assert.assertEquals(2, Iterables.size(atts));
        for (final AttributeModel att : atts) {
            Assert.assertNotNull(att.getFragment());
            Assert.assertEquals(name, att.getFragment().getName());
            Assert.assertEquals(description, att.getFragment().getDescription());
        }
    }

    @Test
    public void deleteAttributeTest() {
        final AttributeModel attModel = AttributeModelBuilder.build("TO_DELETE", AttributeType.STRING)
                .withoutRestriction();
        final AttributeModel saved = saveAttribute(attModel);
        getAttModelRepository().delete(saved);
    }
}
