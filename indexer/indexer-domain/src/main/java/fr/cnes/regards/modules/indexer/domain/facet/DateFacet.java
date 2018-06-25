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
package fr.cnes.regards.modules.indexer.domain.facet;

import java.time.OffsetDateTime;
import java.util.Map;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.indexer.domain.facet.adapters.gson.DateFacetSerializer;

/**
 * Date facet. It represents a sorted map whose keys are date ranges (eventually opened for first and last ranges) and
 * values count of documents of which dates are within key range.
 *
 * @author oroussel
 */
@JsonAdapter(value = DateFacetSerializer.class)
public class DateFacet extends AbstractFacet<Map<Range<OffsetDateTime>, Long>> {

    /**
     * value map
     */
    private final Map<Range<OffsetDateTime>, Long> valueMap;

    public DateFacet(String pAttributeName, Map<Range<OffsetDateTime>, Long> valueMap) {
        super(pAttributeName);
        this.valueMap = valueMap;

    }

    @Override
    public FacetType getType() {
        return FacetType.DATE;
    }

    @Override
    public Map<Range<OffsetDateTime>, Long> getValues() {
        return this.valueMap;
    }

}
