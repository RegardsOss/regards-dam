/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.validator.CheckFloatRange;
import fr.cnes.regards.modules.models.schema.FloatRange;
import fr.cnes.regards.modules.models.schema.FloatRange.Max;
import fr.cnes.regards.modules.models.schema.FloatRange.Min;
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
@CheckFloatRange
@Entity(name = "FloatRangeRestriction")
@DiscriminatorValue("FloatRange")
public class FloatRangeRestriction extends AbstractRestriction {

    /**
     * Minimum possible value (included)
     */
    @Column(name = "MINF")
    @NotNull
    private Double min;

    /**
     * Maximun possible value (included)
     */
    @Column(name = "MAXF")
    @NotNull
    private Double max;

    /**
     * Minimum possible value (excluded)
     */
    @Column(name = "MINF_EXCLUDED")
    private boolean minExcluded = false;

    /**
     * Maximum possible value (excluded)
     */
    @Column(name = "MAXF_EXCLUDED")
    private boolean maxExcluded = false;

    public FloatRangeRestriction() {// NOSONAR
        super();
        setType(RestrictionType.FLOAT_RANGE);
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.FLOAT.equals(pAttributeType) || AttributeType.FLOAT_ARRAY.equals(pAttributeType)
                || AttributeType.FLOAT_INTERVAL.equals(pAttributeType);
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
        final FloatRange frr = new FloatRange();

        Max xmlMax = new Max();
        xmlMax.setValue(max);
        xmlMax.setExcluded(maxExcluded);
        frr.setMax(xmlMax);

        Min xmlMin = new Min();
        xmlMin.setValue(min);
        xmlMin.setExcluded(minExcluded);
        frr.setMin(xmlMin);

        restriction.setFloatRange(frr);
        return restriction;
    }

    @Override
    public void fromXml(Restriction pXmlElement) {
        final FloatRange fr = pXmlElement.getFloatRange();
        Max xmlMax = fr.getMax();
        setMax(xmlMax.getValue());
        setMaxExcluded(xmlMax.isExcluded());
        Min xmlMin = fr.getMin();
        setMin(xmlMin.getValue());
        setMinExcluded(xmlMin.isExcluded());
    }

}
