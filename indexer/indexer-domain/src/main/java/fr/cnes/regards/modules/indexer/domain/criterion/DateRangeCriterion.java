package fr.cnes.regards.modules.indexer.domain.criterion;

import java.time.OffsetDateTime;

/**
 * OffsetDateTime RangeCriterion specialization
 */
public class DateRangeCriterion extends RangeCriterion<OffsetDateTime> implements ICriterion {

    protected DateRangeCriterion(String name) {
        super(name);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitDateRangeCriterion(this);
    }

}
