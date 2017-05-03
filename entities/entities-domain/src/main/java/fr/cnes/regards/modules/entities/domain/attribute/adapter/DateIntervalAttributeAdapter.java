package fr.cnes.regards.modules.entities.domain.attribute.adapter;

import java.io.IOException;
import java.time.OffsetDateTime;

import com.google.common.collect.Range;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.indexer.domain.IMapping;

/**
 * AbstractIntervalAttributeTypeAdapter specialization to manage DateIntervalAttribute.<br/>
 * This adapter is taken into account by GSon if adapted class contains annotation @JsonAdapter.
 * @author oroussel
 */
public class DateIntervalAttributeAdapter
        extends AbstractIntervalAttributeTypeAdapter<OffsetDateTime, DateIntervalAttribute> {

    @Override
    protected void writeValueLowerBound(JsonWriter pOut, AbstractAttribute<Range<OffsetDateTime>> pValue)
            throws IOException {
        pOut.value(OffsetDateTimeAdapter.format(pValue.getValue().lowerEndpoint()));
    }

    @Override
    protected void writeValueUpperBound(JsonWriter pOut, AbstractAttribute<Range<OffsetDateTime>> pValue)
            throws IOException {
        pOut.value(OffsetDateTimeAdapter.format(pValue.getValue().upperEndpoint()));
    }

    @Override
    protected Range<OffsetDateTime> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException {
        OffsetDateTime lowerBound = null;
        OffsetDateTime upperBound = null;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case IMapping.RANGE_LOWER_BOUND:
                    lowerBound = OffsetDateTimeAdapter.parse(pIn.nextString());
                    break;
                case IMapping.RANGE_UPPER_BOUND:
                    upperBound = OffsetDateTimeAdapter.parse(pIn.nextString());
                    break;
                default:
            }
        }
        return Range.closed(lowerBound, upperBound);
    }

    @Override
    protected DateIntervalAttribute createRangeAttribute(String pName, Range<OffsetDateTime> pRange) {
        return AttributeBuilder.buildDateInterval(pName, pRange.lowerEndpoint(), pRange.upperEndpoint());
    }

}
