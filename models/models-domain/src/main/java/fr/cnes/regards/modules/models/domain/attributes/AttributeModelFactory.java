/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import org.springframework.util.Assert;

import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;

/**
 *
 * {@link AttributeModel} factory
 *
 * @author Marc Sordi
 *
 */
public final class AttributeModelFactory {

    private AttributeModelFactory() {
    }

    public static AttributeModel build(Long pId, String pName, AttributeType pType) {
        Assert.notNull(pId);
        final AttributeModel attributeModel = build(pName, pType);
        attributeModel.setId(pId);
        return attributeModel;
    }

    public static AttributeModel build(String pName, AttributeType pType) {
        Assert.notNull(pName);
        Assert.notNull(pType);
        final AttributeModel attributeModel = new AttributeModel();
        attributeModel.setName(pName);
        attributeModel.setType(pType);
        return attributeModel;
    }

    /**
     * @param <T>
     *            restriction
     * @param pName
     *            attribute name
     * @param pType
     *            attribute type
     * @param pDescription
     *            attribute description
     * @param pAlterable
     *            whether this attribute is alterable
     * @param pQueryable
     *            whether this attribute is queryable
     * @param pFacetable
     *            whether this attribute is facetable
     * @param pOptional
     *            whether this attribute is optional
     * @param pRestriction
     *            whether this attribute is restriction
     * @param pFragment
     *            associated fragment
     * @return an attribute model
     */
    public static <T extends AbstractRestriction> AttributeModel build(String pName, AttributeType pType,
            String pDescription, boolean pAlterable, boolean pQueryable, boolean pFacetable, boolean pOptional,
            T pRestriction, Fragment pFragment) {
        final AttributeModel model = AttributeModelFactory.build(pName, AttributeType.STRING);

        if ((pRestriction != null) && !pRestriction.supports(pType)) {
            throw new IllegalArgumentException(
                    "Unsupported restriction " + pRestriction.getType() + " for attribute type " + pType);
        }

        model.setDescription(pDescription);

        model.setAlterable(fallbackToDefault(Boolean.TRUE, Boolean.valueOf(pAlterable)));
        model.setQueryable(fallbackToDefault(Boolean.FALSE, Boolean.valueOf(pQueryable)));
        model.setFacetable(fallbackToDefault(Boolean.FALSE, Boolean.valueOf(pFacetable)));
        model.setOptional(fallbackToDefault(Boolean.FALSE, Boolean.valueOf(pOptional)));
        model.setRestriction(pRestriction);
        model.setFragment(pFragment);

        return model;
    }

    private static <T> T fallbackToDefault(T pDefaultValue, T pValue) {
        Assert.notNull(pDefaultValue);
        // CHECKSTYLE:OFF
        return pValue != null ? pValue : pDefaultValue;
        // CHECKSTYLE:ON
    }
}
