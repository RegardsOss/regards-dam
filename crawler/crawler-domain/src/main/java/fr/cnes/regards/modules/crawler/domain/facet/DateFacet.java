package fr.cnes.regards.modules.crawler.domain.facet;

import java.time.LocalDateTime;
import java.util.Map;

import com.google.common.collect.Range;

/**
 * Date facet. It represents a sorted map whose keys are date ranges (eventually opened for first and last ranges)
 * and values count of documents of which dates are within key range.
 * @author oroussel
 */
public class DateFacet extends AbstractFacet<Map<Range<LocalDateTime>, Long>> {

    /**
     * value map
     */
    private Map<Range<LocalDateTime>, Long> valueMap;

    public DateFacet(String pAttributeName, Map<Range<LocalDateTime>, Long> pValueMap) {
        super(pAttributeName);
        this.valueMap = pValueMap;

    }

    @Override
    public FacetType getType() {
        return FacetType.DATE;
    }

    @Override
    public Map<Range<LocalDateTime>, Long> getValues() {
        return this.valueMap;
    }

}
