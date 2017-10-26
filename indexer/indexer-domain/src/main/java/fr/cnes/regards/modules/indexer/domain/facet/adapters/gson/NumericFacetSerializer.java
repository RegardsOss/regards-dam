/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

        private static final String OPENSEARCH_WILDCARD = "*";

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
            Range<Double> key = pEntry.getKey();
            if (key.hasLowerBound()) {
                lowerBound = pEntry.getKey().lowerEndpoint().toString();
            } else {
                // lowerBound = String.valueOf(Double.NEGATIVE_INFINITY);
                // Directly build openSearch lower bound
                lowerBound = OPENSEARCH_WILDCARD;
            }
            if (key.hasUpperBound()) {
                upperBound = pEntry.getKey().upperEndpoint().toString();
            } else {
                // upperBound = String.valueOf(Double.POSITIVE_INFINITY);
                // Directly build openSearch lower bound
                upperBound = OPENSEARCH_WILDCARD;
            }
            count = pEntry.getValue();
            if (lowerBound.equals(upperBound)) {
                openSearchQuery = pAttributeName + ":" + lowerBound;
            } else {
                openSearchQuery = pAttributeName + ":[" + lowerBound + " TO " + upperBound + "}";
            }
        }

    }

}
