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
package fr.cnes.regards.modules.entities.domain.criterion;

import java.time.OffsetDateTime;

import fr.cnes.regards.modules.entities.domain.StaticProperties;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Search criterion builder from an {@link AttributeModel}.<br/>
 * This builder places the right data JSON path according to the related attribute model.
 *
 * @author Marc Sordi
 *
 */
public interface IFeatureCriterion extends ICriterion {

    static String buildFeaturePath(AttributeModel att) {
        return att.buildJsonPath(StaticProperties.FEATURE_PROPERTIES_PATH);
    }

    static <T extends Number & Comparable<T>> ICriterion gt(AttributeModel att, T value) {
        return ICriterion.gt(buildFeaturePath(att), value);
    }

    static <T extends Number & Comparable<T>> ICriterion ge(AttributeModel att, T value) {
        return ICriterion.ge(buildFeaturePath(att), value);
    }

    static <T extends Number & Comparable<T>> ICriterion lt(AttributeModel att, T value) {
        return ICriterion.lt(buildFeaturePath(att), value);
    }

    static <T extends Number & Comparable<T>> ICriterion le(AttributeModel att, T value) {
        return ICriterion.le(buildFeaturePath(att), value);
    }

    static ICriterion gt(AttributeModel att, OffsetDateTime date) {
        return ICriterion.gt(buildFeaturePath(att), date);
    }

    static ICriterion ge(AttributeModel att, OffsetDateTime date) {
        return ICriterion.ge(buildFeaturePath(att), date);
    }

    static ICriterion lt(AttributeModel att, OffsetDateTime date) {
        return ICriterion.lt(buildFeaturePath(att), date);
    }

    static ICriterion le(AttributeModel att, OffsetDateTime date) {
        return ICriterion.le(buildFeaturePath(att), date);
    }

    static ICriterion eq(AttributeModel att, int value) {
        return ICriterion.eq(buildFeaturePath(att), value);
    }

    static ICriterion eq(AttributeModel att, long value) {
        return ICriterion.eq(buildFeaturePath(att), value);
    }

    static ICriterion isTrue(AttributeModel att) {
        return ICriterion.isTrue(buildFeaturePath(att));
    }

    static ICriterion isFalse(AttributeModel att) {
        return ICriterion.isFalse(buildFeaturePath(att));
    }

    static ICriterion eq(AttributeModel att, boolean value) {
        return ICriterion.eq(buildFeaturePath(att), value);
    }

    static ICriterion in(AttributeModel att, int... values) {
        return ICriterion.in(buildFeaturePath(att), values);
    }

    static ICriterion in(AttributeModel att, long... values) {
        return ICriterion.in(buildFeaturePath(att), values);
    }

    static ICriterion eq(AttributeModel att, double value, double precision) {
        return ICriterion.eq(buildFeaturePath(att), value, precision);
    }

    static ICriterion ne(AttributeModel att, int value) {
        return ICriterion.ne(buildFeaturePath(att), value);
    }

    static ICriterion ne(AttributeModel att, long value) {
        return ICriterion.ne(buildFeaturePath(att), value);
    }

    static ICriterion ne(AttributeModel att, double value, double precision) {
        return ICriterion.ne(buildFeaturePath(att), value, precision);
    }

    /**
     * Criterion to test if a parameter is exactly the provided text or if a String array parameter contains an element
     * which is exactly the provided text
     * @param attName String or String array attribute
     * @param text provided text
     * @return criterion
     */
    static ICriterion eq(AttributeModel att, String text) {
        return ICriterion.eq(buildFeaturePath(att), text);
    }

    /**
     * Criterion to test if a parameter is exactly the provided date
     * @param attName Date attribute
     * @param date provided text
     * @return criterion
     */
    static ICriterion eq(AttributeModel att, OffsetDateTime date) {
        return ICriterion.eq(buildFeaturePath(att), date);
    }

    /**
     * Criterion to test if a parameter starts with the provided text or if a String array parameter contains an element
     * that starts with the provided text
     * @param attName String or String array attribute
     * @param text provided text
     * @return criterion
     */
    static ICriterion startsWith(AttributeModel att, String text) {
        return ICriterion.startsWith(buildFeaturePath(att), text);
    }

    /**
     * Criterion to test if a parameter ends with the provided text or if a String array parameter contains an element
     * that ends with the provided text
     * @param attName String or String array attribute
     * @param text provided text
     * @return criterion
     */
    static ICriterion endsWith(AttributeModel att, String text) {
        return ICriterion.endsWith(buildFeaturePath(att), text);
    }

    /**
     * Criterion to test if a parameter contain the provided text or if a String array parameter contains an element
     * that contains the provided text
     * @param attName String or String array attribute
     * @param text provided text
     * @return criterion
     */
    static ICriterion contains(AttributeModel att, String text) {
        return ICriterion.contains(buildFeaturePath(att), text);
    }

