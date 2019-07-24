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
package fr.cnes.regards.modules.dam.domain.entities.attribute;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.dam.domain.entities.attribute.adapter.IntegerIntervalAttributeAdapter;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#INTEGER_INTERVAL} model attribute
 *
 * @author Marc Sordi
 *
 */
@JsonAdapter(IntegerIntervalAttributeAdapter.class)
public class IntegerIntervalAttribute extends AbstractAttribute<Range<Integer>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.INTEGER_INTERVAL.equals(pAttributeType);
    }
}
