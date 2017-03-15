package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * Criterion that constraints nothing
 * @author oroussel
 */
public final class EmptyCriterion implements ICriterion {

    protected static final EmptyCriterion INSTANCE = new EmptyCriterion();

    private EmptyCriterion() {
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitEmptyCriterion(this);
    }

    @Override
    public boolean equals(Object pObj) {
        return (this == pObj);
    }
}
