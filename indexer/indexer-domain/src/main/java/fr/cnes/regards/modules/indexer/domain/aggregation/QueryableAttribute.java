package fr.cnes.regards.modules.indexer.domain.aggregation;

import org.elasticsearch.search.aggregations.Aggregation;

public class QueryableAttribute {

    private String attributeName;

    private Aggregation aggregation;

    private boolean textAttribute = false;

    private int termsLimit = 0;

    public QueryableAttribute(String attributeName, Aggregation aggregation, boolean textAttribute, int termsLimit) {
        super();
        this.attributeName = attributeName;
        this.aggregation = aggregation;
        this.textAttribute = textAttribute;
        this.termsLimit = termsLimit;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    public boolean isTextAttribute() {
        return textAttribute;
    }

    public void setTextAttribute(boolean textAttribute) {
        this.textAttribute = textAttribute;
    }

    public int getTermsLimit() {
        return termsLimit;
    }

    public void setTermsLimit(int termsLimit) {
        this.termsLimit = termsLimit;
    }

}
