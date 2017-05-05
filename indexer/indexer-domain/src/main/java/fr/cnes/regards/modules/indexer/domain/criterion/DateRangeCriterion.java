package fr.cnes.regards.modules.indexer.domain.criterion;

import java.time.OffsetDateTime;

/**
 * OffsetDateTime RangeCriterion specialization
 */
public class DateRangeCriterion extends RangeCriterion<OffsetDateTime> implements ICriterion {

    protected DateRangeCriterion(String pName) {
        super(pName);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitDateRangeCriterion(this);
    }

}
