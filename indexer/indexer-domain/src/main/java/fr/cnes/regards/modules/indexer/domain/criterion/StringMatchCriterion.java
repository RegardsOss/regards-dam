package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * String specialized AbstractMatchCriterion
 * @author oroussel
 */
public class StringMatchCriterion extends AbstractMatchCriterion<String> {

    protected StringMatchCriterion(String name, MatchType type, String value) {
        super(name, type, value);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitStringMatchCriterion(this);
    }
}
