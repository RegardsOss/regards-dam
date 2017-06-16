/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.Range;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.cnes.regards.modules.indexer.domain.facet.DateFacet;
import fr.cnes.regards.modules.indexer.domain.facet.StringFacet;

/**
 * Simplify the serialization of {@link StringFacet#valueMap}.
 *
 * @author Xavier-Alexandre Brochard
 */
public class DateFacetSerializer implements JsonSerializer<DateFacet> {

    @Override
    public JsonElement serialize(DateFacet pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
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

        public AdaptedFacet(DateFacet pFacet) {
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

        private static final String OPENSEARCH_WILDCARD = "*";

        private final String lowerBound;

        private final String upperBound;

        private final Long count;

        private final String openSearchQuery;

        public AdaptedFacetValue(Entry<Range<OffsetDateTime>, Long> pEntry, String pAttributeName) {
            Range<OffsetDateTime> key = pEntry.getKey();
            if (key.hasLowerBound()) {
                lowerBound = pEntry.getKey().lowerEndpoint().toString();
            } else {
                // Directly build openSearch lower bound
                lowerBound = OPENSEARCH_WILDCARD;
            }
            if (key.hasUpperBound()) {
                upperBound = pEntry.getKey().upperEndpoint().toString();
            } else {
                // Directly build openSearch lower bound
                upperBound = OPENSEARCH_WILDCARD;
            }
            count = pEntry.getValue();
            openSearchQuery = pAttributeName + ":[" + lowerBound + " TO " + upperBound + "]";
        }

    }

}
