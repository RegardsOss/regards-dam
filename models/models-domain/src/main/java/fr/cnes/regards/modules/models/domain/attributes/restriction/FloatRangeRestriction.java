/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Manage date format restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#FLOAT}</li>
 * <li>{@link AttributeType#FLOAT_ARRAY}</li>
 * <li>{@link AttributeType#FLOAT_INTERVAL}</li>
 * </ul>
 *
 * @author msordi
 *
 */
public class FloatRangeRestriction extends AbstractRestriction {

    /**
     * Minimum possible value (included)
     */
    private Float minInclusive;

    /**
     * Maximun possible value (included)
     */
    private Float maxInclusive;

    /**
     * Minimum possible value (excluded)
     */
    private Float minExclusive;

    /**
     * Maximum possible value (excluded)
     */
    private Float maxExclusive;

    public FloatRangeRestriction() {
        super();
        setType(RestrictionType.FLOAT_RANGE);
    }

    public Float getMinInclusive() {
        return minInclusive;
    }

    public void setMinInclusive(Float pMinInclusive) {
        minInclusive = pMinInclusive;
    }

    public Float getMaxInclusive() {
        return maxInclusive;
    }

    public void setMaxInclusive(Float pMaxInclusive) {
        maxInclusive = pMaxInclusive;
    }

    public Float getMinExclusive() {
        return minExclusive;
    }

    public void setMinExclusive(Float pMinExclusive) {
        minExclusive = pMinExclusive;
    }

    public Float getMaxExclusive() {
        return maxExclusive;
    }

    public void setMaxExclusive(Float pMaxExclusive) {
        maxExclusive = pMaxExclusive;
    }

}
