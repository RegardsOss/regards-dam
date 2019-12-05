/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import java.lang.reflect.Type;
import java.util.Arrays;
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

    /**
     * A POJO describing the adapted shape
     *
     * @author Xavier-Alexandre Brochard
     */
    static class AdaptedFacet {

        private final String attributeName;

        private final List<AdaptedFacetValue> values;

        private final long others;

        public AdaptedFacet(StringFacet facet) {
            super();
            attributeName = facet.getAttributeName();
            values = facet.getValues().entrySet().stream()
                    .map(entry -> new AdaptedFacetValue(entry.getKey(), entry.getValue(), facet.getAttributeName()))
                    .collect(Collectors.toList());
            others = facet.getOthers();
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

        public long getOthers() {
            return others;
        }

    }

    /**
     * A POJO describing the shape of a facet value
     *
     * @author Xavier-Alexandre Brochard
     */
    static class AdaptedFacetValue {

        private static final String[] CHARACTER_TO_ESCAPE = { "\\", "+", "-", "&&", "||", "!", "(", ")", "{", "}", "[",
                "]", "^", "\"", "~", "*", "?", ":", "/", " " };

        private final String word;

        private final Long count;

        private final String openSearchQuery;

        /**
         * @param word
         * @param count
         */
        public AdaptedFacetValue(String word, Long count, String attributeName) {
            super();
            this.word = word;
            this.count = count;
            boolean shouldAddQuote = Arrays.stream(CHARACTER_TO_ESCAPE).anyMatch(this.word::contains);
            if (shouldAddQuote) {
                openSearchQuery = attributeName + ":" + "\"" + this.word + "\"";
            } else {
                openSearchQuery = attributeName + ":" + this.word;
            }
        }

        /**
         * @return the openSearchQuery
         */
        public String getOpenSearchQuery() {
            return openSearchQuery;
        }

    }

    @Override
    public JsonElement serialize(StringFacet src, Type srcType, JsonSerializationContext context) {
        return context.serialize(new AdaptedFacet(src));
    }

}
