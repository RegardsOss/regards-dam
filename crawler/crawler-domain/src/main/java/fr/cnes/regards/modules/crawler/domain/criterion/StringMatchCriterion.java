package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * String specialized AbstractMatchCriterion
 */
public class StringMatchCriterion extends AbstractMatchCriterion<String> {

    public StringMatchCriterion(String pName, MatchType pType, String pValue) {
        super(pName, pType, pValue);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitStringMatchCriterion(this);
    }
}
