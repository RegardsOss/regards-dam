package fr.cnes.regards.modules.indexer.domain.criterion;

import java.util.stream.Collectors;

import com.google.common.collect.Lists;

/**
 * Defines a list of optional criterions (logicaly OR)
 * @author oroussel
 */
public final class OrCriterion extends AbstractMultiCriterion implements ICriterion {

    OrCriterion(ICriterion... criteria) {
        this.criterions = Lists.newArrayList(criteria);
    }

    OrCriterion(Iterable<ICriterion> criteria) {
        this.criterions = Lists.newArrayList(criteria);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitOrCriterion(this);
    }

    @Override
    public OrCriterion copy() {
        return new OrCriterion(this.criterions.stream().map(ICriterion::copy).collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + "OR".hashCode();
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
