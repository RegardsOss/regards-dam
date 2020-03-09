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
package fr.cnes.regards.modules.dam.domain.models.attributes.restriction;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.attributes.restriction.validator.CheckFloatRange;
import fr.cnes.regards.modules.dam.domain.models.schema.DoubleRange;
import fr.cnes.regards.modules.dam.domain.models.schema.DoubleRange.Max;
import fr.cnes.regards.modules.dam.domain.models.schema.DoubleRange.Min;
import fr.cnes.regards.modules.dam.domain.models.schema.Restriction;

/**
 *
 * Manage date format restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#DOUBLE}</li>
 * <li>{@link AttributeType#DOUBLE_ARRAY}</li>
 * <li>{@link AttributeType#DOUBLE_INTERVAL}</li>
 * </ul>
 *
 * @author Marc Sordi
 *
 */
@CheckFloatRange
@Entity
@DiscriminatorValue("DOUBLE_RANGE")
public class DoubleRangeRestriction extends AbstractRestriction {

    /**
     * Minimum possible value (included)
     */
    @Column(name = "minf")
    @NotNull
    private Double min;

    /**
     * Maximun possible value (included)
     */
    @Column(name = "maxf")
    @NotNull
    private Double max;

    /**
     * Minimum possible value (excluded)
     */
    @Column(name = "minf_excluded")
    private boolean minExcluded = false;

    /**
     * Maximum possible value (excluded)
     */
    @Column(name = "maxf_excluded")
    private boolean maxExcluded = false;

    public DoubleRangeRestriction() {
        super();
        type = RestrictionType.DOUBLE_RANGE;
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.DOUBLE.equals(pAttributeType) || AttributeType.DOUBLE_ARRAY.equals(pAttributeType)
                || AttributeType.DOUBLE_INTERVAL.equals(pAttributeType);
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double pMin) {
        min = pMin;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double pMax) {
        max = pMax;
    }

    public boolean isMinExcluded() {
        return minExcluded;
    }

    public void setMinExcluded(boolean pMinExcluded) {
        minExcluded = pMinExcluded;
    }

    public boolean isMaxExcluded() {
        return maxExcluded;
    }

    public void setMaxExcluded(boolean pMaxExcluded) {
        maxExcluded = pMaxExcluded;
    }

    @Override
    public Restriction toXml() {

        final Restriction restriction = new Restriction();
        final DoubleRange drr = new DoubleRange();

        Max xmlMax = new Max();
        xmlMax.setValue(max);
        xmlMax.setExcluded(maxExcluded);
        drr.setMax(xmlMax);

        Min xmlMin = new Min();
        xmlMin.setValue(min);
        xmlMin.setExcluded(minExcluded);
        drr.setMin(xmlMin);

        restriction.setDoubleRange(drr);
        return restriction;
    }

    @Override
    public void fromXml(Restriction pXmlElement) {
        final DoubleRange dr = pXmlElement.getDoubleRange();
        Max xmlMax = dr.getMax();
        setMax(xmlMax.getValue());
        setMaxExcluded(xmlMax.isExcluded());
        Min xmlMin = dr.getMin();
        setMin(xmlMin.getValue());
        setMinExcluded(xmlMin.isExcluded());
    }

}
