package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * Criterion that constraint nothing
 * @author oroussel
 */
public class EmptyCriterion implements ICriterion {

    protected EmptyCriterion() {
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitEmptyCriterion(this);
    }

}
