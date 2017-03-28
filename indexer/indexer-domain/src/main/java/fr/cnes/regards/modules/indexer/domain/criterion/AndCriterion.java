package fr.cnes.regards.modules.indexer.domain.criterion;

import com.google.common.collect.Lists;

/**
 * Defines a list of mandatory criterions (logicaly AND)
 * @author oroussel
 */
public final class AndCriterion extends AbstractMultiCriterion implements ICriterion {

    AndCriterion(ICriterion... pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    AndCriterion(Iterable<ICriterion> pCriterions) {
        this.criterions = Lists.newArrayList(pCriterions);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitAndCriterion(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + "AND".hashCode();
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
