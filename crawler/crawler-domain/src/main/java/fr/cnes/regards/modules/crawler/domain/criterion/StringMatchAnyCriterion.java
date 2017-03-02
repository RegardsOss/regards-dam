package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * String[] specialized AbstractMatchCriterion ie a criterion to test if an attribute belongs to an array of
 * values.<br/>
 * <b>NB : This class is only used if none of provided string from given ones contains a blank character</b>
 */
public class StringMatchAnyCriterion extends AbstractMatchCriterion<String[]> {

    protected StringMatchAnyCriterion(String pName, String... pValue) {
        super(pName, MatchType.CONTAINS_ANY, pValue);
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> pVisitor) {
        return pVisitor.visitStringMatchAnyCriterion(this);
    }

}
