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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + "OR".hashCode();
        return result;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        return super.equals(pObj);
    }
}
