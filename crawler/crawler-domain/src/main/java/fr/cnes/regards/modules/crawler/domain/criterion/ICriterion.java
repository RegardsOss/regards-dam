package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * Search criterion
 * @author oroussel
 */
public interface ICriterion {

    <U> U accept(ICriterionVisitor<U> pVisitor);

    static ICriterion and(ICriterion... pCrits) {
        return new AndCriterion(pCrits);
    }

    static ICriterion and(Iterable<ICriterion> pCrits) {
        return new AndCriterion(pCrits);
    }

    static ICriterion not(ICriterion pCrit) {
        return new NotCriterion(pCrit);
    }

    static ICriterion gt(String pAttName, Number pValue) {
        RangeCriterion<Number> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER, pValue));
        return crit;
    }

    static ICriterion ge(String pAttName, Number pValue) {
        RangeCriterion<Number> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pValue));
        return crit;
    }

    static ICriterion lt(String pAttName, Number pValue) {
        RangeCriterion<Number> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS, pValue));
        return crit;
    }

    static ICriterion le(String pAttName, Number pValue) {
        RangeCriterion<Number> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pValue));
        return crit;
    }

    static ICriterion eq(String pAttName, int pValue) {
        return new IntMatchCriterion(pAttName, pValue);
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

    static ICriterion ne(String pAttName, double pValue, double pPrecision) {
        RangeCriterion<Double> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pValue - pPrecision));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pValue + pPrecision));
        return crit;
    }

    static ICriterion equals(String pAttName, String pText) {
        return new StringMatchCriterion(pAttName, MatchType.EQUALS, pText);
    }

    static ICriterion startsWith(String pAttName, String pText) {
        return new StringMatchCriterion(pAttName, MatchType.STARTS_WITH, pText);
    }

    static ICriterion endsWith(String pAttName, String pText) {
        return new StringMatchCriterion(pAttName, MatchType.ENDS_WITH, pText);
    }

    static ICriterion contains(String pAttName, String pText) {
        return new StringMatchCriterion(pAttName, MatchType.CONTAINS, pText);
    }

    static ICriterion between(String pAttName, int pLower, int pUpper) {
        RangeCriterion<Integer> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pLower));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pUpper));
        return crit;
    }

    static ICriterion between(String pAttName, double pLower, double pUpper) {
        RangeCriterion<Double> crit = new RangeCriterion<>(pAttName);
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.GREATER_OR_EQUAL, pLower));
        crit.addValueComparison(new ValueComparison<>(ComparisonOperator.LESS_OR_EQUAL, pUpper));
        return crit;
    }

}
