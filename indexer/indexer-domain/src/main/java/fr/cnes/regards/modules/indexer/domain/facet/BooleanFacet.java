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
package fr.cnes.regards.modules.indexer.domain.facet;

import java.util.Map;

import com.google.gson.annotations.JsonAdapter;
import fr.cnes.regards.modules.indexer.domain.facet.adapters.gson.BooleanFacetSerializer;

/**
 * @author oroussel
 */
@JsonAdapter(value = BooleanFacetSerializer.class)
public class BooleanFacet extends AbstractFacet<Map<Boolean, Long>> {
    /**
     * Facet values. Key is String value, value is occurrence count of the key
     */
    private final Map<Boolean, Long> valueMap;

    public BooleanFacet(String attName, Map<Boolean, Long> valueMap, long others) {
        super(attName, others);
        this.valueMap = valueMap;
    }

    @Override
    public FacetType getType() {
        return FacetType.BOOLEAN;
    }

    @Override
    public Map<Boolean, Long> getValues() {
        return this.valueMap;
    }
}
