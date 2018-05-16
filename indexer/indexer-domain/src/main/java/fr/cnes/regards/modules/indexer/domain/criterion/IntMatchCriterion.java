package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * Integer specialized AbstractMatchCriterion.<br/>
 * <b>Only MatchType.EQUALS is allowed with Integer type
 * @author oroussel
 */
public class IntMatchCriterion extends AbstractMatchCriterion<Integer> {

    public IntMatchCriterion(String name, int value) {
        super(name, MatchType.EQUALS, value);

    }

    @Override
    public IntMatchCriterion copy() {
        return new IntMatchCriterion(super.name, super.value);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitIntMatchCriterion(this);
    }

}
