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
package fr.cnes.regards.modules.entities.domain.attribute.builder;

import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
     * type of pValue is expected to be coherent with the AttributeType. In particular, for intervals we are expecting
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
        if (name == null) {
            throw new IllegalArgumentException("An attribute cannot have a null name");
        }
        if (value == null) {
            return forTypeWithNullValue(attributeType, name);
        }

        try {
            switch (attributeType) {
                case INTEGER:
                    return (T) ((value instanceof Number) ? buildInteger(name, ((Number) value).intValue())
                            : buildInteger(name, new Integer((String) value)));
                case BOOLEAN:
                    return (T) ((value instanceof Boolean) ? buildBoolean(name, (Boolean) value)
                            : buildBoolean(name, Boolean.valueOf((String) value)));
                case DATE_ARRAY:
                    if (value instanceof Collection) {
                        return (T) buildStringCollection(name, (Collection) value);
                    } else if (value instanceof String[]) {
                        return (T) buildDateArray(name, Arrays.stream((String[]) value)
                                .map(v -> OffsetDateTimeAdapter.parse(v)).toArray(size -> new OffsetDateTime[size]));
                    } else {
                        return (T) buildDateArray(name, (OffsetDateTime[]) value);
                    }
                case DATE_INTERVAL:
                    return (T) buildDateInterval(name, (Range<OffsetDateTime>) value);
                case DATE_ISO8601:
                    if (value instanceof String) {
                        return (T) buildDate(name, OffsetDateTimeAdapter.parse((String) value));
                    }
                    return (T) buildDate(name, (OffsetDateTime) value);
                case DOUBLE:
                    return (T) ((value instanceof Number) ? buildDouble(name, ((Number) value).doubleValue())
                            : buildDouble(name, new Double((String) value)));
                case DOUBLE_ARRAY:
                    if (value instanceof Collection) {
                        return (T) buildDoubleCollection(name, (Collection) value);
                    } else {
                        return (T) buildDoubleArray(name,
                                                    Arrays.stream((Number[]) value).mapToDouble(n -> n.doubleValue())
                                                            .mapToObj(Double::new).toArray(size -> new Double[size]));
                    }
                case DOUBLE_INTERVAL:
                    return (T) buildDoubleInterval(name, (Range<Double>) value);
                case INTEGER_ARRAY:
                    if (value instanceof Collection) {
                        return (T) buildIntegerCollection(name, (Collection) value);
                    } else {
                        return (T) buildIntegerArray(name, Arrays.stream(((Number[]) value)).mapToInt(v -> v.intValue())
                                .mapToObj(Integer::new).toArray(size -> new Integer[size]));
                    }
                case INTEGER_INTERVAL:
                    return (T) buildIntegerInterval(name, (Range<Integer>) value);
                case LONG:
                    return (T) ((value instanceof Number) ? buildLong(name, ((Number) value).longValue())
                            : buildLong(name, new Long((String) value)));
                case LONG_ARRAY:
                    if (value instanceof Collection) {
                        return (T) buildLongCollection(name, (Collection) value);
                    } else {
                        return (T) buildLongArray(name, Arrays.stream(((Number[]) value)).mapToLong(v -> v.longValue())
                                .mapToObj(Long::new).toArray(size -> new Long[size]));
                    }
                case LONG_INTERVAL:
                    return (T) buildLongInterval(name, (Range<Long>) value);
                case STRING:
                    return (T) buildString(name, value.toString());
                case STRING_ARRAY:
                    if (value instanceof Collection) {
                        return (T) buildStringCollection(name, (Collection) value);
                    } else {
                        return (T) buildStringArray(name, (String[]) value);
                    }
                case URL:
                    return (T) buildUrl(name, (URL) value);
                default:
                    throw new IllegalArgumentException(attributeType + " is not a handled value of "
                            + AttributeType.class.getName() + " in " + AttributeBuilder.class.getName());
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    String.format("Error trying to build attribute %s of type %s with value %s. cause : %s", name,
                                  attributeType, value.toString(), e.getMessage()),
                    e);
        }
    }

    /**
     * Method allowing to get an AbstractAttribute for an <b>interval</b> AttributeType, for the given name and
     * values. The type of values is expected to be coherent with the AttributeType :
     * <ul>
     * <li>we are expecting an ISO 8601 string for dates</li>
     * <li>a number for double, integer and long</li>
     * </ul>
     * @param <U> type of the value
     * @param <T> type of the attribute generated
     * @param attributeType Type of the attribute created
     * @param name name of the attribute to be created
     * @param lowerBound value of the attribute to be created
     * @param upperBound value of the attribute to be created
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static DateArrayAttribute buildDateCollection(String pName, Collection offsetDateTimes) {
        DateArrayAttribute att = new DateArrayAttribute();
        att.setName(pName);
        if (offsetDateTimes instanceof HashSet<?>) {
            att.setValue(((Set<OffsetDateTime>) offsetDateTimes).stream().toArray(OffsetDateTime[]::new));
        } else if (offsetDateTimes instanceof ArrayList<?>) {
            att.setValue(((ArrayList<OffsetDateTime>) offsetDateTimes).stream().toArray(OffsetDateTime[]::new));
        }
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static DoubleArrayAttribute buildDoubleCollection(String pName, Collection values) {
        DoubleArrayAttribute att = new DoubleArrayAttribute();
        att.setName(pName);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Double>) values).stream().toArray(Double[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Double>) values).stream().toArray(Double[]::new));
        }
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static IntegerArrayAttribute buildIntegerCollection(String pName, Collection values) {
        IntegerArrayAttribute att = new IntegerArrayAttribute();
        att.setName(pName);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Integer>) values).stream().toArray(Integer[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Integer>) values).stream().toArray(Integer[]::new));
        }
        return att;
    }

    public static AbstractAttribute<?> buildInteger(String pName, Integer pValue) {
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static LongArrayAttribute buildLongCollection(String pName, Collection values) {
        LongArrayAttribute att = new LongArrayAttribute();
        att.setName(pName);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<Long>) values).stream().toArray(Long[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<Long>) values).stream().toArray(Long[]::new));
        }
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static StringArrayAttribute buildStringCollection(String pName, Collection values) {
        StringArrayAttribute att = new StringArrayAttribute();
        att.setName(pName);
        if (values instanceof HashSet<?>) {
            att.setValue(((Set<String>) values).stream().toArray(String[]::new));
        } else if (values instanceof ArrayList<?>) {
            att.setValue(((ArrayList<String>) values).stream().toArray(String[]::new));
        }
        return att;
    }

    public static StringAttribute buildString(String pName, String pValue) {
        StringAttribute att = new StringAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }
}
