package fr.cnes.regards.modules.dam.domain.entities.attribute.adapter;

import java.io.IOException;

import com.google.common.collect.Range;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.LongIntervalAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.indexer.domain.IMapping;

/**
 * AbstractIntervalAttributeTypeAdapter specialization to manage LongIntervalAttribute.<br/>
 * This adapter is taken into account by GSon if adapted class contains annotation @JsonAdapter.
 * @author oroussel
 */
public class LongIntervalAttributeAdapter extends AbstractIntervalAttributeTypeAdapter<Long, LongIntervalAttribute> {

    @Override
    protected void writeValueLowerBound(JsonWriter pOut, AbstractAttribute<Range<Long>> pValue) throws IOException {
        pOut.value(pValue.getValue().lowerEndpoint());
    }

    @Override
    protected void writeValueUpperBound(JsonWriter pOut, AbstractAttribute<Range<Long>> pValue) throws IOException {
        pOut.value(pValue.getValue().upperEndpoint());
    }

    @Override
    protected Range<Long> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException {
        long lowerBound = 0;
        long upperBound = 0;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case IMapping.RANGE_LOWER_BOUND:
                    lowerBound = pIn.nextLong();
                    break;
                case IMapping.RANGE_UPPER_BOUND:
                    upperBound = pIn.nextLong();
                    break;
                default:
            }
        }
        return Range.closed(lowerBound, upperBound);
    }

    @Override
    protected LongIntervalAttribute createRangeAttribute(String pName, Range<Long> pRange) {
        return AttributeBuilder.buildLongInterval(pName, pRange.lowerEndpoint(), pRange.upperEndpoint());
    }

}
