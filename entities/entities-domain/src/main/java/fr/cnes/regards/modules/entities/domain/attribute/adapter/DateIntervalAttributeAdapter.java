package fr.cnes.regards.modules.entities.domain.attribute.adapter;

import java.io.IOException;
import java.time.LocalDateTime;

import com.google.common.collect.Range;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.framework.gson.adapters.LocalDateTimeAdapter;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;

/**
 * AbstractIntervalAttributeTypeAdapter specialization to manage DateIntervalAttribute.<br/>
 * This adapter is taken into account by GSon if adapted class contains annotation @JsonAdapter.
 * @author oroussel
 */
public class DateIntervalAttributeAdapter
        extends AbstractIntervalAttributeTypeAdapter<LocalDateTime, DateIntervalAttribute> {

    @Override
    protected void writeValueLowerBound(JsonWriter pOut, AbstractAttribute<Range<LocalDateTime>> pValue)
            throws IOException {
        pOut.value(LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET.format(pValue.getValue().lowerEndpoint()));
    }

    @Override
    protected void writeValueUpperBound(JsonWriter pOut, AbstractAttribute<Range<LocalDateTime>> pValue)
            throws IOException {
        pOut.value(LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET.format(pValue.getValue().upperEndpoint()));
    }

    @Override
    protected Range<LocalDateTime> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException {
        LocalDateTime lowerBound = null;
        LocalDateTime upperBound = null;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case "lowerBound":
                    lowerBound = LocalDateTime.parse(pIn.nextString(),
                                                     LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET);
                    break;
                case "upperBound":
                    upperBound = LocalDateTime.parse(pIn.nextString(),
                                                     LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET);
                    break;
                default:
            }
        }
        return Range.closed(lowerBound, upperBound);
    }

    @Override
    protected DateIntervalAttribute createRangeAttribute(String pName, Range<LocalDateTime> pRange) {
        return AttributeBuilder.buildDateInterval(pName, pRange.lowerEndpoint(), pRange.upperEndpoint());
    }

}
