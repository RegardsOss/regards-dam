package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * Boolean specialized AbstractMatchCriterion.<br/>
 * <b>Only MatchType.EQUALS is allowed with Boolean type
 * @author oroussel
 */
public class BooleanMatchCriterion extends AbstractMatchCriterion<Boolean> {

    public BooleanMatchCriterion(String pName, boolean pValue) {
        super(pName, MatchType.EQUALS, pValue);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitBooleanMatchCriterion(this);
    }

}
