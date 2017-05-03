package fr.cnes.regards.modules.indexer.domain.facet;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.indexer.domain.facet.adapters.gson.DateFacetSerializer;

/**
 * Date facet. It represents a sorted map whose keys are date ranges (eventually opened for first and last ranges) and
 * values count of documents of which dates are within key range.
 *
 * @author oroussel
 */
@JsonAdapter(value = DateFacetSerializer.class)
public class DateFacet extends AbstractFacet<Map<Range<OffsetDateTime>, Long>> {

    /**
     * value map
     */
    private final Map<Range<OffsetDateTime>, Long> valueMap;

    public DateFacet(String pAttributeName, Map<Range<OffsetDateTime>, Long> pValueMap) {
        super(pAttributeName);
        this.valueMap = pValueMap;

    }

    @Override
    public FacetType getType() {
        return FacetType.DATE;
    }

    @Override
    public Map<Range<OffsetDateTime>, Long> getValues() {
        return this.valueMap;
    }

}
