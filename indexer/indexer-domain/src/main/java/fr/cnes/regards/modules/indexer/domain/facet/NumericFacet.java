package fr.cnes.regards.modules.indexer.domain.facet;

import java.util.Map;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;
import fr.cnes.regards.modules.indexer.domain.facet.adapters.gson.NumericFacetSerializer;

/**
 * Numeric facet. It represents a sorted map whose keys are double ranges (eventually opened for first and last ranges)
 * and values count of documents of which concerned values are within key range. double is used even for int values
 *
 * @author oroussel
 */
@JsonAdapter(value = NumericFacetSerializer.class)
public class NumericFacet extends AbstractFacet<Map<Range<Double>, Long>> {

    /**
     * value map
     */
    private final Map<Range<Double>, Long> valueMap;

    public NumericFacet(String attName, Map<Range<Double>, Long> valueMap) {
        super(attName);
        this.valueMap = valueMap;
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
