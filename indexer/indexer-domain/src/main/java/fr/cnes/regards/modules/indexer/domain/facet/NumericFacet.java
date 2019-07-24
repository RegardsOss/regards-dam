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
package fr.cnes.regards.modules.indexer.domain.facet;

import java.util.Map;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.indexer.domain.facet.adapters.gson.NumericFacetSerializer;

/**
 * Numeric facet. It represents a sorted map whose keys are double ranges (eventually opened for first and last ranges)
 * and values count of documents of which concerned values are within key range. double is used even for int values
 *
 * @author oroussel
 */
@JsonAdapter(value = NumericFacetSerializer.class)
public class NumericFacet extends AbstractFacet<Map<Range<Double>, Long>> {

    /**
     * value map
     */
    private final Map<Range<Double>, Long> valueMap;

    public NumericFacet(String attName, Map<Range<Double>, Long> valueMap) {
        super(attName);
        this.valueMap = valueMap;
    }

    @Override
    public FacetType getType() {
        return FacetType.DATE;
    }

    @Override
    public Map<Range<Double>, Long> getValues() {
        return this.valueMap;
    }
}
