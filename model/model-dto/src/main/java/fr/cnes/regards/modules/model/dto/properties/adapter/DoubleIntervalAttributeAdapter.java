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
import fr.cnes.regards.modules.model.dto.properties.DoubleIntervalProperty;
import fr.cnes.regards.modules.model.dto.properties.IProperty;

/**
 * AbstractIntervalAttributeTypeAdapter specialization to manage DoubleIntervalAttribute.<br/>
 * This adapter is taken into account by GSon if adapted class contains annotation @JsonAdapter.
 * @author oroussel
 */
public class DoubleIntervalAttributeAdapter
        extends AbstractIntervalAttributeTypeAdapter<Double, DoubleIntervalProperty> {

    @Override
    protected void writeValueLowerBound(JsonWriter pOut, AbstractProperty<Range<Double>> pValue) throws IOException {
        pOut.value(pValue.getValue().lowerEndpoint());
    }

    @Override
    protected void writeValueUpperBound(JsonWriter pOut, AbstractProperty<Range<Double>> pValue) throws IOException {
        pOut.value(pValue.getValue().upperEndpoint());
    }

    @Override
    protected Range<Double> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException {
        double lowerBound = 0.0;
        double upperBound = 0.0;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case IntervalMapping.RANGE_LOWER_BOUND:
                    lowerBound = pIn.nextDouble();
                    break;
                case IntervalMapping.RANGE_UPPER_BOUND:
                    upperBound = pIn.nextDouble();
                    break;
                default:
            }
        }
        return Range.closed(lowerBound, upperBound);
    }

    @Override
    protected DoubleIntervalProperty createRangeAttribute(String pName, Range<Double> pRange) {
        return IProperty.buildDoubleInterval(pName, pRange.lowerEndpoint(), pRange.upperEndpoint());
    }

}
