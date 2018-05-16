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

    public NotCriterion(ICriterion criterion) {
        this.criterion = criterion;
    }

    public ICriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(ICriterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public NotCriterion copy() {
        return new NotCriterion(this.criterion.copy());
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitNotCriterion(this);
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
