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
package fr.cnes.regards.modules.model.domain.attributes.restriction;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.model.domain.attributes.restriction.validator.CheckIntegerRange;
import fr.cnes.regards.modules.model.domain.schema.IntegerRange;
import fr.cnes.regards.modules.model.domain.schema.IntegerRange.Max;
import fr.cnes.regards.modules.model.domain.schema.IntegerRange.Min;
import fr.cnes.regards.modules.model.domain.schema.Restriction;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 *
 * Manage date format restriction for attribute of type :
 * <ul>
 * <li>{@link PropertyType#INTEGER}</li>
 * <li>{@link PropertyType#INTEGER_ARRAY}</li>
 * <li>{@link PropertyType#INTEGER_INTERVAL}</li>
 * </ul>
 *
 * @author msordi
 *
 */
@CheckIntegerRange
@Entity
@DiscriminatorValue("INTEGER_RANGE")
public class IntegerRangeRestriction extends AbstractRestriction {

    /**
     * Minimum possible value (included)
     */
    @Column(name = "mini")
    @NotNull
    private Long min;

    /**
     * Maximun possible value (included)
     */
    @Column(name = "maxi")
    @NotNull
    private Long max;

    /**
     * Minimum possible value (excluded)
     */
    @Column(name = "mini_excluded")
    private boolean minExcluded = false;

    /**
     * Maximum possible value (excluded)
     */
    @Column(name = "maxi_excluded")
    private boolean maxExcluded = false;

    public IntegerRangeRestriction() {// NOSONAR
        super();
        type = RestrictionType.INTEGER_RANGE;
    }

    @Override
    public Boolean supports(PropertyType pPropertyType) {
        return PropertyType.INTEGER.equals(pPropertyType) || PropertyType.INTEGER_ARRAY.equals(pPropertyType)
                || PropertyType.INTEGER_INTERVAL.equals(pPropertyType);
    }

    public Integer getMin() {
        return min.intValue();
    }

    public void setMin(Integer pMin) {
        if (pMin == null) {
            min = null;
        } else {
            min = pMin.longValue();
        }
    }

    public Integer getMax() {
        return max.intValue();
    }

    public void setMax(Integer pMax) {
        if (pMax == null) {
            max = null;
        } else {
            max = pMax.longValue();
        }
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
        final IntegerRange irr = new IntegerRange();

        Max xmlMax = new Max();
        xmlMax.setValue(BigInteger.valueOf(max));
        xmlMax.setExcluded(maxExcluded);
        irr.setMax(xmlMax);

        Min xmlMin = new Min();
        xmlMin.setValue(BigInteger.valueOf(min));
        xmlMin.setExcluded(minExcluded);
        irr.setMin(xmlMin);

        restriction.setIntegerRange(irr);
        return restriction;
    }

    @Override
    public void fromXml(Restriction pXmlElement) {
        final IntegerRange ir = pXmlElement.getIntegerRange();
        Max xmlMax = ir.getMax();
        setMax(xmlMax.getValue().intValueExact());
        setMaxExcluded(xmlMax.isExcluded());
        Min xmlMin = ir.getMin();
        setMin(xmlMin.getValue().intValue());
        setMinExcluded(xmlMin.isExcluded());
    }

}
