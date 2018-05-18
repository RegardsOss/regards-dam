/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
    private static class AdaptedFacet {

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
    private static class AdaptedFacetValue {

        private static final String OPENSEARCH_WILDCARD = "*";

        private final String lowerBound;

        private final String upperBound;

        private final Long count;

        private final String openSearchQuery;

        /**
         * Be careful, the range must be closed-opened => [ date1 TO date2 }
         * @param entry
         * @param attributeName
         */
        public AdaptedFacetValue(Entry<Range<OffsetDateTime>, Long> entry, String attributeName) {
            Range<OffsetDateTime> key = entry.getKey();
            if (key.hasLowerBound()) {
                lowerBound = entry.getKey().lowerEndpoint().toString();
            } else {
                // Directly build openSearch lower bound
                lowerBound = OPENSEARCH_WILDCARD;
            }
            if (key.hasUpperBound()) {
                upperBound = entry.getKey().upperEndpoint().toString();
            } else {
                // Directly build openSearch lower bound
                upperBound = OPENSEARCH_WILDCARD;
            }
            count = entry.getValue();
            if (lowerBound.equals(upperBound)) {
                openSearchQuery = attributeName + ":\"" + lowerBound + "\"";
            } else {
                openSearchQuery = attributeName + ":[" + lowerBound + " TO " + upperBound + "}";
            }
        }

    }

}
