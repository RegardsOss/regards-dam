package fr.cnes.regards.modules.indexer.domain.criterion;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A range criterion defines a range of value comparisons for a named property.<br/>
 * For example : property "toto" between 0 and 1 (ie toto range = > 0 and < 1).<br/>
 * This class is also to be used for only one comparison.
 * @param <T> value type
 * @author oroussel
 */
public class RangeCriterion<T extends Comparable<? super T>> extends AbstractPropertyCriterion implements ICriterion {

    /**
     * Set of comparisons (att > 0, att <= 25.34, etc...)
     */
    protected final Set<ValueComparison<T>> valueComparisons = new HashSet<>();

    protected RangeCriterion(String name) {
        super(name);
    }

    public void addValueComparison(ValueComparison<T> valueComparison) {
        valueComparisons.add(valueComparison);
    }

    public Set<ValueComparison<T>> getValueComparisons() {
        return valueComparisons;
    }

    @Override
    public RangeCriterion<T> copy() {
        RangeCriterion<T> copy = new RangeCriterion<>(super.name);
        copy.valueComparisons.addAll(this.valueComparisons.stream().map(ValueComparison::copy).collect(Collectors.toSet()));
        return copy;
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitRangeCriterion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        RangeCriterion<?> that = (RangeCriterion<?>) o;
        return Objects.equals(valueComparisons, that.valueComparisons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), valueComparisons);
    }
}
