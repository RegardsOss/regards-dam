package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * Boolean specialized AbstractMatchCriterion.<br/>
 * <b>Only MatchType.EQUALS is allowed with Boolean type
 * @author oroussel
 */
public class BooleanMatchCriterion extends AbstractMatchCriterion<Boolean> {

    public BooleanMatchCriterion(String name, boolean value) {
        super(name, MatchType.EQUALS, value);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitBooleanMatchCriterion(this);
    }

}
