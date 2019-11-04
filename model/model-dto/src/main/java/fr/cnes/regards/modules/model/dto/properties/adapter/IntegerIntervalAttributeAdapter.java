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
package fr.cnes.regards.modules.model.dto.properties.adapter;

import java.io.IOException;

import com.google.common.collect.Range;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.model.dto.properties.AbstractProperty;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.IntegerIntervalProperty;

/**
 * AbstractIntervalAttributeTypeAdapter specialization to manage IntegerIntervalAttribute.<br/>
 * This adapter is taken into account by GSon if adapted class contains annotation @JsonAdapter.
 * @author oroussel
 */
public class IntegerIntervalAttributeAdapter
        extends AbstractIntervalAttributeTypeAdapter<Integer, IntegerIntervalProperty> {

    @Override
    protected void writeValueLowerBound(JsonWriter pOut, AbstractProperty<Range<Integer>> pValue) throws IOException {
        pOut.value(pValue.getValue().lowerEndpoint());
    }

    @Override
    protected void writeValueUpperBound(JsonWriter pOut, AbstractProperty<Range<Integer>> pValue) throws IOException {
        pOut.value(pValue.getValue().upperEndpoint());
    }

    @Override
    protected Range<Integer> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException {
        int lowerBound = 0;
        int upperBound = 0;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case IntervalMapping.RANGE_LOWER_BOUND:
                    lowerBound = pIn.nextInt();
                    break;
                case IntervalMapping.RANGE_UPPER_BOUND:
                    upperBound = pIn.nextInt();
                    break;
                default:
            }
        }
        return Range.closed(lowerBound, upperBound);
    }

    @Override
    protected IntegerIntervalProperty createRangeAttribute(String pName, Range<Integer> pRange) {
        return IProperty.buildIntegerInterval(pName, pRange.lowerEndpoint(), pRange.upperEndpoint());
    }

}
