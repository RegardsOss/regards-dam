/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import fr.cnes.regards.modules.indexer.domain.facet.BooleanFacet;

/**
 * @author oroussel
 */
public class BooleanFacetSerializer implements JsonSerializer<BooleanFacet> {

    @Override
    public JsonElement serialize(BooleanFacet src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(new AdaptedFacet(src));
    }

    /**
     * A POJO describing the adapted shape
     */
    static class AdaptedFacet {

        private final String attributeName;

        private final List<AdaptedFacetValue> values;

        private final long others;

        public AdaptedFacet(BooleanFacet facet) {
            super();
            attributeName = facet.getAttributeName();
            values = facet.getValues().entrySet().stream()
                    .map(entry -> new AdaptedFacetValue(entry.getKey(), entry.getValue(), facet.getAttributeName()))
                    .collect(Collectors.toList());
            others = facet.getOthers();
        }

        public String getAttributeName() {
            return attributeName;
        }

        public List<AdaptedFacetValue> getValues() {
            return values;
        }

        public long getOthers() {
            return others;
        }

    }

    /**
     * A POJO describing the shape of a facet value
     */
    static class AdaptedFacetValue {

        private final Boolean value;

        private final Long count;

        private final String openSearchQuery;

        public AdaptedFacetValue(Boolean value, Long count, String attributeName) {
            super();
            this.value = value;
            this.count = count;
            openSearchQuery = attributeName + ":" + value;
        }

        /**
         * @return the openSearchQuery
         */
        public String getOpenSearchQuery() {
            return openSearchQuery;
        }

    }

}
