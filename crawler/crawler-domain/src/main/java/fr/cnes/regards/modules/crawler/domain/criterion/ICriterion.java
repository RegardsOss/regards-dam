/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.crawler.domain.criterion;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import fr.cnes.regards.framework.gson.annotation.Gsonable;
import fr.cnes.regards.modules.crawler.domain.IMapping;

/**
 * Search criterion
 *
 * @author oroussel
 */
@Gsonable
public interface ICriterion {

    <U> U accept(ICriterionVisitor<U> pVisitor);

    static ICriterion all() {
        return EmptyCriterion.INSTANCE;
    }

    static ICriterion and(ICriterion... pCrits) {
        return new AndCriterion(pCrits);
    }

    static ICriterion and(Iterable<ICriterion> pCrits) {
        return new AndCriterion(pCrits);
    }

    static ICriterion or(ICriterion... pCrits) {
        return new OrCriterion(pCrits);
    }

    static ICriterion or(Iterable<ICriterion> pCrits) {
        return new OrCriterion(pCrits);
    }

    static ICriterion not(ICriterion pCrit) {
        return new NotCriterion(pCrit);
    }

    static <T extends Number & Comparable<T>> ICriterion gt(String pAttName, T pValue) {
        RangeCriterion<T> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER, pValue));
        return crit;
    }

    static <T extends Number & Comparable<T>> ICriterion ge(String pAttName, T pValue) {
        RangeCriterion<T> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pValue));
        return crit;
    }

    static <T extends Number & Comparable<T>> ICriterion lt(String pAttName, T pValue) {
        RangeCriterion<T> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS, pValue));
        return crit;
    }

    static <T extends Number & Comparable<T>> ICriterion le(String pAttName, T pValue) {
        RangeCriterion<T> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pValue));
        return crit;
    }

    static ICriterion gt(String pAttName, LocalDateTime pDate) {
        DateRangeCriterion crit = new DateRangeCriterion(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER, pDate));
        return crit;
    }

    static ICriterion ge(String pAttName, LocalDateTime pDate) {
        DateRangeCriterion crit = new DateRangeCriterion(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pDate));
        return crit;
    }

    static ICriterion lt(String pAttName, LocalDateTime pDate) {
        DateRangeCriterion crit = new DateRangeCriterion(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS, pDate));
        return crit;
    }

    static ICriterion le(String pAttName, LocalDateTime pDate) {
        DateRangeCriterion crit = new DateRangeCriterion(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pDate));
        return crit;
    }

    static ICriterion eq(String pAttName, int pValue) {
        return new IntMatchCriterion(pAttName, pValue);
    }

    static ICriterion eq(String pAttName, long pValue) {
        return new LongMatchCriterion(pAttName, pValue);
    }

    static ICriterion isTrue(String pAttName) {
        return ICriterion.eq(pAttName, true);
    }

    static ICriterion isFalse(String pAttName) {
        return ICriterion.eq(pAttName, false);
    }

    static ICriterion eq(String pAttName, boolean pValue) {
        return new BooleanMatchCriterion(pAttName, pValue);
    }

    static ICriterion in(String pAttName, int... pValues) {
        return new OrCriterion(IntStream.of(pValues).mapToObj(val -> new IntMatchCriterion(pAttName, val))
                .collect(Collectors.toList()));
    }

    static ICriterion in(String pAttName, long... pValues) {
        return new OrCriterion(LongStream.of(pValues).mapToObj(val -> new LongMatchCriterion(pAttName, val))
                .collect(Collectors.toList()));
    }

    static ICriterion eq(String pAttName, double pValue, double pPrecision) {
        RangeCriterion<Double> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pValue - pPrecision));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pValue + pPrecision));
        return crit;
    }

    static ICriterion ne(String pAttName, int pValue) {
        return new NotCriterion(ICriterion.eq(pAttName, pValue));
    }

    static ICriterion ne(String pAttName, long pValue) {
        return new NotCriterion(ICriterion.eq(pAttName, pValue));
    }

    static ICriterion ne(String pAttName, double pValue, double pPrecision) {
        return ICriterion.not(ICriterion.eq(pAttName, pValue, pPrecision));
    }

    /**
     * Criterion to test if a parameter is exactly the provided text or if a String array parameter contains an element
     * which is exactly the provided text
     * @param pAttName String or String array attribute
     * @param pText provided text
     * @return criterion
     */
    static ICriterion equals(String pAttName, String pText) {
        return new StringMatchCriterion(pAttName, MatchType.EQUALS, pText);
    }

    /**
     * Criterion to test if a parameter starts with the provided text or if a String array parameter contains an element
     * that starts with the provided text
     * @param pAttName String or String array attribute
     * @param pText provided text
     * @return criterion
     */
    static ICriterion startsWith(String pAttName, String pText) {
        return new StringMatchCriterion(pAttName, MatchType.STARTS_WITH, pText);
    }

    /**
     * Criterion to test if a parameter ends with the provided text or if a String array parameter contains an element
     * that ends with the provided text
     * @param pAttName String or String array attribute
     * @param pText provided text
     * @return criterion
     */
    static ICriterion endsWith(String pAttName, String pText) {
        return new StringMatchCriterion(pAttName, MatchType.ENDS_WITH, pText);
    }

    /**
     * Criterion to test if a parameter contain the provided text or if a String array parameter contains an element
     * that contains the provided text
     * @param pAttName String or String array attribute
     * @param pText provided text
     * @return criterion
     */
    static ICriterion contains(String pAttName, String pText) {
        return new StringMatchCriterion(pAttName, MatchType.CONTAINS, pText);
    }

    /**
     * Criterion to test if an array parameter contains specified value
     * @param pAttName attribute name
     * @param pValue value to search
     * @return criterion
     */
    static ICriterion contains(String pAttName, int pValue) {
        return ICriterion.eq(pAttName, pValue);
    }

    /**
     * Criterion to test if an array parameter contains specified value
     * @param pAttName attribute name
     * @param pValue value to search
     * @return criterion
     */
    static ICriterion contains(String pAttName, long pValue) {
        return ICriterion.eq(pAttName, pValue);
    }

    /**
     * Criterion to test if a double array parameter contains specified double value specifying precision
     * @param pAttName attribute name
     * @param pValue value to search
     * @param pPrecision wanted precision
     * @return criterion
     */
    static ICriterion contains(String pAttName, double pValue, double pPrecision) {
        return ICriterion.eq(pAttName, pValue, pPrecision);
    }

    /**
     * Criterion to test if a date array parameter contains a date between given lower and upper dates
     * @param pAttName attribute name
     * @param pLowerDate inclusive lower bound
     * @param pUpperDate inclusive upper bound
     * @return criterion
     */
    static ICriterion containsDateBetween(String pAttName, LocalDateTime pLowerDate, LocalDateTime pUpperDate) {
        return ICriterion.between(pAttName, pLowerDate, pUpperDate);
    }

    /**
     * Criterion to test if a string parameter has one of the provided values
     * @param pAttName attribute name
     * @param pTexts text array to test
     * @return criterion
     */
    static ICriterion in(String pAttName, String... pTexts) {
        // If one of the texts contains a blank character, StringMatchAnyCriterion cannot be used due to ES limitations
        if (Stream.of(pTexts).anyMatch(str -> str.contains(" "))) {
            return new OrCriterion(
                    Stream.of(pTexts).map(str -> ICriterion.equals(pAttName, str)).collect(Collectors.toList()));
        }
        return new StringMatchAnyCriterion(pAttName, pTexts);
    }

    /**
     * Criterion to test if an int parameter has a value into given range
     * @param pAttName attribute name
     * @param pLower inclusive lower bound
     * @param pUpper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(String pAttName, int pLower, int pUpper) {
        RangeCriterion<Integer> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pLower));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pUpper));
        return crit;
    }

    /**
     * Criterion to test if a long parameter has a value into given range
     * @param pAttName attribute name
     * @param pLower inclusive lower bound
     * @param pUpper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(String pAttName, long pLower, long pUpper) {
        RangeCriterion<Long> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pLower));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pUpper));
        return crit;
    }

    /**
     * Criterion to test if a date parameter is into given range period
     * @param pAttName attribute name
     * @param pLower inclusive lower bound
     * @param pUpper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(String pAttName, LocalDateTime pLower, LocalDateTime pUpper) {
        DateRangeCriterion crit = new DateRangeCriterion(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pLower));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pUpper));
        return crit;
    }

    /**
     * Criterion to test if a double parameter has a value into given range
     * @param pAttName attribute name
     * @param pLower inclusive lower bound
     * @param pUpper inclusive upper bound
     * @return criterion
     */
    static ICriterion between(String pAttName, double pLower, double pUpper) {
        RangeCriterion<Double> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pLower));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pUpper));
        return crit;
    }

    /**
     * Criterion to test if a numeric value (int or double) is into (inclusive) given interval attribute name
     * @param pAttName interval attribute name
     * @param pValue value to test inclusion
     * @return criterion
     */
    // CHECKSTYLE:OFF
    static <T extends Number & Comparable<T>> ICriterion into(String pAttName, T pValue) {
        return ICriterion.and(ICriterion.le(pAttName + "." + IMapping.RANGE_LOWER_BOUND, pValue),
                              ICriterion.ge(pAttName + "." + IMapping.RANGE_UPPER_BOUND, pValue));
    }

    /**
     * Criterion to test if given date range intersects given interval attribute name
     * @param pAttName interval attribute name
     * @param pLowerBound lower bound
     * @param pUpperBound upper bound
     * @return criterion
     */
    static ICriterion intersects(String pAttName, LocalDateTime pLowerBound, LocalDateTime pUpperBound) {
        return ICriterion.and(ICriterion.le(pAttName + "." + IMapping.RANGE_LOWER_BOUND, pUpperBound),
                              ICriterion.ge(pAttName + "." + IMapping.RANGE_UPPER_BOUND, pLowerBound));
    }
    // CHECKSTYLE:ON

}
