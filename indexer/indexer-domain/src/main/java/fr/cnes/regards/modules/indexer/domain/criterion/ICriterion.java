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
package fr.cnes.regards.modules.indexer.domain.criterion;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;

import fr.cnes.regards.framework.gson.annotation.Gsonable;
import fr.cnes.regards.modules.indexer.domain.criterion.exception.InvalidGeometryException;

/**
 * Search criterion
 * @author oroussel
 */
@Gsonable
public interface ICriterion {

    /**
     * @return a copy of the criterion
     */
    ICriterion copy();

    <U> U accept(ICriterionVisitor<U> visitor);

    static ICriterion all() {
        return EmptyCriterion.INSTANCE;
    }

    static ICriterion and(ICriterion... criteria) {
        return new AndCriterion(criteria);
    }

    static ICriterion and(Iterable<ICriterion> criteria) {
        return new AndCriterion(criteria);
    }

    static ICriterion or(ICriterion... criteria) {
        return new OrCriterion(criteria);
    }

    static ICriterion or(Iterable<ICriterion> criteria) {
        return new OrCriterion(criteria);
    }

    static ICriterion not(ICriterion criterion) {
        return new NotCriterion(criterion);
    }

    static <T extends Number & Comparable<T>> ICriterion gt(String attName, T value) {
        RangeCriterion<T> crit = new RangeCriterion<>(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER, value));
        return crit;
    }

    static <T extends Number & Comparable<T>> ICriterion ge(String attName, T value) {
        RangeCriterion<T> crit = new RangeCriterion<>(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, value));
        return crit;
    }

    static <T extends Number & Comparable<T>> ICriterion lt(String attName, T value) {
        RangeCriterion<T> crit = new RangeCriterion<>(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS, value));
        return crit;
    }

    static <T extends Number & Comparable<T>> ICriterion le(String attName, T value) {
        RangeCriterion<T> crit = new RangeCriterion<>(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, value));
        return crit;
    }

    static ICriterion gt(String attName, OffsetDateTime date) {
        DateRangeCriterion crit = new DateRangeCriterion(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER, date));
        return crit;
    }

    static ICriterion ge(String attName, OffsetDateTime date) {
        DateRangeCriterion crit = new DateRangeCriterion(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, date));
        return crit;
    }

    static ICriterion lt(String attName, OffsetDateTime date) {
        DateRangeCriterion crit = new DateRangeCriterion(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS, date));
        return crit;
    }

    static ICriterion le(String attName, OffsetDateTime date) {
        DateRangeCriterion crit = new DateRangeCriterion(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, date));
        return crit;
    }

    static ICriterion eq(String attName, int value) {
        return new IntMatchCriterion(attName, value);
    }

    static ICriterion eq(String attName, long value) {
        return new LongMatchCriterion(attName, value);
    }

    static ICriterion isTrue(String attName) {
        return ICriterion.eq(attName, true);
    }

    static ICriterion isFalse(String attName) {
        return ICriterion.eq(attName, false);
    }

    static ICriterion eq(String attName, boolean value) {
        return new BooleanMatchCriterion(attName, value);
    }

    static ICriterion in(String attName, int... values) {
        if (values.length == 0) {
            return new NotCriterion(all());
        }
        return new OrCriterion(
                IntStream.of(values).mapToObj(val -> new IntMatchCriterion(attName, val)).collect(Collectors.toList()));
    }

    static ICriterion in(String attName, long... values) {
        if (values.length == 0) {
            return new NotCriterion(all());
        }
        return new OrCriterion(LongStream.of(values).mapToObj(val -> new LongMatchCriterion(attName, val))
                .collect(Collectors.toList()));
    }

    static ICriterion eq(String attName, double value, double precision) {
        RangeCriterion<Double> crit = new RangeCriterion<>(attName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, value - precision));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, value + precision));
        return crit;
    }

    static ICriterion ne(String attName, int value) {
        return new NotCriterion(ICriterion.eq(attName, value));
    }

    static ICriterion ne(String attName, long value) {
        return new NotCriterion(ICriterion.eq(attName, value));
    }

    static ICriterion ne(String attName, double value, double precision) {
        return ICriterion.not(ICriterion.eq(attName, value, precision));
    }

    /**
     * Criterion to test if a parameter is exactly the provided text or if a String array parameter contains an element
     * which is exactly the provided text
     * @param attName String or String array attribute
     * @param text provided text
     * @return criterion
     */
    static ICriterion eq(String attName, String text) {
        return new StringMatchCriterion(attName, MatchType.EQUALS, text);
    }

    /**
     * Criterion to test if a parameter is exactly the provided date
     * @param attName Date attribute
     * @param date provided text
     * @return criterion
     */
    static ICriterion eq(String attName, OffsetDateTime date) {
        return new DateMatchCriterion(attName, date);
    }

    /**
     * Criterion to test if a parameter starts with the provided text or if a String array parameter contains an element
     * that starts with the provided text
     * @param attName String or String array attribute
     * @param text provided text
     * @return criterion
     */
    static ICriterion startsWith(String attName, String text) {
        return new StringMatchCriterion(attName, MatchType.STARTS_WITH, text);
    }

    /**
     * Criterion to test if a parameter ends with the provided text or if a String array parameter contains an element
     * that ends with the provided text
     * @param attName String or String array attribute
     * @param text provided text
     * @return criterion
     */
    static ICriterion endsWith(String attName, String text) {
        return new StringMatchCriterion(attName, MatchType.ENDS_WITH, text);
    }

    /**
     * Criterion to test if a parameter contain the provided text or if a String array parameter contains an element
     * that contains the provided text
     * @param attName String or String array attribute
     * @param text provided text
     * @return criterion
     */
    static ICriterion contains(String attName, String text) {
        return new StringMatchCriterion(attName, MatchType.CONTAINS, text);
    }

    /**
     * Criterion to test if a parameter follows given regular expression or if a String array parameter contains an
     * element which follows given regular expression
     * @param attName String or String array attribute
     * @param regexp provided regular expression
     * @return criterion
     */
    static ICriterion regexp(String attName, String regexp) {
        return new StringMatchCriterion(attName, MatchType.REGEXP, regexp);
    }

    /**
     * Criterion to test if an array parameter contains specified value
     * @param attName attribute name
     * @param value value to search
     * @return criterion
     */
    static ICriterion contains(String attName, int value) {
        return ICriterion.eq(attName, value);
    }

    /**
     * Criterion to test if an array parameter contains specified value
     * @param attName attribute name
     * @param value value to search
     * @return criterion
     */
    static ICriterion contains(String attName, long value) {
        return ICriterion.eq(attName, value);
    }

    /**
     * Criterion to test if a double array parameter contains specified double value specifying precision
     * @param attName attribute name
     * @param value value to search
     * @param precision wanted precision
     * @return criterion
     */
    static ICriterion contains(String attName, double value, double precision) {
        return ICriterion.eq(attName, value, precision);
    }

    /**
     * Criterion to test if a date array parameter contains a date between given lower and upper dates
     * @param attName attribute name
     * @param lowerDate inclusive lower bound
     * @param upperDate inclusive upper bound
     * @return criterion
     */
    static ICriterion containsDateBetween(String attName, OffsetDateTime lowerDate, OffsetDateTime upperDate) {
        return ICriterion.between(attName, lowerDate, upperDate);
    }

    /**
     * Criterion to test if a string parameter has one of the provided values
     * @param attName attribute name
     * @param texts text array to test
     * @return criterion
     */
    static ICriterion in(String attName, String... texts) {
        if (texts.length == 0) {
            return new NotCriterion(all());
        }
        // If one of the texts contains a blank character, StringMatchAnyCriterion cannot be used due to ES limitations
        if (Stream.of(texts).anyMatch(str -> str.contains(" "))) {
            return new OrCriterion(
                    Stream.of(texts).map(str -> ICriterion.eq(attName, str)).collect(Collectors.toList()));
        }
        return new StringMatchAnyCriterion(attName, texts);
    }

    /**
     * Criterion to test if an int parameter has a value into given range
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(String attName, int lower, int upper) {
        return between(attName, lower, true, upper, true);
    }

    /**
     * Criterion to test if a long parameter has a value into given range
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(String attName, long lower, long upper) {
        return between(attName, lower, true, upper, true);
    }

    /**
     * Criterion to test if a date parameter is into given range period
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(String attName, OffsetDateTime lower, OffsetDateTime upper) {
        return between(attName, lower, true, upper, true);
    }

    /**
     * Criterion to test if a double parameter has a value into given range
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(String attName, double lower, double upper) {
        return between(attName, lower, true, upper, true);
    }

    /**
     * Criterion to test if an int parameter has a value into given range
     * @param attName attribute name
     * @param lower lower bound
     * @param lowerInclusive inclusive lower bound or not
     * @param upper upper bound
     * @param upperInclusive inclusive upper bound or not
     * @return criterion
     */
    static ICriterion between(String attName, int lower, boolean lowerInclusive, int upper, boolean upperInclusive) {
        RangeCriterion<Integer> crit = new RangeCriterion<>(attName);
        crit.addValueComparison(new ValueComparison<>(
                lowerInclusive ? ComparisonOperator.GREATER_OR_EQUAL : ComparisonOperator.GREATER, lower));
        crit.addValueComparison(new ValueComparison<>(
                upperInclusive ? ComparisonOperator.LESS_OR_EQUAL : ComparisonOperator.LESS, upper));
        return crit;
    }

    /**
     * Criterion to test if a long parameter has a value into given range
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param lowerInclusive inclusive lower bound or not
     * @param upper inclusive upper bound
     * @param upperInclusive inclusive upper bound or not
     * @return criterion
     */
    static ICriterion between(String attName, long lower, boolean lowerInclusive, long upper, boolean upperInclusive) {
        RangeCriterion<Long> crit = new RangeCriterion<>(attName);
        crit.addValueComparison(new ValueComparison<>(
                lowerInclusive ? ComparisonOperator.GREATER_OR_EQUAL : ComparisonOperator.GREATER, lower));
        crit.addValueComparison(new ValueComparison<>(
                upperInclusive ? ComparisonOperator.LESS_OR_EQUAL : ComparisonOperator.LESS, upper));
        return crit;
    }

    /**
     * Criterion to test if a date parameter is into given range period
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param lowerInclusive inclusive lower bound or not
     * @param upper inclusive upper bound
     * @param upperInclusive inclusive upper bound or not
     * @return criterion
     */
    static ICriterion between(String attName, OffsetDateTime lower, boolean lowerInclusive, OffsetDateTime upper,
            boolean upperInclusive) {
        DateRangeCriterion crit = new DateRangeCriterion(attName);
        crit.addValueComparison(new ValueComparison<>(
                lowerInclusive ? ComparisonOperator.GREATER_OR_EQUAL : ComparisonOperator.GREATER, lower));
        crit.addValueComparison(new ValueComparison<>(
                upperInclusive ? ComparisonOperator.LESS_OR_EQUAL : ComparisonOperator.LESS, upper));
        return crit;
    }

    /**
     * Criterion to test if a double parameter has a value into given range
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param lowerInclusive inclusive lower bound or not
     * @param upper inclusive upper bound
     * @param upperInclusive inclusive upper bound or not
     * @return criterion
     */
    static ICriterion between(String attName, double lower, boolean lowerInclusive, double upper,
            boolean upperInclusive) {
        RangeCriterion<Double> crit = new RangeCriterion<>(attName);
        crit.addValueComparison(new ValueComparison<>(
                lowerInclusive ? ComparisonOperator.GREATER_OR_EQUAL : ComparisonOperator.GREATER, lower));
        crit.addValueComparison(new ValueComparison<>(
                upperInclusive ? ComparisonOperator.LESS_OR_EQUAL : ComparisonOperator.LESS, upper));
        return crit;
    }

    /**
     * Criterion to test if a numeric value (int or double) is into (inclusive) given interval attribute name
     * @param <T> extends {@link Number}
     * @param attName interval attribute name
     * @param value value to test inclusion
     * @return criterion
     */
    static <T extends Number & Comparable<T>> ICriterion into(String attName, T value) {
        return ICriterion.and(ICriterion.le(attName + "." + IMapping.RANGE_LOWER_BOUND, value),
                              ICriterion.ge(attName + "." + IMapping.RANGE_UPPER_BOUND, value));
    }

    /**
     * Criterion to test if given date range intersects given interval attribute name
     * @param attName interval attribute name
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return criterion
     */
    static ICriterion intersects(String attName, OffsetDateTime lowerBound, OffsetDateTime upperBound) {
        return ICriterion.and(ICriterion.le(attName + "." + IMapping.RANGE_LOWER_BOUND, upperBound),
                              ICriterion.ge(attName + "." + IMapping.RANGE_UPPER_BOUND, lowerBound));
    }

    /**
     * Criterion to test if given number range intersects given interval attribute name
     * @param <T> extends {@link Number}
     * @param attName interval attribute name
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return criterion
     */
    static <T extends Number & Comparable<T>> ICriterion intersects(String attName, T lowerBound, T upperBound) {
        return ICriterion.and(ICriterion.le(attName + "." + IMapping.RANGE_LOWER_BOUND, upperBound),
                              ICriterion.ge(attName + "." + IMapping.RANGE_UPPER_BOUND, lowerBound));
    }

    /**
     * Criterion to test the intersection with a circle giving center coordinates and radius.
     * @param center coordinates of center
     * @param radius radius eventually with unit (ie "100m" or "5km"), default to meters
     * @return criterion
     */
    static ICriterion intersectsCircle(double[] center, String radius) {
        return new CircleCriterion(center, radius);
    }

    /**
     * Criterion to test the intersection with a polygon
     * @param coordinates coordinates of polygon
     * @return criterion
     */
    static ICriterion intersectsPolygon(double[][][] coordinates) {
        return new PolygonCriterion(coordinates);
    }

    /**
     * Criterion to test the intersaction with a boundary box
     * @param bbox String bbox as "left,bottom,right,top" (or "minX, minY, maxX, maxY" or "minLon, minLat, maxLon,
     * maxLat"), blanks are accepted
     * @return {@link ICriterion}
     * @throws InvalidGeometryException
     */
    static ICriterion intersectsBbox(String bbox) throws InvalidGeometryException {
        return new BoundaryBoxCriterion(bbox);
    }

    /**
     * Criterion to test the intersaction with a boundary box
     * @param left
     * @param bottom
     * @param right
     * @param top
     * @return  {@link ICriterion}
     */
    static ICriterion intersectsBbox(double left, double bottom, double right, double top) {
        return new BoundaryBoxCriterion(left, bottom, right, top);
    }

    /**
     * Criterion to test if given attribute exists
     * @param attName attribute name
     * @return criterion
     */
    static ICriterion attributeExists(String attName) {
        return new FieldExistsCriterion(attName);
    }

    /**
     * Criterion to test if at least one of the parameters contains the provided text
     * @param attNames list of String
     * @param text provided text
     * @return criterion
     */
    static ICriterion multiMatch(Set<String> attNames, String text) {
        return new StringMultiMatchCriterion(attNames, MultiMatchQueryBuilder.Type.BEST_FIELDS, text);
    }

    /**
     * Criterion to test if at least one of the parameters starts with the provided text
     * @param attNames list of String
     * @param text provided text
     * @return criterion
     */
    static ICriterion multiMatchStartWith(Set<String> attNames, String text) {
        return new StringMultiMatchCriterion(attNames, MultiMatchQueryBuilder.Type.PHRASE_PREFIX, text);
    }
}
