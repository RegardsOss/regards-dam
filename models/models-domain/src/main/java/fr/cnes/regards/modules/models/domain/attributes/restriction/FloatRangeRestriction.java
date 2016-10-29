/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
@Entity(name = "FloatRangeRestriction")
@DiscriminatorValue("FloatRange")
public class FloatRangeRestriction extends AbstractRestriction {

    /**
     * Minimum possible value (included)
     */
    private Float minfInclusive;

    /**
     * Maximun possible value (included)
     */
    private Float maxfInclusive;

    /**
     * Minimum possible value (excluded)
     */
    private Float minfExclusive;

    /**
     * Maximum possible value (excluded)
     */
    private Float maxfExclusive;

    public FloatRangeRestriction() {
        super();
        setType(RestrictionType.FLOAT_RANGE);
    }

    public Float getMinfInclusive() {
        return minfInclusive;
    }

    public void setMinfInclusive(Float pMinfInclusive) {
        minfInclusive = pMinfInclusive;
    }

    public Float getMaxfInclusive() {
        return maxfInclusive;
    }

    public void setMaxfInclusive(Float pMaxfInclusive) {
        maxfInclusive = pMaxfInclusive;
    }

    public Float getMinfExclusive() {
        return minfExclusive;
    }

    public void setMinfExclusive(Float pMinfExclusive) {
        minfExclusive = pMinfExclusive;
    }

    public Float getMaxfExclusive() {
        return maxfExclusive;
    }

    public void setMaxfExclusive(Float pMaxfExclusive) {
        maxfExclusive = pMaxfExclusive;
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.FLOAT.equals(pAttributeType) || AttributeType.FLOAT_ARRAY.equals(pAttributeType)
                || AttributeType.FLOAT_INTERVAL.equals(pAttributeType);
    }
}
