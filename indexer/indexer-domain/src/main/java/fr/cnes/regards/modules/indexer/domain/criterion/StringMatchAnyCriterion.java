package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * String[] specialized AbstractMatchCriterion ie a criterion to test if an attribute belongs to an array of
 * values.<br/>
 * <b>NB : This class is only used if none of provided string from given ones contains a blank character</b>
 * @author oroussel
 */
public class StringMatchAnyCriterion extends AbstractMatchCriterion<String[]> {

    protected StringMatchAnyCriterion(String name, String... value) {
        super(name, MatchType.CONTAINS_ANY, value);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitStringMatchAnyCriterion(this);
    }

}
