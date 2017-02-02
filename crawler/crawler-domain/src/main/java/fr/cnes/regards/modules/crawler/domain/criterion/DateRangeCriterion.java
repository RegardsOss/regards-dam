package fr.cnes.regards.modules.crawler.domain.criterion;

import java.time.LocalDateTime;

/**
 * LocalDateTime RangeCriterion specialization
 */
public class DateRangeCriterion extends RangeCriterion<LocalDateTime> implements ICriterion {

    protected DateRangeCriterion(String pName) {
        super(pName);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitDateRangeCriterion(this);
    }

}
