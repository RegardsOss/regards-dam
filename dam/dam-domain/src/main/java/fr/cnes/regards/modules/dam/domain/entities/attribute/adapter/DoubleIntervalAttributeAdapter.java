package fr.cnes.regards.modules.dam.domain.entities.attribute.adapter;

import java.io.IOException;

import com.google.common.collect.Range;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.DoubleIntervalAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.indexer.domain.IMapping;

/**
 * AbstractIntervalAttributeTypeAdapter specialization to manage DoubleIntervalAttribute.<br/>
 * This adapter is taken into account by GSon if adapted class contains annotation @JsonAdapter.
 * @author oroussel
 */
public class DoubleIntervalAttributeAdapter
        extends AbstractIntervalAttributeTypeAdapter<Double, DoubleIntervalAttribute> {

    @Override
    protected void writeValueLowerBound(JsonWriter pOut, AbstractAttribute<Range<Double>> pValue) throws IOException {
        pOut.value(pValue.getValue().lowerEndpoint());
    }

    @Override
    protected void writeValueUpperBound(JsonWriter pOut, AbstractAttribute<Range<Double>> pValue) throws IOException {
        pOut.value(pValue.getValue().upperEndpoint());
    }

    @Override
    protected Range<Double> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException {
        double lowerBound = 0.0;
        double upperBound = 0.0;
        while (pIn.hasNext()) {
            switch (pIn.nextName()) {
                case IMapping.RANGE_LOWER_BOUND:
                    lowerBound = pIn.nextDouble();
                    break;
                case IMapping.RANGE_UPPER_BOUND:
                    upperBound = pIn.nextDouble();
                    break;
                default:
            }
        }
        return Range.closed(lowerBound, upperBound);
    }

    @Override
    protected DoubleIntervalAttribute createRangeAttribute(String pName, Range<Double> pRange) {
        return AttributeBuilder.buildDoubleInterval(pName, pRange.lowerEndpoint(), pRange.upperEndpoint());
    }

}
