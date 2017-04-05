/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Range;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.cnes.regards.modules.indexer.domain.facet.NumericFacet;
import fr.cnes.regards.modules.indexer.domain.facet.StringFacet;

/**
 * Simplify the serialization of {@link StringFacet#valueMap}.
 *
 * @author Xavier-Alexandre Brochard
 */
public class NumericFacetSerializer implements JsonSerializer<NumericFacet> {

    @Override
    public JsonElement serialize(NumericFacet pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
        return pContext.serialize(new AdaptedFacet(pSrc));
    }

    /**
     * A POJO describing the adapted shape
     *
     * @author Xavier-Alexandre Brochard
     */
    private class AdaptedFacet {

        private final String attributeName;

        private final List<AdaptedFacetValue> values;

        /**
         * @param pAttributeName
         * @param pValues
         */
        public AdaptedFacet(NumericFacet pFacet) {
            super();
            attributeName = pFacet.getAttributeName();
            values = pFacet.getValues().entrySet().stream()
                    .map(entry -> new AdaptedFacetValue(entry, pFacet.getAttributeName())).collect(Collectors.toList());
        }

    }

    /**
     * A POJO describing the shape of a facet value
     *
     * @author Xavier-Alexandre Brochard
     */
    private class AdaptedFacetValue {

        private final String lowerBound;

        private final String upperBound;

        private final Long count;

        private final String openSearchQuery;

        /**
         * @param pLowerBound
         * @param pUpperBound
         * @param pCount
         */
        public AdaptedFacetValue(Entry<Range<Double>, Long> pEntry, String pAttributeName) {
            super();
            lowerBound = pEntry.getKey().lowerEndpoint().toString();
            upperBound = pEntry.getKey().upperEndpoint().toString();
            count = pEntry.getValue();
            openSearchQuery = pAttributeName + ":[" + lowerBound + " TO " + upperBound + "]";
        }

    }

}
