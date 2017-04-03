/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import fr.cnes.regards.modules.indexer.domain.facet.StringFacet;

/**
 * Simplify the serialization of {@link StringFacet#valueMap}.
 *
 * @author Xavier-Alexandre Brochard
 */
public class StringFacetValuesSerializer implements JsonSerializer<Map<String, Long>> {

    /*
     * (non-Javadoc)
     *
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type,
     * com.google.gson.JsonSerializationContext)
     */
    @Override
    public JsonElement serialize(Map<String, Long> pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
        List<AdaptedNumericFacetValue> adapted = new ArrayList<>();
        pSrc.forEach((key, value) -> adapted.add(new AdaptedNumericFacetValue(key, value)));

        return pContext.serialize(adapted);
    }

    /**
     * A POJO describing the adapted shape
     *
     * @author Xavier-Alexandre Brochard
     */
    private class AdaptedNumericFacetValue {

        private final String word;

        private final Long count;

        /**
         * @param pWord
         * @param pCount
         */
        public AdaptedNumericFacetValue(String pWord, Long pCount) {
            super();
            word = pWord;
            count = pCount;
        }

    }

}
