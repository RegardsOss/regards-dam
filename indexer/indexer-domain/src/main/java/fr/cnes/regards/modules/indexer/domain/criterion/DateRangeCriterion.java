package fr.cnes.regards.modules.indexer.domain.criterion;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * OffsetDateTime RangeCriterion specialization
 */
public class DateRangeCriterion extends RangeCriterion<OffsetDateTime> implements ICriterion {

    protected DateRangeCriterion(String name) {
        super(name);
    }

    @Override
    public DateRangeCriterion copy() {
        DateRangeCriterion copy = new DateRangeCriterion(super.name);
        copy.valueComparisons.addAll(super.valueComparisons.stream().map(ValueComparison::copy).collect(Collectors.toSet()));
        return copy;

    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitDateRangeCriterion(this);
    }

}
