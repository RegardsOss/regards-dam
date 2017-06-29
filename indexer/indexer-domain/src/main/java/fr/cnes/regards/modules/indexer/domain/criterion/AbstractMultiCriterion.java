package fr.cnes.regards.modules.indexer.domain.criterion;

import java.util.ArrayList;
import java.util.List;

/**
 * ICriterion aggregator
 * @author oroussel
 */
public abstract class AbstractMultiCriterion implements ICriterion {

    /**
     * Criterions
     */
    protected List<ICriterion> criterions = new ArrayList<>();

    protected AbstractMultiCriterion() {
        super();
    }

    public List<ICriterion> getCriterions() {
        return criterions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((criterions == null) ? 0 : criterions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractMultiCriterion other = (AbstractMultiCriterion) obj;
        if (criterions == null) {
            if (other.criterions != null) {
                return false;
            }
        } else if (!criterions.equals(other.criterions)) {
            return false;
        }
        return true;
    }

}