/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.schema.FloatRange;
import fr.cnes.regards.modules.models.schema.Restriction;

/**
 *
 * Manage date format restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#FLOAT}</li>
 * <li>{@link AttributeType#FLOAT_ARRAY}</li>
 * <li>{@link AttributeType#FLOAT_INTERVAL}</li>
 * </ul>
 *
 * @author Marc Sordi
 *
 */
@Entity(name = "FloatRangeRestriction")
@DiscriminatorValue("FloatRange")
public class FloatRangeRestriction extends AbstractRestriction {

    /**
     * Minimum possible value (included)
     */
    @Column(name = "MINF_INCLUSIVE")
    private Float minInclusive;

    /**
     * Maximun possible value (included)
     */
    @Column(name = "MAXF_INCLUSIVE")
    private Float maxInclusive;

    /**
     * Minimum possible value (excluded)
     */
    @Column(name = "MINF_EXCLUSIVE")
    private Float minExclusive;

    /**
     * Maximum possible value (excluded)
     */
    @Column(name = "MAXF_EXCLUSIVE")
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

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.FLOAT.equals(pAttributeType) || AttributeType.FLOAT_ARRAY.equals(pAttributeType)
                || AttributeType.FLOAT_INTERVAL.equals(pAttributeType);
    }

    @Override
    public Boolean isPublic() {
        return Boolean.TRUE;
    }

    @Override
    public Restriction toXml() {

        final Restriction restriction = new Restriction();
        final FloatRange frr = new FloatRange();
        frr.setMaxExclusive(maxExclusive);
        frr.setMaxInclusive(maxInclusive);
        frr.setMinExclusive(minExclusive);
        frr.setMinInclusive(minInclusive);
        restriction.setFloatRange(frr);
        return restriction;
    }

    @Override
    public void fromXml(Restriction pXmlElement) {
        final FloatRange fr = pXmlElement.getFloatRange();
        setMaxExclusive(fr.getMaxExclusive());
        setMaxInclusive(fr.getMaxInclusive());
        setMinExclusive(fr.getMinExclusive());
        setMinInclusive(fr.getMinInclusive());
    }
}
