package fr.cnes.regards.modules.crawler.domain.criterion;

import java.util.HashSet;
import java.util.Set;

/**
 * A range criterion defines a range of value comparisons for a named property.<br/>
 * For example : property "toto" between 0 and 1 (ie toto range = > 0 and < 1).<br/>
 * This class is also to be used for only one comparison.
 * @param <T> value type
 */
public class RangeCriterion<T extends Comparable<? super T>> extends AbstractPropertyCriterion implements ICriterion {

    /**
     * Set of comparisons (att > 0, att <= 25.34, etc...)
     */
    private Set<ValueComparison<T>> valueComparisons = new HashSet<>();

    protected RangeCriterion(String pName) {
        super(pName);
    }

    public void addValueComparison(ValueComparison<T> pValueComparison) {
        valueComparisons.add(pValueComparison);
    }

    public Set<ValueComparison<T>> getValueComparisons() {
        return valueComparisons;
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitRangeCriterion(this);
    }
}