    /**
     * Criterion to test if a parameter follows given regular expression or if a String array parameter contains an
     * element which follows given regular expression
     * @param attName String or String array attribute
     * @param text provided regular expression
     * @return criterion
     */
    static ICriterion likes(AttributeModel att, String text) {
        return ICriterion.likes(buildFeaturePath(att), text);
    }

    /**
     * Criterion to test if an array parameter contains specified value
     * @param attName attribute name
     * @param value value to search
     * @return criterion
     */
    static ICriterion contains(AttributeModel att, int value) {
        return ICriterion.contains(buildFeaturePath(att), value);
    }

    /**
     * Criterion to test if an array parameter contains specified value
     * @param attName attribute name
     * @param value value to search
     * @return criterion
     */
    static ICriterion contains(AttributeModel att, long value) {
        return ICriterion.contains(buildFeaturePath(att), value);
    }

    /**
     * Criterion to test if a double array parameter contains specified double value specifying precision
     * @param attName attribute name
     * @param value value to search
     * @param precision wanted precision
     * @return criterion
     */
    static ICriterion contains(AttributeModel att, double value, double precision) {
        return ICriterion.contains(buildFeaturePath(att), value, precision);
    }

    /**
     * Criterion to test if a date array parameter contains a date between given lower and upper dates
     * @param attName attribute name
     * @param lowerDate inclusive lower bound
     * @param upperDate inclusive upper bound
     * @return criterion
     */
    static ICriterion containsDateBetween(AttributeModel att, OffsetDateTime lowerDate, OffsetDateTime upperDate) {
        return ICriterion.containsDateBetween(buildFeaturePath(att), lowerDate, upperDate);
    }

    /**
     * Criterion to test if a string parameter has one of the provided values
     * @param attName attribute name
     * @param texts text array to test
     * @return criterion
     */
    static ICriterion in(AttributeModel att, String... texts) {
        return ICriterion.in(buildFeaturePath(att), texts);
    }

    /**
     * Criterion to test if an int parameter has a value into given range
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(AttributeModel att, int lower, int upper) {
        return ICriterion.between(buildFeaturePath(att), lower, upper);
    }

    /**
     * Criterion to test if a long parameter has a value into given range
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(AttributeModel att, long lower, long upper) {
        return ICriterion.between(buildFeaturePath(att), lower, upper);
    }

    /**
     * Criterion to test if a date parameter is into given range period
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(AttributeModel att, OffsetDateTime lower, OffsetDateTime upper) {
        return ICriterion.between(buildFeaturePath(att), lower, upper);
    }

    /**
     * Criterion to test if a double parameter has a value into given range
     * @param attName attribute name
     * @param lower inclusive lower bound
     * @param upper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(AttributeModel att, double lower, double upper) {
        return ICriterion.between(buildFeaturePath(att), lower, upper);
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
    static ICriterion between(AttributeModel att, int lower, boolean lowerInclusive, int upper,
            boolean upperInclusive) {
        return ICriterion.between(buildFeaturePath(att), lower, lowerInclusive, upper, upperInclusive);
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
    static ICriterion between(AttributeModel att, long lower, boolean lowerInclusive, long upper,
            boolean upperInclusive) {
        return ICriterion.between(buildFeaturePath(att), lower, lowerInclusive, upper, upperInclusive);
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
    static ICriterion between(AttributeModel att, OffsetDateTime lower, boolean lowerInclusive, OffsetDateTime upper,
            boolean upperInclusive) {
        return ICriterion.between(buildFeaturePath(att), lower, lowerInclusive, upper, upperInclusive);
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
    static ICriterion between(AttributeModel att, double lower, boolean lowerInclusive, double upper,
            boolean upperInclusive) {
        return ICriterion.between(buildFeaturePath(att), lower, lowerInclusive, upper, upperInclusive);
    }

    /**
     * Criterion to test if a numeric value (int or double) is into (inclusive) given interval attribute name
     * @param attName interval attribute name
     * @param value value to test inclusion
     * @return criterion
     */
    static <T extends Number & Comparable<T>> ICriterion into(AttributeModel att, T value) {
        return ICriterion.into(buildFeaturePath(att), value);
    }

    /**
     * Criterion to test if given date range intersects given interval attribute name
     * @param attName interval attribute name
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return criterion
     */
    static ICriterion intersects(AttributeModel att, OffsetDateTime lowerBound, OffsetDateTime upperBound) {
        return ICriterion.intersects(buildFeaturePath(att), lowerBound, upperBound);
    }

    /**
     * Criterion to test if given number range intersects given interval attribute name
     * @param attName interval attribute name
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return criterion
     */
    static <T extends Number & Comparable<T>> ICriterion intersects(AttributeModel att, T lowerBound, T upperBound) {
        return ICriterion.intersects(buildFeaturePath(att), lowerBound, upperBound);
    }

    /**
     * Criterion to test if given attribute exists
     * @param attName attribute name
     * @return criterion
     */
    static ICriterion attributeExists(AttributeModel att) {
        return ICriterion.attributeExists(buildFeaturePath(att));
    }
}
