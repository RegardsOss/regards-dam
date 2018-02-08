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
package fr.cnes.regards.modules.entities.domain.attribute.builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Arrays;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
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
     * @param <U> type of the value
     * @param <T> type of the attribute generated
     * @param attributeType Type of the attribute created
     * @param name name of the attribute to be created
     * @param value value of the attribute to be created
     * @return a newly created AbstractAttribute according the given AttributeType, name and value
     */
    @SuppressWarnings("unchecked")
    public static <U, T extends AbstractAttribute<U>> T forType(AttributeType attributeType, String name, U value) {
        if (value == null) {
            return forTypeWithNullValue(attributeType, name);
        }

        switch (attributeType) {
            case INTEGER:
                return (T) buildInteger(name, ((Number) value).intValue());
            case BOOLEAN:
                return (T) buildBoolean(name, (Boolean) value);
            case DATE_ARRAY:
                if (value instanceof String[]) {
                    return (T) buildDateArray(name,
                                              Arrays.stream((String[]) value).map(v -> OffsetDateTimeAdapter.parse(v))
                                                      .toArray(size -> new OffsetDateTime[size]));
                }
                return (T) buildDateArray(name, (OffsetDateTime[]) value);
            case DATE_INTERVAL:
                return (T) buildDateInterval(name, (Range<OffsetDateTime>) value);
            case DATE_ISO8601:
                if (value instanceof String) {
                    return (T) buildDate(name, OffsetDateTimeAdapter.parse((String) value));
                }
                return (T) buildDate(name, (OffsetDateTime) value);
            case DOUBLE:
                return (T) buildDouble(name, ((Number) value).doubleValue());
            case DOUBLE_ARRAY:
                return (T) buildDoubleArray(name, Arrays.stream((Number[]) value).mapToDouble(n -> n.doubleValue())
                        .mapToObj(Double::new).toArray(size -> new Double[size]));
            case DOUBLE_INTERVAL:
                return (T) buildDoubleInterval(name, (Range<Double>) value);
            case INTEGER_ARRAY:
                return (T) buildIntegerArray(name, Arrays.stream(((Number[]) value)).mapToInt(v -> v.intValue())
                        .mapToObj(Integer::new).toArray(size -> new Integer[size]));
            case INTEGER_INTERVAL:
                return (T) buildIntegerInterval(name, (Range<Integer>) value);
            case LONG:
                return (T) buildLong(name, ((Number) value).longValue());
            case LONG_ARRAY:
                return (T) buildLongArray(name, Arrays.stream(((Number[]) value)).mapToLong(v -> v.longValue())
                        .mapToObj(Long::new).toArray(size -> new Long[size]));
            case LONG_INTERVAL:
                return (T) buildLongInterval(name, (Range<Long>) value);
            case STRING:
                return (T) buildString(name, (String) value);
            case STRING_ARRAY:
                return (T) buildStringArray(name, (String[]) value);
            case URL:
                return (T) buildUrl(name, (URL) value);
            default:
                throw new IllegalArgumentException(
                        attributeType + " is not a handled value of " + AttributeType.class.getName() + " in "
                                + AttributeBuilder.class.getName());
        }
    }

    public static <U, T extends AbstractAttribute<U>> T forTypeWithNullValue(AttributeType pAttributeType,
            String pName) {
        switch (pAttributeType) {
            case INTEGER:
                return (T) buildInteger(pName, null);
            case BOOLEAN:
                return (T) buildBoolean(pName, null);
            case DATE_ARRAY:
                return (T) buildDateArray(pName, null);
            case DATE_INTERVAL:
                return (T) buildDateInterval(pName, null);
            case DATE_ISO8601:
                return (T) buildDate(pName, null);
            case DOUBLE:
                return (T) buildDouble(pName, null);
            case DOUBLE_ARRAY:
                return (T) buildDoubleArray(pName, null);
            case DOUBLE_INTERVAL:
                return (T) buildDoubleInterval(pName, null);
            case INTEGER_ARRAY:
                return (T) buildIntegerArray(pName, null);
            case INTEGER_INTERVAL:
                return (T) buildIntegerInterval(pName, null);
            case LONG:
                return (T) buildLong(pName, null);
            case LONG_ARRAY:
                return (T) buildLongArray(pName, null);
            case LONG_INTERVAL:
                return (T) buildLongInterval(pName, null);
            case STRING:
                return (T) buildString(pName, null);
            case STRING_ARRAY:
                return (T) buildStringArray(pName, null);
            case URL:
                return (T) buildUrl(pName, null);
            default:
                throw new IllegalArgumentException(
                        pAttributeType + " is not a handled value of " + AttributeType.class.getName() + " in "
                                + AttributeBuilder.class.getName());
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

    public static UrlAttribute buildUrl(String pName, URL pValue) {
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
