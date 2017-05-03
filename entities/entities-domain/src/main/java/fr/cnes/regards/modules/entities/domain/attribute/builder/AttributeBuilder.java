/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute.builder;

import java.net.URL;
import java.time.OffsetDateTime;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.BooleanAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.LongArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.LongAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.LongIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.UrlAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Attribute builder
 *
 * @author Marc Sordi
 * @author oroussel
 * @author Sylvain Vissiere-Guerinet
 */
public final class AttributeBuilder {

    private AttributeBuilder() {

    }

    /**
     * Method allowing to get an AbstractAttribute according to the AttributeType, for the given name and value. The
     * type of pValue is expected to be coherant with the AttributeType. In particular, for intervals we are expecting
     * {@link Range} and as dates we are expecting {@link OffsetDateTime}
     *
     * @param <U> type of the value
     * @param <T> type of the attribute generated
     * @param pAttributeType Type of the attribute created
     * @param pName name of the attribute to be created
     * @param pValue value of the attribute to be created
     * @return a newly created AbstractAttribute according the given AttributeType, name and value
     */
    @SuppressWarnings("unchecked")
    public static <U, T extends AbstractAttribute<U>> T forType(AttributeType pAttributeType, String pName, U pValue) {
        switch (pAttributeType) {
            case INTEGER:
                return (T) buildInteger(pName, (Integer) pValue);
            case BOOLEAN:
                return (T) buildBoolean(pName, (Boolean) pValue);
            case DATE_ARRAY:
                return (T) buildDateArray(pName, (OffsetDateTime[]) pValue);
            case DATE_INTERVAL:
                return (T) buildDateInterval(pName, (Range<OffsetDateTime>) pValue);
            case DATE_ISO8601:
                return (T) buildDate(pName, (OffsetDateTime) pValue);
            case DOUBLE:
                return (T) buildDouble(pName, (Double) pValue);
            case DOUBLE_ARRAY:
                return (T) buildDoubleArray(pName, (Double[]) pValue);
            case DOUBLE_INTERVAL:
                return (T) buildDoubleInterval(pName, (Range<Double>) pValue);
            case INTEGER_ARRAY:
                return (T) buildIntegerArray(pName, (Integer[]) pValue);
            case INTEGER_INTERVAL:
                return (T) buildIntegerInterval(pName, (Range<Integer>) pValue);
            case LONG:
                return (T) buildLong(pName, (Long) pValue);
            case LONG_ARRAY:
                return (T) buildLongArray(pName, (Long[]) pValue);
            case LONG_INTERVAL:
                return (T) buildLongInterval(pName, (Range<Long>) pValue);
            case STRING:
                return (T) buildString(pName, (String) pValue);
            case STRING_ARRAY:
                return (T) buildStringArray(pName, (String[]) pValue);
            case URL:
                return (T) buildUrl(pName, (URL) pValue);
            default:
                throw new IllegalArgumentException(pAttributeType + " is not a handled value of "
                        + AttributeType.class.getName() + " in " + AttributeBuilder.class.getName());
        }
    }

    private static LongIntervalAttribute buildLongInterval(String pName, Range<Long> pValue) {
        LongIntervalAttribute att = new LongIntervalAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    private static IntegerIntervalAttribute buildIntegerInterval(String pName, Range<Integer> pValue) {
        IntegerIntervalAttribute att = new IntegerIntervalAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    private static DoubleIntervalAttribute buildDoubleInterval(String pName, Range<Double> pValue) {
        DoubleIntervalAttribute att = new DoubleIntervalAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    private static DateIntervalAttribute buildDateInterval(String pName, Range<OffsetDateTime> pValue) {
        DateIntervalAttribute att = new DateIntervalAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    private static UrlAttribute buildUrl(String pName, URL pValue) {
        UrlAttribute att = new UrlAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static BooleanAttribute buildBoolean(String pName, Boolean pValue) {
        BooleanAttribute att = new BooleanAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static DateArrayAttribute buildDateArray(String pName, OffsetDateTime... pOffsetDateTimes) {
        DateArrayAttribute att = new DateArrayAttribute();
        att.setName(pName);
        att.setValue(pOffsetDateTimes);
        return att;
    }

    public static DateAttribute buildDate(String pName, OffsetDateTime pOffsetDateTime) {
        DateAttribute att = new DateAttribute();
        att.setName(pName);
        att.setValue(pOffsetDateTime);
        return att;
    }

    public static DateIntervalAttribute buildDateInterval(String pName, OffsetDateTime pLowerBoundDate,
            OffsetDateTime pUpperBoundDate) {
        DateIntervalAttribute att = new DateIntervalAttribute();
        att.setName(pName);
        att.setValue(Range.closed(pLowerBoundDate, pUpperBoundDate));
        return att;
    }

    public static DoubleArrayAttribute buildDoubleArray(String pName, Double... pValues) {
        DoubleArrayAttribute att = new DoubleArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    public static DoubleAttribute buildDouble(String pName, Double pValue) {
        DoubleAttribute att = new DoubleAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static DoubleIntervalAttribute buildDoubleInterval(String pName, Double pLowerBoundDouble,
            Double pUpperBoundDouble) {
        DoubleIntervalAttribute att = new DoubleIntervalAttribute();
        att.setName(pName);
        att.setValue(Range.closed(pLowerBoundDouble, pUpperBoundDouble));
        return att;
    }

    public static IntegerArrayAttribute buildIntegerArray(String pName, Integer... pValues) {
        IntegerArrayAttribute att = new IntegerArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    public static IntegerAttribute buildInteger(String pName, Integer pValue) {
        IntegerAttribute att = new IntegerAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static IntegerIntervalAttribute buildIntegerInterval(String pName, Integer pLowerBoundInteger,
            Integer pUpperBoundInteger) {
        IntegerIntervalAttribute att = new IntegerIntervalAttribute();
        att.setName(pName);
        att.setValue(Range.closed(pLowerBoundInteger, pUpperBoundInteger));
        return att;
    }

    public static LongArrayAttribute buildLongArray(String pName, Long... pValues) {
        LongArrayAttribute att = new LongArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    public static LongAttribute buildLong(String pName, Long pValue) {
        LongAttribute att = new LongAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static LongIntervalAttribute buildLongInterval(String pName, Long pLowerBoundLong, Long pUpperBoundLong) {
        LongIntervalAttribute att = new LongIntervalAttribute();
        att.setName(pName);
        att.setValue(Range.closed(pLowerBoundLong, pUpperBoundLong));
        return att;
    }

    public static ObjectAttribute buildObject(String pName, AbstractAttribute<?>... pAttributes) {
        ObjectAttribute att = new ObjectAttribute();
        att.setName(pName);
        att.setValue(Sets.newHashSet(pAttributes));
        return att;
    }

    public static StringArrayAttribute buildStringArray(String pName, String... pValues) {
        StringArrayAttribute att = new StringArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    public static StringAttribute buildString(String pName, String pValue) {
        StringAttribute att = new StringAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }
}
