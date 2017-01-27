/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.entities.domain.attribute.adapter.DoubleIntervalAttributeAdapter;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#DOUBLE_INTERVAL} model attribute
 *
 * @author Marc Sordi
 *
 */
@JsonAdapter(DoubleIntervalAttributeAdapter.class)
public class DoubleIntervalAttribute extends AbstractAttribute<Range<Double>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.DOUBLE_INTERVAL.equals(pAttributeType);
    }
}
