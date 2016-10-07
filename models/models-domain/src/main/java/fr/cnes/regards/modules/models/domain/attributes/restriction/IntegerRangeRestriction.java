/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Manage date format restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#INTEGER}</li>
 * <li>{@link AttributeType#INTEGER_ARRAY}</li>
 * <li>{@link AttributeType#INTEGER_INTERVAL}</li>
 * </ul>
 *
 * @author msordi
 *
 */
public class IntegerRangeRestriction extends AbstractRestriction {

    /**
     * Minimum possible value (included)
     */
    private Integer minInclusive;

    /**
     * Maximun possible value (included)
     */
    private Integer maxInclusive;

    /**
     * Minimum possible value (excluded)
     */
    private Integer minExclusive;

    /**
     * Maximum possible value (excluded)
     */
    private Integer maxExclusive;

    public IntegerRangeRestriction() {
        super();
        setType(RestrictionType.INTEGER_RANGE);
    }

    public Integer getMinInclusive() {
        return minInclusive;
    }

    public void setMinInclusive(Integer pMinInclusive) {
        minInclusive = pMinInclusive;
    }

    public Integer getMaxInclusive() {
        return maxInclusive;
    }

    public void setMaxInclusive(Integer pMaxInclusive) {
        maxInclusive = pMaxInclusive;
    }

    public Integer getMinExclusive() {
        return minExclusive;
    }

    public void setMinExclusive(Integer pMinExclusive) {
        minExclusive = pMinExclusive;
    }

    public Integer getMaxExclusive() {
        return maxExclusive;
    }

    public void setMaxExclusive(Integer pMaxExclusive) {
        maxExclusive = pMaxExclusive;
    }
}
