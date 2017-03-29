/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Range;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.cnes.regards.modules.indexer.domain.facet.NumericFacet;

/**
 * Simplify the serialization of {@link NumericFacet#valueMap}.
 *
 * @author Xavier-Alexandre Brochard
 */
public class NumericFacetValuesSerializer implements JsonSerializer<Map<Range<Double>, Long>> {

    /*
     * (non-Javadoc)
     *
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type,
     * com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(Map<Range<Double>, Long> pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
        List<AdaptedNumericFacetValue> adapted = new ArrayList<>();
        pSrc.forEach((key, value) -> adapted.add(new AdaptedNumericFacetValue(key.lowerEndpoint().toString(),
                key.upperEndpoint().toString(), value)));

        return pContext.serialize(adapted);
    }

    /**
     * A POJO describing the adapted shape
     *
     * @author Xavier-Alexandre Brochard
     */
    private class AdaptedNumericFacetValue {

        private final String lowerBound;

        private final String upperBound;

        private final Long count;

        /**
         * @param pLowerBound
         * @param pUpperBound
         * @param pCount
         */
        public AdaptedNumericFacetValue(String pLowerBound, String pUpperBound, Long pCount) {
            super();
            lowerBound = pLowerBound;
            upperBound = pUpperBound;
            count = pCount;
        }

    }

}
