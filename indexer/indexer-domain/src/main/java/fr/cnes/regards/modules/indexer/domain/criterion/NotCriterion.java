package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * Defines a NOT criterion
 * @author oroussel
 */
public class NotCriterion implements ICriterion {

    /**
     * Criterion that not be true
     */
    private ICriterion criterion;

    public NotCriterion(ICriterion pCriterion) {
        criterion = pCriterion;
    }

    public ICriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(ICriterion pCriterion) {
        criterion = pCriterion;
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitNotCriterion(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((criterion == null) ? 0 : criterion.hashCode());
        result = (prime * result) + "NOT".hashCode();
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
        NotCriterion other = (NotCriterion) obj;
        if (criterion == null) {
            if (other.criterion != null) {
                return false;
            }
        } else if (!criterion.equals(other.criterion)) {
            return false;
        }
        return true;
    }

}
