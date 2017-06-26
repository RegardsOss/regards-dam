/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.cnes.regards.modules.indexer.domain.facet.StringFacet;

/**
 * Simplify the serialization of {@link StringFacet#valueMap}.
 *
 * @author Xavier-Alexandre Brochard
 */
public class StringFacetSerializer implements JsonSerializer<StringFacet> {

    @Override
    public JsonElement serialize(StringFacet pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
        return pContext.serialize(new AdaptedFacet(pSrc));
    }

    /**
     * A POJO describing the adapted shape
     *
     * @author Xavier-Alexandre Brochard
     */
    class AdaptedFacet {

        private final String attributeName;

        private final List<AdaptedFacetValue> values;

        /**
         * @param pAttributeName
         * @param pValues
         */
        public AdaptedFacet(StringFacet pFacet) {
            super();
            attributeName = pFacet.getAttributeName();
            values = pFacet.getValues().entrySet().stream()
                    .map(entry -> new AdaptedFacetValue(entry.getKey(), entry.getValue(), pFacet.getAttributeName()))
                    .collect(Collectors.toList());
        }

        /**
         * @return the attributeName
         */
        public String getAttributeName() {
            return attributeName;
        }

        /**
         * @return the values
         */
        public List<AdaptedFacetValue> getValues() {
            return values;
        }

    }

    /**
     * A POJO describing the shape of a facet value
     *
     * @author Xavier-Alexandre Brochard
     */
    class AdaptedFacetValue {

        private final String word;

        private final Long count;

        private final String openSearchQuery;

        /**
         * @param pWord
         * @param pCount
         */
        public AdaptedFacetValue(String pWord, Long pCount, String pAttributeName) {
            super();
            word = pWord;
            count = pCount;
            if (pWord.contains(" ")) {
                openSearchQuery = pAttributeName + ":" + "\"" + pWord + "\"";
            } else {
                openSearchQuery = pAttributeName + ":" + pWord;
            }
        }

        /**
         * @return the openSearchQuery
         */
        public String getOpenSearchQuery() {
            return openSearchQuery;
        }

    }

}
