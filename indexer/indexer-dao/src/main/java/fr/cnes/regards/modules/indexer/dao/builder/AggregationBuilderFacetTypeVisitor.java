package fr.cnes.regards.modules.indexer.dao.builder;

import java.util.Iterator;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentile;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.indexer.domain.facet.IFacetTypeVisitor;

/**
 * FacetType visitor implementation to generate AggregationBuilder from a search criterion with facets
 * @author oroussel
 */
@Component
public class AggregationBuilderFacetTypeVisitor implements IFacetTypeVisitor<AggregationBuilder> {

    public static final String STRING_FACET_POSTFIX = "_terms";

    public static final String DATE_FACET_POSTFIX = "_percents";

    public static final String NUMERIC_FACET_POSTFIX = "_percents";

    public static final String RANGE_FACET_POSTFIX = "_range";

    private int stringFacetSize;

    private int stringFacetMinDocCount;

    public AggregationBuilderFacetTypeVisitor(@Value("${regards.elasticsearch.string.facet.size:10}") int stringFacetSize,
            @Value("${regards.elasticsearch.string.facet.min.doc.count:1}") int stringFacetMinDocCount) {
        this.stringFacetSize = stringFacetSize;
        this.stringFacetMinDocCount = stringFacetMinDocCount;
    }

    @Override
    public AggregationBuilder visitStringFacet(Object... pArgs) {
        String pAttributeName = (String) pArgs[0]; // Development error if ClassCast or null array
        TermsAggregationBuilder termsAggBuilder = AggregationBuilders.terms(pAttributeName + STRING_FACET_POSTFIX);
        termsAggBuilder.field(pAttributeName + ".keyword");
        termsAggBuilder.size(stringFacetSize);
        termsAggBuilder.minDocCount(stringFacetMinDocCount);
        return termsAggBuilder;
    }

    @Override
    public AggregationBuilder visitDateFacet(Object... pArgs) {
        String pAttributeName = (String) pArgs[0]; // Development error if ClassCast or null array
        PercentilesAggregationBuilder percentsAggsBuilder = AggregationBuilders
                .percentiles(pAttributeName + DATE_FACET_POSTFIX);
        percentsAggsBuilder.field(pAttributeName);
        percentsAggsBuilder.percentiles(10., 20., 30., 40., 50., 60., 70., 80., 90.);
        return percentsAggsBuilder;
    }

    @Override
    public AggregationBuilder visitNumericFacet(Object... pArgs) {
        String pAttributeName = (String) pArgs[0]; // Development error if ClassCast or null array
        PercentilesAggregationBuilder percentsAggsBuilder = AggregationBuilders
                .percentiles(pAttributeName + NUMERIC_FACET_POSTFIX);
        percentsAggsBuilder.field(pAttributeName);
        percentsAggsBuilder.percentiles(10., 20., 30., 40., 50., 60., 70., 80., 90.);
        return percentsAggsBuilder;
    }

    @Override
    public AggregationBuilder visitRangeFacet(Object... pArgs) {
        String pAttributeName = (String) pArgs[0]; // Development error if ClassCast or null array
        Percentiles percentiles = (Percentiles) pArgs[1]; // Development error if ClassCast or null array
        RangeAggregationBuilder rangeAggBuilder = AggregationBuilders.range(pAttributeName + RANGE_FACET_POSTFIX);
        rangeAggBuilder.field(pAttributeName);
        Double previousValue = null;
        for (Iterator<Percentile> i = percentiles.iterator(); i.hasNext();) {
            if (previousValue == null) { // first value
                previousValue = i.next().getValue();
                rangeAggBuilder.addUnboundedTo(previousValue);
            } else if (!i.hasNext()) { // last value
                rangeAggBuilder.addUnboundedFrom(i.next().getValue());
            } else {
                double currentValue = i.next().getValue();
                rangeAggBuilder.addRange(previousValue, currentValue);
                previousValue = currentValue;
            }
        }
        return rangeAggBuilder;
    }

}
