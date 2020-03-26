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
package fr.cnes.regards.modules.model.dto.properties;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.springframework.util.Assert;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.model.dto.properties.logger.PropertyPatchLogger;

/**
 * @param <T> type of Attribute
 * @author lmieulet
 * @author Marc Sordi
 */
public interface IProperty<T> extends Comparable<IProperty<T>> {

    /**
     * Get attribute name
     * @return attribute name
     */
    String getName();

    /**
     * @return the attribute value
     */
    T getValue();

    /**
     * Allows to update property value.
     * @param value new value or <code>null</code>
     */
    void updateValue(T value);

    boolean represents(PropertyType type);

    PropertyType getType();

    @Override
    default int compareTo(IProperty<T> o) {
        // name is not null (mandatory)
        return this.getName().compareToIgnoreCase(o.getName());
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
        throw new IllegalArgumentException(
                String.format("Value '%s' cannot be converted into a boolean", value.toString()));
    }

    /**
     * Converts to expected attribute value
     *
     * @param value to convert
     * @return converted attribute value
     * @throws IllegalArgumentException when conversion is not possible
     */
    static OffsetDateTime toDateValue(Object value) throws IllegalArgumentException {
        // only strings are accepted here as valid input
        if (value instanceof TemporalAccessor) {
            return OffsetDateTime.from((TemporalAccessor) value);
        }
        if (value instanceof Date) {
            return OffsetDateTime.from(((Date) value).toInstant());
        }
        if (value instanceof String) {
            try {
                return OffsetDateTimeAdapter.parse((String) value);
            } catch (DateTimeException e) {
                // do nothing, raise final exception instead
            }
        }
        throw new IllegalArgumentException(
                String.format("Value '%s' cannot be converted into a date", value.toString()));
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
        throw new IllegalArgumentException(
                String.format("Value '%s' cannot be converted into a double number", value.toString()));
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
        throw new IllegalArgumentException(
                String.format("Input value '%s' cannot be converted into an integer number", value.toString()));
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
        throw new IllegalArgumentException(
                String.format("Input value '%s' cannot be converted into a long number", value.toString()));
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
    static MarkdownURL toURLValue(Object value) throws IllegalArgumentException {
        if (value instanceof MarkdownURL) {
            return (MarkdownURL) value;
        }
        if (value instanceof String) {
            try {
                return MarkdownURL.build((String) value);
            } catch (MalformedURLException e) {
                // do nothing, raise final exception instead
            }
        }
        throw new IllegalArgumentException(
                String.format("Input value '%s' cannot be converted into an URL", value.toString()));
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
    @SuppressWarnings("unchecked")
    static <T> T[] toArrayValue(Object value, Function<Object, T> elementsConverter, Class<T> elementsClass)
            throws IllegalArgumentException {
        Collection<?> sourceList = null;
        List<String> invalidValues = new ArrayList<>();

        // 1 - recover a list of elements
        if (value.getClass().isArray()) {
            // Convert each value to date then return array if there were no error
            sourceList = Arrays.asList((Object[]) value);
        } else if (value instanceof Collection) {
            sourceList = (Collection<?>) value;
        } else {
            throw new IllegalArgumentException(String
                    .format("Input value '%s' cannot be converted into an %s[] (expected array or collection types)",
                            value.toString(), elementsClass.getName()));
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
            throw new IllegalArgumentException(
                    String.format("In input array, the values '%s' could not be converted into %s",
                                  String.join(",", invalidValues), elementsClass.getName()));
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
    public static IProperty<?> forType(PropertyType attributeType, String name, Object value)
            throws IllegalArgumentException {
        if ((name == null) || (attributeType == null)) {
            throw new IllegalArgumentException("An attribute cannot have a null name");
        }
        if (value == null) {
            return forTypeWithNullValue(attributeType, name);
        }

        switch (attributeType) {
            case BOOLEAN:
                return buildBoolean(name, toBooleanValue(value));
            case DATE_ARRAY:
                return buildDateArray(name, toArrayValue(value, IProperty::toDateValue, OffsetDateTime.class));
            case DATE_INTERVAL:
                return buildDateInterval(name, (Range<OffsetDateTime>) value);
            case DATE_ISO8601:
                return buildDate(name, toDateValue(value));
            case DOUBLE:
                return buildDouble(name, toDoubleValue(value));
            case DOUBLE_ARRAY:
                return buildDoubleArray(name, toArrayValue(value, IProperty::toDoubleValue, Double.class));
            case DOUBLE_INTERVAL:
                return buildDoubleInterval(name, (Range<Double>) value);
            case INTEGER:
                return buildInteger(name, toIntegerValue(value));
            case INTEGER_ARRAY:
                return buildIntegerArray(name, toArrayValue(value, IProperty::toIntegerValue, Integer.class));
            case INTEGER_INTERVAL:
                return buildIntegerInterval(name, (Range<Integer>) value);
            case LONG:
                return buildLong(name, toLongValue(value));
            case LONG_ARRAY:
                return buildLongArray(name, toArrayValue(value, IProperty::toLongValue, Long.class));
            case LONG_INTERVAL:
                return buildLongInterval(name, (Range<Long>) value);
            case STRING:
                return buildString(name, toStringValue(value));
            case STRING_ARRAY:
                return buildStringArray(name, toArrayValue(value, IProperty::toStringValue, String.class));
            case URL:
                return buildUrl(name, toURLValue(value));
            default:
                throw new IllegalArgumentException(attributeType + " is not a handled value of "
                        + PropertyType.class.getName() + " in " + IProperty.class.getName());
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
    public static <U, T extends IProperty<U>> T forType(PropertyType attributeType, String name, U lowerBound,
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
                        + PropertyType.class.getName() + " in " + IProperty.class.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public static IProperty<?> updatePropertyValue(IProperty<?> property, Object value)
            throws IllegalArgumentException {
        if (value == null) {
            property.updateValue(null);
        }

        switch (property.getType()) {
            case BOOLEAN:
                ((BooleanProperty) property).updateValue(toBooleanValue(value));
                break;
            case DATE_ARRAY:
                ((DateArrayProperty) property)
                        .updateValue(toArrayValue(value, IProperty::toDateValue, OffsetDateTime.class));
                break;
            case DATE_INTERVAL:
                ((DateIntervalProperty) property).updateValue((Range<OffsetDateTime>) value);
                break;
            case DATE_ISO8601:
                ((DateProperty) property).updateValue(toDateValue(value));
                break;
            case DOUBLE:
                ((DoubleProperty) property).updateValue(toDoubleValue(value));
                break;
            case DOUBLE_ARRAY:
                ((DoubleArrayProperty) property)
                        .updateValue(toArrayValue(value, IProperty::toDoubleValue, Double.class));
                break;
            case DOUBLE_INTERVAL:
                ((DoubleIntervalProperty) property).updateValue((Range<Double>) value);
                break;
            case INTEGER:
                ((IntegerProperty) property).updateValue(toIntegerValue(value));
                break;
            case INTEGER_ARRAY:
                ((IntegerArrayProperty) property)
                        .updateValue(toArrayValue(value, IProperty::toIntegerValue, Integer.class));
                break;
            case INTEGER_INTERVAL:
                ((IntegerIntervalProperty) property).updateValue((Range<Integer>) value);
                break;
            case LONG:
                ((LongProperty) property).updateValue(toLongValue(value));
                break;
            case LONG_ARRAY:
                ((LongArrayProperty) property).updateValue(toArrayValue(value, IProperty::toLongValue, Long.class));
                break;
            case LONG_INTERVAL:
                ((LongIntervalProperty) property).updateValue((Range<Long>) value);
                break;
            case STRING:
                ((StringProperty) property).updateValue(toStringValue(value));
                break;
            case STRING_ARRAY:
                ((StringArrayProperty) property)
                        .updateValue(toArrayValue(value, IProperty::toStringValue, String.class));
                break;
            case URL:
                ((UrlProperty) property).updateValue(toURLValue(value));
                break;
            default:
                throw new IllegalArgumentException(property.getType() + " is not a handled value of "
                        + PropertyType.class.getName() + " in " + IProperty.class.getName());
        }
        return property;
    }

    /**
     * Build a range considering null value for one of the bound
     *
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return a range representation
     */
    static <U extends Comparable<?>> Range<U> buildRange(U lowerBound, U upperBound) {
        if (lowerBound == null) {
            return Range.atMost(upperBound);
        } else if (upperBound == null) {
            return Range.atLeast(lowerBound);
        } else {
            return Range.closed(lowerBound, upperBound);
        }
    }

    @SuppressWarnings("unchecked")
    public static <U, T extends IProperty<?>> T forTypeWithNullValue(PropertyType attributeType, String name) {
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
                return (T) buildLongArray(name);
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
                        + PropertyType.class.getName() + " in " + IProperty.class.getName());
        }
    }

    static LongIntervalProperty buildLongInterval(String name, Range<Long> value) {
        LongIntervalProperty att = new LongIntervalProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    static IntegerIntervalProperty buildIntegerInterval(String name, Range<Integer> value) {
        IntegerIntervalProperty att = new IntegerIntervalProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    static DoubleIntervalProperty buildDoubleInterval(String name, Range<Double> value) {
        DoubleIntervalProperty att = new DoubleIntervalProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    static DateIntervalProperty buildDateInterval(String name, Range<OffsetDateTime> value) {
        DateIntervalProperty att = new DateIntervalProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static UrlProperty buildUrl(String name, MarkdownURL value) {
        UrlProperty att = new UrlProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static UrlProperty buildUrl(String name) {
        UrlProperty att = new UrlProperty();
        att.setName(name);
        att.setValue(null);
        return att;
    }

    public static UrlProperty buildUrl(String name, String value) {
        UrlProperty att = new UrlProperty();
        att.setName(name);
        try {
            att.setValue(MarkdownURL.build(value));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(name + " is not a handled value of " + URL.class.getName());
        }
        return att;
    }

    public static BooleanProperty buildBoolean(String name, Boolean value) {
        BooleanProperty att = new BooleanProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static DateArrayProperty buildDateArray(String name, OffsetDateTime... offsetDateTimes) {
        DateArrayProperty att = new DateArrayProperty();
        att.setName(name);
        att.setValue(offsetDateTimes);
        return att;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static DateArrayProperty buildDateCollection(String name, Collection offsetDateTimes) {
        DateArrayProperty att = new DateArrayProperty();
        att.setName(name);
        if (offsetDateTimes instanceof HashSet<?>) {
            att.setValue(((Set<OffsetDateTime>) offsetDateTimes).stream().toArray(OffsetDateTime[]::new));
        } else if (offsetDateTimes instanceof ArrayList<?>) {
            att.setValue(((ArrayList<OffsetDateTime>) offsetDateTimes).stream().toArray(OffsetDateTime[]::new));
        }
        return att;
    }

    public static DateProperty buildDate(String name, OffsetDateTime offsetDateTime) {
        DateProperty att = new DateProperty();
        att.setName(name);
        att.setValue(offsetDateTime);
        return att;
    }

    public static DateIntervalProperty buildDateInterval(String name, OffsetDateTime lowerBoundDate,
            OffsetDateTime upperBoundDate) {
        DateIntervalProperty att = new DateIntervalProperty();
        att.setName(name);
        att.setValue(Range.closed(lowerBoundDate, upperBoundDate));
        return att;
    }

    public static DoubleArrayProperty buildDoubleArray(String pName, Double... pValues) {
        DoubleArrayProperty att = new DoubleArrayProperty();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static DoubleArrayProperty buildDoubleCollection(String name, Collection values) {
        DoubleArrayProperty att = new DoubleArrayProperty();
        att.setName(name);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Double>) values).stream().toArray(Double[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Double>) values).stream().toArray(Double[]::new));
        }
        return att;
    }

    public static DoubleProperty buildDouble(String name, Double value) {
        DoubleProperty att = new DoubleProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static DoubleIntervalProperty buildDoubleInterval(String name, Double lowerBoundDouble,
            Double upperBoundDouble) {
        DoubleIntervalProperty att = new DoubleIntervalProperty();
        att.setName(name);
        att.setValue(Range.closed(lowerBoundDouble, upperBoundDouble));
        return att;
    }

    public static IntegerArrayProperty buildIntegerArray(String name, Integer... values) {
        IntegerArrayProperty att = new IntegerArrayProperty();
        att.setName(name);
        att.setValue(values);
        return att;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static IntegerArrayProperty buildIntegerCollection(String name, Collection values) {
        IntegerArrayProperty att = new IntegerArrayProperty();
        att.setName(name);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Integer>) values).stream().toArray(Integer[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Integer>) values).stream().toArray(Integer[]::new));
        }
        return att;
    }

    public static IntegerProperty buildInteger(String name, Integer value) {
        IntegerProperty att = new IntegerProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static IntegerIntervalProperty buildIntegerInterval(String name, Integer lowerBoundInteger,
            Integer upperBoundInteger) {
        IntegerIntervalProperty att = new IntegerIntervalProperty();
        att.setName(name);
        att.setValue(Range.closed(lowerBoundInteger, upperBoundInteger));
        return att;
    }

    public static LongArrayProperty buildLongArray(String name, Long... values) {
        LongArrayProperty att = new LongArrayProperty();
        att.setName(name);
        att.setValue(values);
        return att;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static LongArrayProperty buildLongCollection(String name, Collection values) {
        LongArrayProperty att = new LongArrayProperty();
        att.setName(name);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Long>) values).stream().toArray(Long[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Long>) values).stream().toArray(Long[]::new));
        }
        return att;
    }

    public static LongProperty buildLong(String name, Long value) {
        LongProperty att = new LongProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    public static LongIntervalProperty buildLongInterval(String name, Long lowerBoundLong, Long upperBoundLong) {
        LongIntervalProperty att = new LongIntervalProperty();
        att.setName(name);
        att.setValue(Range.closed(lowerBoundLong, upperBoundLong));
        return att;
    }

    public static ObjectProperty buildObject(String name, IProperty<?>... properties) {
        ObjectProperty att = new ObjectProperty();
        att.setName(name);
        att.setValue(Sets.newHashSet(properties));
        return att;
    }

    public static StringArrayProperty buildStringArray(String name, String... values) {
        StringArrayProperty att = new StringArrayProperty();
        att.setName(name);
        att.setValue(values);
        return att;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static StringArrayProperty buildStringCollection(String name, Collection values) {
        StringArrayProperty att = new StringArrayProperty();
        att.setName(name);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<String>) values).stream().toArray(String[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<String>) values).stream().toArray(String[]::new));
        }
        return att;
    }

    public static StringProperty buildString(String name, String value) {
        StringProperty att = new StringProperty();
        att.setName(name);
        att.setValue(value);
        return att;
    }

    static Set<IProperty<?>> set(IProperty<?>... properties) {
        Set<IProperty<?>> set = new HashSet<>();
        if (properties != null) {
            for (IProperty<?> ppty : properties) {
                set.add(ppty);
            }
        }
        return set;
    }

    /**
     * Build a fast access map for current properties
     */
    public static Map<String, IProperty<?>> getPropertyMap(Set<IProperty<?>> properties) {
        Map<String, IProperty<?>> pmap = new HashMap<>();
        Map<String, ObjectProperty> omap = new HashMap<>();
        getPropertyMap(pmap, omap, properties);
        return pmap;
    }

    /**
     * Build a fast access map for properties and objects
     */
    public static void getPropertyMap(Map<String, IProperty<?>> pmap, Map<String, ObjectProperty> omap,
            Set<IProperty<?>> properties) {
        if (properties != null) {
            for (IProperty<?> ppt : properties) {
                addPropertyToMap(pmap, omap, ppt, null);
            }
        }
    }

    public static void addPropertyToMap(Map<String, IProperty<?>> pmap, Map<String, ObjectProperty> omap,
            IProperty<?> ppt, String namespace) {
        if (ppt.represents(PropertyType.OBJECT)) {
            omap.put(ppt.getName(), (ObjectProperty) ppt);
            for (IProperty<?> inner : ((ObjectProperty) ppt).getValue()) {
                addPropertyToMap(pmap, omap, inner, ppt.getName());
            }
        } else {
            StringBuilder builder = new StringBuilder();
            if ((namespace != null) && !namespace.isEmpty()) {
                builder.append(namespace);
                builder.append(DOT);
            }
            pmap.put(builder.append(ppt.getName()).toString(), ppt);
        }
    }

    public static Optional<String> getPropertyNamespace(String propertyKey) {
        if ((propertyKey != null) && propertyKey.contains(DOT)) {
            return Optional.of(propertyKey.substring(0, propertyKey.indexOf(DOT)));
        }
        return Optional.empty();
    }

    /**
     * Merge patch properties into reference ones
     * @param reference not <code>null</code> reference properties
     * @param patch not <code>null</code> patch properties
     * @param urn not <code>null</code>
     */
    public static void mergeProperties(Set<IProperty<?>> reference, Set<IProperty<?>> patch, String urn) {

        Assert.notNull(reference, "Reference properties must not be null");
        Assert.notNull(patch, "Patch properties must not be null");

        // Build fast property access maps
        Map<String, IProperty<?>> refMap = new HashMap<>();
        Map<String, ObjectProperty> refObjectMap = new HashMap<>();
        IProperty.getPropertyMap(refMap, refObjectMap, reference);

        // Build fast property for patch feature
        Map<String, IProperty<?>> patchMap = new HashMap<>();
        Map<String, ObjectProperty> patchObjectMap = new HashMap<>();
        IProperty.getPropertyMap(patchMap, patchObjectMap, patch);

        // Loop on patch properties
        for (Entry<String, IProperty<?>> entry : patchMap.entrySet()) {
            IProperty<?> property = entry.getValue();

            if (property.getValue() == null) {
                if (refMap.containsKey(entry.getKey())) {
                    // Unset property if exists
                    refMap.get(entry.getKey()).updateValue(null);
                }
            } else {
                if (refMap.containsKey(entry.getKey())) {
                    PropertyPatchLogger.LOGGER.trace("Update - \"{}\" - \"{}\" - \"{}\" -> \"{}\"", urn, entry.getKey(),
                                                     refMap.get(entry.getKey()).getValue(), property.getValue());
                    // Update property if already exists
                    IProperty.updatePropertyValue(refMap.get(entry.getKey()), property.getValue());
                } else {
                    // Add property
                    Optional<String> namespace = IProperty.getPropertyNamespace(entry.getKey());
                    if (namespace.isPresent()) {
                        if (refObjectMap.containsKey(namespace.get())) {
                            refObjectMap.get(namespace.get()).addProperty(property);
                        } else {
                            // Create object
                            ObjectProperty o = IProperty.buildObject(namespace.get(), property);
                            // Add it to the feature and to the reference map
                            reference.add(o);
                            refObjectMap.put(o.getName(), o);
                        }
                    } else {
                        reference.add(property);
                    }
                }
            }
        }
    }

    static final String DOT = ".";
}
