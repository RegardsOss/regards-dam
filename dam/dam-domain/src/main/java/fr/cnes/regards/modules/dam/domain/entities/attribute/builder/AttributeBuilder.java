/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.entities.attribute.builder;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.dam.domain.entities.attribute.*;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Function;

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
     * Converts to expected attribute value
     *
     * @param value to convert
     * @return converted attribute value
     * @throws IllegalArgumentException when conversion is not possible
     */
    static Boolean toBooleanValue(Object value) throws IllegalArgumentException {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.valueOf((String) value); // always returns a value
        }
        throw new IllegalArgumentException(String.format("Value '%s' cannot be converted into a boolean", value.toString()));
    }

    /**
     * Converts to expected attribute value
     *
     * @param value to convert
     * @return converted attribute value
     * @throws IllegalArgumentException when conversion is not possible
     */
    private static OffsetDateTime toDateValue(Object value) throws IllegalArgumentException {
        // only strings are accepted here as valid input
        if (value instanceof TemporalAccessor){
            return OffsetDateTime.from(((TemporalAccessor) value));
        }
        if (value instanceof  Date){
            return OffsetDateTime.from(((Date) value).toInstant());
        }
        if (value instanceof String) {
            try {
                return OffsetDateTimeAdapter.parse((String) value);
            } catch (DateTimeException e) {
                // do nothing, raise final exception instead
            }
        }
        throw new IllegalArgumentException(String.format("Value '%s' cannot be converted into a date", value.toString()));
    }

    /**
     * Converts to expected attribute value
     *
     * @param value to convert
     * @return converted attribute value
     * @throws IllegalArgumentException when conversion is not possible
     */
    static Double toDoubleValue(Object value) throws IllegalArgumentException {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException e) {
                // do nothing, raise final exception instead
            }
        }
        throw new IllegalArgumentException(String.format("Value '%s' cannot be converted into a double number", value.toString()));
    }

    /**
     * Converts to expected attribute value
     *
     * @param value to convert
     * @return converted attribute value
     * @throws IllegalArgumentException when conversion is not possible
     */
    static Integer toIntegerValue(Object value) throws IllegalArgumentException {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.valueOf((String) value);
            } catch (NumberFormatException e) {
                // do nothing, raise final exception instead
            }
        }
        throw new IllegalArgumentException(String.format("Input value '%s' cannot be converted into an integer number", value.toString()));
    }

    /**
     * Converts to expected attribute value
     *
     * @param value to convert
     * @return converted attribute value
     * @throws IllegalArgumentException when conversion is not possible
     */
    static Long toLongValue(Object value) throws IllegalArgumentException {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.valueOf((String) value);
            } catch (NumberFormatException e) {
                // do nothing, raise final exception instead
            }
        }
        throw new IllegalArgumentException(String.format("Input value '%s' cannot be converted into a long number", value.toString()));
    }

    /**
     * Converts to expected attribute value
     *
     * @param value to convert
     * @return converted attribute value
     */
    static String toStringValue(Object value) {
        return value.toString();
    }

    /**
     * Converts to expected attribute value
     *
     * @param value to convert
     * @return converted attribute value
     * @throws IllegalArgumentException when conversion is not possible
     */
    static URL toURLValue(Object value) throws IllegalArgumentException {
        if (value instanceof URL) {
            return (URL) value;
        }
        if (value instanceof String) {
            try {
                return new URL((String) value);
            } catch (MalformedURLException e) {
                // do nothing, raise final exception instead
            }
        }
        throw new IllegalArgumentException(String.format("Input value '%s' cannot be converted into an URL", value.toString()));
    }


    /**
     * Converts value into typed array value.
     *
     * @param value             input value from GEOJson results. Both array and lists are supproted
     * @param elementsConverter array elements converter, that returns null when an element cannot be converted
     * @param elementsClass     targeted elements class
     * @param <T>               Array elements type
     * @return converted list
     * @throws IllegalArgumentException when conversion is not possible
     */
    static <T> T[] toArrayValue(Object value, Function<Object, T> elementsConverter, Class<T> elementsClass) throws IllegalArgumentException {
        Collection<?> sourceList = null;
        List<String> invalidValues = new ArrayList<>();

        // 1 - recover a list of elements
        if (value.getClass().isArray()) {
            // Convert each value to date then return array if there were no error
            sourceList = Arrays.asList((Object[]) value);
        } else if (value instanceof Collection) {
            sourceList = (Collection<?>) value;
        } else {
            throw new IllegalArgumentException(String.format("Input value '%s' cannot be converted into an %s[] (expected array or collection types)", value.toString(), elementsClass.getName()));
        }
        // 2 - convert each element
        ArrayList<T> converted = new ArrayList<>(sourceList.size());
        for (Object elt : sourceList) {
            try {
                converted.add(elt == null ? null : elementsConverter.apply(elt));
            } catch (IllegalArgumentException e) {
                invalidValues.add(elt == null ? null : elt.toString());
            }
        }
        // 3 - return converted array or throw exception
        if (invalidValues.isEmpty()) {
            //noinspection unchecked
            return converted.toArray((T[]) Array.newInstance(elementsClass, converted.size()));
        } else {
            throw new IllegalArgumentException(String.format("In input array, the values '%s' could not be converted into %s", String.join(",", invalidValues), elementsClass.getName()));
        }
    }

    /**
     * Method allowing to get an AbstractAttribute according to the AttributeType, for the given name and value. The
     * type of pValue is expected to be coherent with the AttributeType. In particular, for intervals we are expecting
     * {@link Range}. For other elements, value parsing will be attempted if they are provided as string.
     *
     * @param attributeType Type of the attribute to be created
     * @param name          name of the attribute to be created
     * @param value         value of the attribute to be created
     * @return a newly created AbstractAttribute according the given AttributeType, name and value
     * @throws IllegalArgumentException when the value cannot converted into expected type for attribute
     */
    @SuppressWarnings("unchecked")
    public static AbstractAttribute<?> forType(AttributeType attributeType, String name, Object value) throws IllegalArgumentException {
        if (name == null || attributeType == null) {
            throw new IllegalArgumentException("An attribute cannot have a null name");
        }
        if (value == null) {
            return forTypeWithNullValue(attributeType, name);
        }

        switch (attributeType) {
            case BOOLEAN:
                return buildBoolean(name, toBooleanValue(value));
            case DATE_ARRAY:
                return buildDateArray(name, toArrayValue(value, AttributeBuilder::toDateValue, OffsetDateTime.class));
            case DATE_INTERVAL:
                return buildDateInterval(name, (Range<OffsetDateTime>) value);
            case DATE_ISO8601:
                return buildDate(name, toDateValue(value));
            case DOUBLE:
                return buildDouble(name, toDoubleValue(value));
            case DOUBLE_ARRAY:
                return buildDoubleArray(name, toArrayValue(value, AttributeBuilder::toDoubleValue, Double.class));
            case DOUBLE_INTERVAL:
                return buildDoubleInterval(name, (Range<Double>) value);
            case INTEGER:
                return buildInteger(name, toIntegerValue(value));
            case INTEGER_ARRAY:
                return buildIntegerArray(name, toArrayValue(value, AttributeBuilder::toIntegerValue, Integer.class));
            case INTEGER_INTERVAL:
                return buildIntegerInterval(name, (Range<Integer>) value);
            case LONG:
                return buildLong(name, toLongValue((value)));
            case LONG_ARRAY:
                return buildLongArray(name, toArrayValue(value, AttributeBuilder::toLongValue, Long.class));
            case LONG_INTERVAL:
                return buildLongInterval(name, (Range<Long>) value);
            case STRING:
                return buildString(name, toStringValue(value));
            case STRING_ARRAY:
                return buildStringArray(name, toArrayValue(value, AttributeBuilder::toStringValue, String.class));
            case URL:
                return buildUrl(name, toURLValue(value));
            default:
                throw new IllegalArgumentException(attributeType + " is not a handled value of "
                        + AttributeType.class.getName() + " in " + AttributeBuilder.class.getName());
        }
    }

    /**
     * Method allowing to get an AbstractAttribute for an <b>interval</b> AttributeType, for the given name and
     * values. The type of values is expected to be coherent with the AttributeType :
     * <ul>
     * <li>we are expecting an ISO 8601 string for dates</li>
     * <li>a number for double, integer and long</li>
     * </ul>
     *
     * @param <U>           type of the value
     * @param <T>           type of the attribute generated
     * @param attributeType Type of the attribute created
     * @param name          name of the attribute to be created
     * @param lowerBound    value of the attribute to be created
     * @param upperBound    value of the attribute to be created
     * @return a newly created AbstractAttribute according the given AttributeType, name and value
     */
    @SuppressWarnings("unchecked")
    public static <U, T extends AbstractAttribute<U>> T forType(AttributeType attributeType, String name, U lowerBound,
                                                                U upperBound) {

        if (!attributeType.isInterval()) {
            throw new IllegalArgumentException(attributeType + " with name " + name + " is not an interval type");
        }

        if ((lowerBound == null) && (upperBound == null)) {
            return forTypeWithNullValue(attributeType, name);
        }

        switch (attributeType) {
            case DATE_INTERVAL:
                OffsetDateTime lowerDateTime = lowerBound == null ? null
                        : OffsetDateTimeAdapter.parse((String) lowerBound);
                OffsetDateTime upperDateTime = upperBound == null ? null
                        : OffsetDateTimeAdapter.parse((String) upperBound);
                return (T) buildDateInterval(name, buildRange(lowerDateTime, upperDateTime));
            case DOUBLE_INTERVAL:
                Double lowerDouble = lowerBound == null ? null : ((Number) lowerBound).doubleValue();
                Double upperDouble = upperBound == null ? null : ((Number) upperBound).doubleValue();
                return (T) buildDoubleInterval(name, buildRange(lowerDouble, upperDouble));
            case INTEGER_INTERVAL:
                Integer lowerInteger = lowerBound == null ? null : ((Number) lowerBound).intValue();
                Integer upperInteger = upperBound == null ? null : ((Number) upperBound).intValue();
                return (T) buildIntegerInterval(name, buildRange(lowerInteger, upperInteger));
            case LONG_INTERVAL:
                Long lowerLong = lowerBound == null ? null : ((Number) lowerBound).longValue();
                Long upperLong = upperBound == null ? null : ((Number) upperBound).longValue();
                return (T) buildLongInterval(name, buildRange(lowerLong, upperLong));
            default:
                throw new IllegalArgumentException(attributeType + " is not a handled value of "
                        + AttributeType.class.getName() + " in " + AttributeBuilder.class.getName());
        }
    }

    /**
     * Build a range considering null value for one of the bound
     *
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return a range representation
     */
    private static <U extends Comparable<?>> Range<U> buildRange(U lowerBound, U upperBound) {
        if (lowerBound == null) {
            return Range.atMost(upperBound);
        } else if (upperBound == null) {
            return Range.atLeast(lowerBound);
        } else {
            return Range.closed(lowerBound, upperBound);
        }
    }

    @SuppressWarnings("unchecked")
    public static <U, T extends AbstractAttribute<?>> T forTypeWithNullValue(AttributeType attributeType, String name) {
        switch (attributeType) {
            case INTEGER:
                return (T) buildInteger(name, null);
            case BOOLEAN:
                return (T) buildBoolean(name, null);
            case DATE_ARRAY:
                return (T) buildDateArray(name);
            case DATE_INTERVAL:
                return (T) buildDateInterval(name, null);
            case DATE_ISO8601:
                return (T) buildDate(name, null);
            case DOUBLE:
                return (T) buildDouble(name, null);
            case DOUBLE_ARRAY:
                return (T) buildDoubleArray(name);
            case DOUBLE_INTERVAL:
                return (T) buildDoubleInterval(name, null);
            case INTEGER_ARRAY:
                return (T) buildIntegerArray(name);
            case INTEGER_INTERVAL:
                return (T) buildIntegerInterval(name, null);
            case LONG:
                return (T) buildLong(name, null);
            case LONG_ARRAY:
                return (T) buildLongArray(name, null);
            case LONG_INTERVAL:
                return (T) buildLongInterval(name, null);
            case STRING:
                return (T) buildString(name, null);
            case STRING_ARRAY:
                return (T) buildStringArray(name);
            case URL:
                return (T) buildUrl(name);
            default:
                throw new IllegalArgumentException(attributeType + " is not a handled value of "
                        + AttributeType.class.getName() + " in " + AttributeBuilder.class.getName());
        }
    }

    private static LongIntervalAttribute buildLongInterval(String name, Range<Long> value) {
        LongIntervalAttribute att = new LongIntervalAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    private static IntegerIntervalAttribute buildIntegerInterval(String name, Range<Integer> value) {
        IntegerIntervalAttribute att = new IntegerIntervalAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    private static DoubleIntervalAttribute buildDoubleInterval(String name, Range<Double> value) {
        DoubleIntervalAttribute att = new DoubleIntervalAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    private static DateIntervalAttribute buildDateInterval(String name, Range<OffsetDateTime> value) {
        DateIntervalAttribute att = new DateIntervalAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static UrlAttribute buildUrl(String name, URL value) {
        UrlAttribute att = new UrlAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static UrlAttribute buildUrl(String name) {
        UrlAttribute att = new UrlAttribute();
        att.setName(name);
        att.setValue(null);
        return att;
    }

    public static UrlAttribute buildUrl(String name, String value) {
        UrlAttribute att = new UrlAttribute();
        att.setName(name);
        try {
            att.setValue(new URL(value));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(name + " is not a handled value of " + URL.class.getName());
        }
        return att;
    }

    public static BooleanAttribute buildBoolean(String name, Boolean value) {
        BooleanAttribute att = new BooleanAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static DateArrayAttribute buildDateArray(String name, OffsetDateTime... offsetDateTimes) {
        DateArrayAttribute att = new DateArrayAttribute();
        att.setName(name);
        att.setValue(offsetDateTimes);
        return att;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static DateArrayAttribute buildDateCollection(String name, Collection offsetDateTimes) {
        DateArrayAttribute att = new DateArrayAttribute();
        att.setName(name);
        if (offsetDateTimes instanceof HashSet<?>) {
            att.setValue(((Set<OffsetDateTime>) offsetDateTimes).stream().toArray(OffsetDateTime[]::new));
        } else if (offsetDateTimes instanceof ArrayList<?>) {
            att.setValue(((ArrayList<OffsetDateTime>) offsetDateTimes).stream().toArray(OffsetDateTime[]::new));
        }
        return att;
    }

    public static DateAttribute buildDate(String name, OffsetDateTime offsetDateTime) {
        DateAttribute att = new DateAttribute();
        att.setName(name);
        att.setValue(offsetDateTime);
        return att;
    }

    public static DateIntervalAttribute buildDateInterval(String name, OffsetDateTime lowerBoundDate,
                                                          OffsetDateTime upperBoundDate) {
        DateIntervalAttribute att = new DateIntervalAttribute();
        att.setName(name);
        att.setValue(Range.closed(lowerBoundDate, upperBoundDate));
        return att;
    }

    public static DoubleArrayAttribute buildDoubleArray(String pName, Double... pValues) {
        DoubleArrayAttribute att = new DoubleArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static DoubleArrayAttribute buildDoubleCollection(String name, Collection values) {
        DoubleArrayAttribute att = new DoubleArrayAttribute();
        att.setName(name);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Double>) values).stream().toArray(Double[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Double>) values).stream().toArray(Double[]::new));
        }
        return att;
    }

    public static DoubleAttribute buildDouble(String name, Double value) {
        DoubleAttribute att = new DoubleAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static DoubleIntervalAttribute buildDoubleInterval(String name, Double lowerBoundDouble,
                                                              Double upperBoundDouble) {
        DoubleIntervalAttribute att = new DoubleIntervalAttribute();
        att.setName(name);
        att.setValue(Range.closed(lowerBoundDouble, upperBoundDouble));
        return att;
    }

    public static IntegerArrayAttribute buildIntegerArray(String name, Integer... values) {
        IntegerArrayAttribute att = new IntegerArrayAttribute();
        att.setName(name);
        att.setValue(values);
        return att;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static IntegerArrayAttribute buildIntegerCollection(String name, Collection values) {
        IntegerArrayAttribute att = new IntegerArrayAttribute();
        att.setName(name);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Integer>) values).stream().toArray(Integer[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Integer>) values).stream().toArray(Integer[]::new));
        }
        return att;
    }

    public static AbstractAttribute<?> buildInteger(String name, Integer value) {
        IntegerAttribute att = new IntegerAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static IntegerIntervalAttribute buildIntegerInterval(String name, Integer lowerBoundInteger,
                                                                Integer upperBoundInteger) {
        IntegerIntervalAttribute att = new IntegerIntervalAttribute();
        att.setName(name);
        att.setValue(Range.closed(lowerBoundInteger, upperBoundInteger));
        return att;
    }

    public static LongArrayAttribute buildLongArray(String name, Long... values) {
        LongArrayAttribute att = new LongArrayAttribute();
        att.setName(name);
        att.setValue(values);
        return att;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static LongArrayAttribute buildLongCollection(String name, Collection values) {
        LongArrayAttribute att = new LongArrayAttribute();
        att.setName(name);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Long>) values).stream().toArray(Long[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Long>) values).stream().toArray(Long[]::new));
        }
        return att;
    }

    public static LongAttribute buildLong(String name, Long value) {
        LongAttribute att = new LongAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static LongIntervalAttribute buildLongInterval(String name, Long lowerBoundLong, Long upperBoundLong) {
        LongIntervalAttribute att = new LongIntervalAttribute();
        att.setName(name);
        att.setValue(Range.closed(lowerBoundLong, upperBoundLong));
        return att;
    }

    public static ObjectAttribute buildObject(String name, AbstractAttribute<?>... attributes) {
        ObjectAttribute att = new ObjectAttribute();
        att.setName(name);
        att.setValue(Sets.newHashSet(attributes));
        return att;
    }

    public static StringArrayAttribute buildStringArray(String name, String... values) {
        StringArrayAttribute att = new StringArrayAttribute();
        att.setName(name);
        att.setValue(values);
        return att;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static StringArrayAttribute buildStringCollection(String name, Collection values) {
        StringArrayAttribute att = new StringArrayAttribute();
        att.setName(name);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<String>) values).stream().toArray(String[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<String>) values).stream().toArray(String[]::new));
        }
        return att;
    }

    public static StringAttribute buildString(String name, String value) {
        StringAttribute att = new StringAttribute();
        att.setName(name);
        att.setValue(value);
        return att;
    }
}
