package fr.cnes.regards.modules.crawler.domain.criterion;

import com.google.common.collect.Lists;

/**
 * Defines a list of optional criterions (logicaly OR)
 * @author oroussel
 */
public final class OrCriterion extends AbstractMultiCriterion implements ICriterion {

    OrCriterion(ICriterion... pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    OrCriterion(Iterable<ICriterion> pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitOrCriterion(this);
    }

}
