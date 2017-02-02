package fr.cnes.regards.modules.crawler.domain.criterion;

import com.google.common.collect.Lists;

/**
 * Defines a list of optional criterions (logicaly OR)
 * @author oroussel
 */
public class OrCriterion extends AbstractMultiCriterion implements ICriterion {

    protected OrCriterion() {
    }

    protected OrCriterion(ICriterion... pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    protected OrCriterion(Iterable<ICriterion> pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitOrCriterion(this);
    }

}
