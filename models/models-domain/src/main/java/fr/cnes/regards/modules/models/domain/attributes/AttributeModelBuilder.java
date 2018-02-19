/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

    /**
     * Initialize the builder by instanciating a minimal attribute model
     * @param pName attribute name
     * @param pType attribute type
     * @param label attribute label
     * @return initialized builder
     */
    public static AttributeModelBuilder build(String pName, AttributeType pType, String label) {
        final AttributeModel am = new AttributeModel();
        am.setName(pName);
        am.setType(pType);
        am.setAlterable(Boolean.FALSE);
        am.setOptional(Boolean.FALSE);
        am.setLabel(label);
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

    public AttributeModelBuilder isOptional() {
        attributeModel.setOptional(Boolean.TRUE);
        return this;
    }

    public AttributeModelBuilder fragment(Fragment pFragment) {
        attributeModel.setFragment(pFragment);
        return this;
    }

    public AttributeModelBuilder defaultFragment() {
        attributeModel.setFragment(Fragment.buildDefault());
        return this;
    }

    public AttributeModelBuilder isStatic() {
        attributeModel.setDynamic(false);
        return this;
    }

    // Restriction

    public <T extends AbstractRestriction> AttributeModel withRestriction(T pRestriction) {
        Assert.notNull(pRestriction, "Restriction is required");
        if (!pRestriction.supports(attributeModel.getType())) {
            throw new IllegalArgumentException("Unsupported restriction " + pRestriction.getType()
                    + " for attribute type " + attributeModel.getType());
        }
        attributeModel.setRestriction(pRestriction);
        return attributeModel;
    }

    public AttributeModel withEnumerationRestriction(String... pAcceptableValues) {
        return withRestriction(RestrictionFactory.buildEnumerationRestriction(pAcceptableValues));
    }

    public AttributeModel withFloatRangeRestriction(Double pMin, Double pMax, boolean pMinExcluded,
            boolean pMaxExcluded) {
        return withRestriction(RestrictionFactory.buildFloatRangeRestriction(pMin, pMax, pMinExcluded, pMaxExcluded));
    }

    public AttributeModel withIntegerRangeRestriction(Integer pMin, Integer pMax, boolean pMinExcluded,
            boolean pMaxExcluded) {
        return withRestriction(RestrictionFactory.buildIntegerRangeRestriction(pMin, pMax, pMinExcluded, pMaxExcluded));
    }

    public AttributeModel withLongRangeRestriction(Long pMin, Long pMax, boolean pMinExcluded, boolean pMaxExcluded) {
        return withRestriction(RestrictionFactory.buildLongRangeRestriction(pMin, pMax, pMinExcluded, pMaxExcluded));
    }

    public AttributeModel withoutRestriction() {
        attributeModel.setRestriction(null);
        return attributeModel;
    }

    public AttributeModel withPatternRestriction(String pPattern) {
        return withRestriction(RestrictionFactory.buildPatternRestriction(pPattern));
    }
}
