package fr.cnes.regards.modules.indexer.domain.facet;

import java.util.Map;

import com.google.common.collect.Range;

/**
 * Numeric facet. It represents a sorted map whose keys are double ranges (eventually opened for first and last ranges)
 * and values count of documents of which concerned values are within key range.
 * double is used even for int values
 * @author oroussel
 */
public class NumericFacet extends AbstractFacet<Map<Range<Double>, Long>> {

    /**
     * value map
     */
    private Map<Range<Double>, Long> valueMap;

    public NumericFacet(String pAttributeName, Map<Range<Double>, Long> pValueMap) {
        super(pAttributeName);
        this.valueMap = pValueMap;

    }

    @Override
    public FacetType getType() {
        return FacetType.DATE;
    }

    @Override
    public Map<Range<Double>, Long> getValues() {
        return this.valueMap;
    }
}
