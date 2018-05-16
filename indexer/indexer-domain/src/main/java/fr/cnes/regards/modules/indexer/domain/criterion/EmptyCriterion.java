package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * Criterion that constraints nothing
 * @author oroussel
 */
public final class EmptyCriterion implements ICriterion {

    protected static final EmptyCriterion INSTANCE = new EmptyCriterion();

    private EmptyCriterion() {
    }

    @Override
    public EmptyCriterion copy() {
        return INSTANCE;
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitEmptyCriterion(this);
    }

    @Override
    public boolean equals(Object o) {
        return (this == o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
