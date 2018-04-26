package fr.cnes.regards.modules.indexer.dao.builder;

import java.util.Iterator;
import java.util.function.UnaryOperator;

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.min.MinAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentile;
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles;
import org.elasticsearch.search.aggregations.metrics.percentiles.PercentilesAggregationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.indexer.dao.EsHelper;
import fr.cnes.regards.modules.indexer.domain.facet.IFacetTypeVisitor;

/**
 * FacetType visitor implementation to generate AggregationBuilder from a search criterion with facets
 * @author oroussel
 */
@Component
public class AggregationBuilderFacetTypeVisitor implements IFacetTypeVisitor<AggregationBuilder> {


    public static final String STRING_FACET_SUFFIX = "_terms";

    public static final String DATE_FACET_SUFFIX = "_percents";

    public static final String NUMERIC_FACET_SUFFIX = "_percents";

    public static final String RANGE_FACET_SUFFIX = "_range";

    public static final String MIN_FACET_SUFFIX = "_min";

    public static final String MAX_FACET_SUFFIX = "_max";

    private final int stringFacetSize;

    private final int stringFacetMinDocCount;

    public AggregationBuilderFacetTypeVisitor(
            @Value("${regards.elasticsearch.string.facet.size:10}") int stringFacetSize,
            @Value("${regards.elasticsearch.string.facet.min.doc.count:1}") int stringFacetMinDocCount) {
        this.stringFacetSize = stringFacetSize;
        this.stringFacetMinDocCount = stringFacetMinDocCount;
    }

    @Override
    public AggregationBuilder visitStringFacet(Object... args) {
        String attributeName = (String) args[0]; // Development error if ClassCast or null array
        TermsAggregationBuilder termsAggBuilder = AggregationBuilders.terms(attributeName + STRING_FACET_SUFFIX);
        termsAggBuilder.field(attributeName + ".keyword");
        termsAggBuilder.size(stringFacetSize);
        termsAggBuilder.minDocCount(stringFacetMinDocCount);
        return termsAggBuilder;
    }

    @Override
    public AggregationBuilder visitDateFacet(Object... args) {
        String attributeName = (String) args[0]; // Development error if ClassCast or null array
        PercentilesAggregationBuilder percentsAggsBuilder = AggregationBuilders
                .percentiles(attributeName + DATE_FACET_SUFFIX);
        percentsAggsBuilder.field(attributeName);
        percentsAggsBuilder.percentiles(10., 20., 30., 40., 50., 60., 70., 80., 90.);
        return percentsAggsBuilder;
    }

    @Override
    public AggregationBuilder visitNumericFacet(Object... args) {
        String attributeName = (String) args[0]; // Development error if ClassCast or null array
        PercentilesAggregationBuilder percentsAggsBuilder = AggregationBuilders
                .percentiles(attributeName + NUMERIC_FACET_SUFFIX);
        percentsAggsBuilder.field(attributeName);
        percentsAggsBuilder.percentiles(10., 20., 30., 40., 50., 60., 70., 80., 90.);
        return percentsAggsBuilder;
    }

    /**
     * Double range almost equals date range except that as doubles are compared, all values are scaled with {@link EsHelper#PRECISION}.
     */
    @Override
    public AggregationBuilder visitRangeDoubleFacet(Object... args) {
        return visitRangeFacet(args, EsHelper::scaled);
    }

    /**
     * Date range almost equals double range except that as date is stored into double IT MUST NOT BE scaled !!!!
     */
    @Override
    public AggregationBuilder visitRangeDateFacet(Object... args) {
        return visitRangeFacet(args, UnaryOperator.identity());
    }

    private AggregationBuilder visitRangeFacet(Object[] args, UnaryOperator<Double> scalingFct) {
        String attributeName = (String) args[0]; // Development error if ClassCast or null array
        Percentiles percentiles = (Percentiles) args[1]; // Development error if ClassCast or null array
        RangeAggregationBuilder rangeAggBuilder = AggregationBuilders.range(attributeName + RANGE_FACET_SUFFIX);
        rangeAggBuilder.field(attributeName);
        Double previousValue = null;
        // INFO : ES API range creation use closedOpened ranges ([a , b[)
        for (Iterator<Percentile> i = percentiles.iterator(); i.hasNext(); ) {
            if (previousValue == null) { // first value
                previousValue = scalingFct.apply(i.next().getValue());
                // Armor Elasticsearch bullshits
                if (Double.isInfinite(previousValue)) {
                    // If first value is -Infinity, skip it
                    previousValue = null;
                } else {
                    rangeAggBuilder.addUnboundedTo(previousValue);
                }
            } else {
                double currentValue = scalingFct.apply(i.next().getValue());
                // Avoid creating [x, x[
                if (currentValue != previousValue) {
                    // Armor Elasticsearch bullshits
                    if (!Double.isInfinite(currentValue)) {
                        // if +Infinity appears in percentiles value, we can skip it, this case will be treated by last
                        // value
                        rangeAggBuilder.addRange(previousValue, currentValue);
                        previousValue = currentValue;
                    }
                }
            }
            if (!i.hasNext()) { // last value
                if (rangeAggBuilder.ranges().size() < 1) {
                    // No range => no facet
                    return null;
                }
                rangeAggBuilder.addUnboundedFrom(previousValue);
            }
        }
        return rangeAggBuilder;
    }

    @Override
    public AggregationBuilder visitMinFacet(Object... args) {
        String attributeName = (String) args[0]; // Development error if ClassCast or null array
        MinAggregationBuilder minAggBuilder = AggregationBuilders.min(attributeName + MIN_FACET_SUFFIX);
        minAggBuilder.field(attributeName);
        return minAggBuilder;
    }

    @Override
    public AggregationBuilder visitMaxFacet(Object... args) {
        String attributeName = (String) args[0]; // Development error if ClassCast or null array
        MaxAggregationBuilder maxAggBuilder = AggregationBuilders.max(attributeName + MAX_FACET_SUFFIX);
        maxAggBuilder.field(attributeName);
        return maxAggBuilder;
    }


}
