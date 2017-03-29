/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Range;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.cnes.regards.modules.indexer.domain.facet.DateFacet;

/**
 * Simplify the serialization of {@link DateFacet#valueMap}.
 *
 * @author Xavier-Alexandre Brochard
 */
public class DateFacetValuesSerializer implements JsonSerializer<Map<Range<LocalDateTime>, Long>> {

    /*
     * (non-Javadoc)
     *
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type,
     * com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(Map<Range<LocalDateTime>, Long> pSrc, Type pTypeOfSrc,
            JsonSerializationContext pContext) {
        List<AdaptedDateFacetValue> adapted = new ArrayList<>();
        pSrc.forEach((key, value) -> adapted
                .add(new AdaptedDateFacetValue(key.lowerEndpoint().toString(), key.upperEndpoint().toString(), value)));

        return pContext.serialize(adapted);
    }

    /**
     * A POJO describing the adapted shape
     *
     * @author Xavier-Alexandre Brochard
     */
    private class AdaptedDateFacetValue {

        private final String lowerBound;

        private final String upperBound;

        private final Long count;

        /**
         * @param pLowerBound
         * @param pUpperBound
         * @param pCount
         */
        public AdaptedDateFacetValue(String pLowerBound, String pUpperBound, Long pCount) {
            super();
            lowerBound = pLowerBound;
            upperBound = pUpperBound;
            count = pCount;
        }

    }

}
