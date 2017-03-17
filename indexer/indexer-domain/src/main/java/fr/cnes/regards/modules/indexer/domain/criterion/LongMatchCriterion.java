package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * Long specialized AbstractMatchCriterion.<br/>
 * <b>Only MatchType.EQUALS is allowed with Long type
 */
public class LongMatchCriterion extends AbstractMatchCriterion<Long> {

    public LongMatchCriterion(String pName, long pValue) {
        super(pName, MatchType.EQUALS, pValue);

    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitLongMatchCriterion(this);
    }

}
