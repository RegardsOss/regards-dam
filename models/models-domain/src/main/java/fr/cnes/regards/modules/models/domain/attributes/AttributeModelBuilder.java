/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes;

import org.springframework.util.Assert;

import fr.cnes.regards.modules.models.domain.attributes.restriction.AbstractRestriction;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionFactory;

/**
 *
 * Attribute model builder
 *
 * @author Marc Sordi
 *
 */
public final class AttributeModelBuilder {

    /**
     * Current attribute model
     */
    private final AttributeModel attributeModel;

    private AttributeModelBuilder(AttributeModel pAttributeModel) {
        this.attributeModel = pAttributeModel;
    }

    public static AttributeModelBuilder build(String pName, AttributeType pType) {
        final AttributeModel am = new AttributeModel();
        am.setName(pName);
        am.setType(pType);
        am.setAlterable(Boolean.FALSE);
        am.setQueryable(Boolean.FALSE);
        am.setFacetable(Boolean.FALSE);
        am.setOptional(Boolean.FALSE);
        return new AttributeModelBuilder(am);
    }

    public AttributeModel get() {
        return attributeModel;
    }

    public AttributeModelBuilder withId(Long pId) {
        attributeModel.setId(pId);
        return this;
    }

    public AttributeModelBuilder description(String pDescription) {
        attributeModel.setDescription(pDescription);
        return this;
    }

    public AttributeModelBuilder isAlterable() {
        attributeModel.setAlterable(Boolean.TRUE);
        return this;
    }

    public AttributeModelBuilder isQueryable() {
        attributeModel.setQueryable(Boolean.TRUE);
        return this;
    }

    public AttributeModelBuilder isQueryableAndFacetable() {
        attributeModel.setQueryable(Boolean.TRUE);
        attributeModel.setFacetable(Boolean.TRUE);
        return this;
    }

    public AttributeModelBuilder isOptional() {
        attributeModel.setOptional(Boolean.TRUE);
        return this;
    }

    public AttributeModelBuilder fragment(Fragment pFragment) {
        attributeModel.setFragment(pFragment);
        return this;
    }

    // Restriction

    public <T extends AbstractRestriction> AttributeModel withRestriction(T pRestriction) {
        Assert.notNull(pRestriction);
        if (!pRestriction.supports(attributeModel.getType())) {
            throw new IllegalArgumentException("Unsupported restriction " + pRestriction.getType()
                    + " for attribute type " + attributeModel.getType());
        }
        attributeModel.setRestriction(pRestriction);
        return attributeModel;
    }

    public AttributeModel withDateISO8601Restriction() {
        return withRestriction(RestrictionFactory.buildDateISO8601Restriction());
    }

    public AttributeModel withEnumerationRestriction(String... pAcceptableValues) {
        return withRestriction(RestrictionFactory.buildEnumerationRestriction(pAcceptableValues));
    }

    public AttributeModel withFloatRangeRestriction(Float pMinInclusive, Float pMaxInclusive, Float pMinExclusive,
            Float pMaxExclusive) {
        return withRestriction(RestrictionFactory.buildFloatRangeRestriction(pMinInclusive, pMaxInclusive,
                                                                             pMinExclusive, pMaxExclusive));
    }

    public AttributeModel withGeometryRestriction() {
        return withRestriction(RestrictionFactory.buildGeometryRestriction());
    }

    public AttributeModel withIntegerRangeRestriction(Integer pMinInclusive, Integer pMaxInclusive,
            Integer pMinExclusive, Integer pMaxExclusive) {
        return withRestriction(RestrictionFactory.buildIntegerRangeRestriction(pMinInclusive, pMaxInclusive,
                                                                               pMinExclusive, pMaxExclusive));
    }

    public AttributeModel withoutRestriction() {
        return withRestriction(RestrictionFactory.buildNoRestriction());
    }

    public AttributeModel withPatternRestriction(String pPattern) {
        return withRestriction(RestrictionFactory.buildPatternRestriction(pPattern));
    }

    public AttributeModel withUrlRestriction() {
        return withRestriction(RestrictionFactory.buildUrlRestriction());
    }
}
