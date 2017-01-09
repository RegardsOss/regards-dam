/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.restriction.validator.CheckIntegerRange;
import fr.cnes.regards.modules.models.schema.IntegerRange;
import fr.cnes.regards.modules.models.schema.IntegerRange.Max;
import fr.cnes.regards.modules.models.schema.IntegerRange.Min;
import fr.cnes.regards.modules.models.schema.Restriction;

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
@CheckIntegerRange
@Entity
@DiscriminatorValue("INTEGER_RANGE")
public class IntegerRangeRestriction extends AbstractRestriction {

    /**
     * Minimum possible value (included)
     */
    @Column(name = "mini")
    @NotNull
    private Integer min;

    /**
     * Maximun possible value (included)
     */
    @Column(name = "maxi")
    @NotNull
    private Integer max;

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
        setType(RestrictionType.INTEGER_RANGE);
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.INTEGER.equals(pAttributeType) || AttributeType.INTEGER_ARRAY.equals(pAttributeType)
                || AttributeType.INTEGER_INTERVAL.equals(pAttributeType);
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer pMin) {
        min = pMin;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer pMax) {
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
