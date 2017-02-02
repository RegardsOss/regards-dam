package fr.cnes.regards.modules.entities.domain.attribute.adapter;

import java.io.IOException;

import com.google.common.collect.Range;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.crawler.domain.IMapping;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;

/**
 * AbstractIntervalAttributeTypeAdapter specialization to manage IntegerIntervalAttribute.<br/>
 * This adapter is taken into account by GSon if adapted class contains annotation @JsonAdapter.
 * @author oroussel
 */
public class IntegerIntervalAttributeAdapter
        extends AbstractIntervalAttributeTypeAdapter<Integer, IntegerIntervalAttribute> {

    @Override
    protected void writeValueLowerBound(JsonWriter pOut, AbstractAttribute<Range<Integer>> pValue) throws IOException {
        pOut.value(pValue.getValue().lowerEndpoint());
    }

    @Override
    protected void writeValueUpperBound(JsonWriter pOut, AbstractAttribute<Range<Integer>> pValue) throws IOException {
        pOut.value(pValue.getValue().upperEndpoint());
    }

    @Override
    protected Range<Integer> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException {
        int lowerBound = 0;
        int upperBound = 0;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case IMapping.RANGE_LOWER_BOUND:
                    lowerBound = pIn.nextInt();
                    break;
                case IMapping.RANGE_UPPER_BOUND:
                    upperBound = pIn.nextInt();
                    break;
                default:
            }
        }
        return Range.closed(lowerBound, upperBound);
    }

    @Override
    protected IntegerIntervalAttribute createRangeAttribute(String pName, Range<Integer> pRange) {
        return AttributeBuilder.buildIntegerInterval(pName, pRange.lowerEndpoint(), pRange.upperEndpoint());
    }

}
