package fr.cnes.regards.modules.crawler.domain.criterion;

import com.google.common.collect.Lists;

/**
 * Defines a list of mandatory criterions (logicaly AND)
 * @author oroussel
 */
public class AndCriterion extends AbstractMultiCriterion implements ICriterion {

    protected AndCriterion() {
    }

    protected AndCriterion(ICriterion... pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    protected AndCriterion(Iterable<ICriterion> pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitAndCriterion(this);
    }
}
