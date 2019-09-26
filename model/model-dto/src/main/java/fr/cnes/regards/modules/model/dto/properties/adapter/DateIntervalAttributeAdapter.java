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
import java.time.OffsetDateTime;

import com.google.common.collect.Range;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.model.dto.properties.AbstractProperty;
import fr.cnes.regards.modules.model.dto.properties.DateIntervalProperty;
import fr.cnes.regards.modules.model.dto.properties.IProperty;

/**
 * AbstractIntervalAttributeTypeAdapter specialization to manage DateIntervalAttribute.<br/>
 * This adapter is taken into account by GSon if adapted class contains annotation @JsonAdapter.
 * @author oroussel
 */
public class DateIntervalAttributeAdapter
        extends AbstractIntervalAttributeTypeAdapter<OffsetDateTime, DateIntervalProperty> {

    @Override
    protected void writeValueLowerBound(JsonWriter pOut, AbstractProperty<Range<OffsetDateTime>> pValue)
            throws IOException {
        pOut.value(OffsetDateTimeAdapter.format(pValue.getValue().lowerEndpoint()));
    }

    @Override
    protected void writeValueUpperBound(JsonWriter pOut, AbstractProperty<Range<OffsetDateTime>> pValue)
            throws IOException {
        pOut.value(OffsetDateTimeAdapter.format(pValue.getValue().upperEndpoint()));
    }

    @Override
    protected Range<OffsetDateTime> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException {
        OffsetDateTime lowerBound = null;
        OffsetDateTime upperBound = null;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case IntervalMapping.RANGE_LOWER_BOUND:
                    lowerBound = OffsetDateTimeAdapter.parse(pIn.nextString());
                    break;
                case IntervalMapping.RANGE_UPPER_BOUND:
                    upperBound = OffsetDateTimeAdapter.parse(pIn.nextString());
                    break;
                default:
            }
        }
        return Range.closed(lowerBound, upperBound);
    }

    @Override
    protected DateIntervalProperty createRangeAttribute(String pName, Range<OffsetDateTime> pRange) {
        return IProperty.buildDateInterval(pName, pRange.lowerEndpoint(), pRange.upperEndpoint());
    }

}
