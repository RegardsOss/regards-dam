package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * Long specialized AbstractMatchCriterion.<br/>
 * <b>Only MatchType.EQUALS is allowed with Long type
 * @author oroussel
 */
public class LongMatchCriterion extends AbstractMatchCriterion<Long> {

    public LongMatchCriterion(String name, long value) {
        super(name, MatchType.EQUALS, value);

    }

    @Override
    public LongMatchCriterion copy() {
        return new LongMatchCriterion(super.name, super.value);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitLongMatchCriterion(this);
    }

}
