package fr.cnes.regards.modules.indexer.domain.criterion;

import java.util.stream.Collectors;

import com.google.common.collect.Lists;

/**
 * Defines a list of mandatory criterions (logicaly AND)
 * @author oroussel
 */
public final class AndCriterion extends AbstractMultiCriterion implements ICriterion {

    AndCriterion(ICriterion... criteria) {
        this.criterions = Lists.newArrayList(criteria);
    }

    AndCriterion(Iterable<ICriterion> criteria) {
        this.criterions = Lists.newArrayList(criteria);
    }

    @Override
    public AndCriterion copy() {
        return new AndCriterion(this.criterions.stream().map(ICriterion::copy).collect(Collectors.toList()));
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitAndCriterion(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + "AND".hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o);
    }
}
