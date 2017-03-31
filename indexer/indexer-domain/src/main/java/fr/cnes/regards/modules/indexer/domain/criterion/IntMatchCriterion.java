package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * Integer specialized AbstractMatchCriterion.<br/>
 * <b>Only MatchType.EQUALS is allowed with Integer type
 * @author oroussel
 */
public class IntMatchCriterion extends AbstractMatchCriterion<Integer> {

    public IntMatchCriterion(String pName, int pValue) {
        super(pName, MatchType.EQUALS, pValue);

    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitIntMatchCriterion(this);
    }

}
