package fr.cnes.regards.modules.crawler.domain.criterion;

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

}
