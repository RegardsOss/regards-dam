package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * String specialized AbstractMatchCriterion
 * @author oroussel
 */
public class StringMatchCriterion extends AbstractMatchCriterion<String> {

    protected StringMatchCriterion(String pName, MatchType pType, String pValue) {
        super(pName, pType, pValue);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitStringMatchCriterion(this);
    }
}
