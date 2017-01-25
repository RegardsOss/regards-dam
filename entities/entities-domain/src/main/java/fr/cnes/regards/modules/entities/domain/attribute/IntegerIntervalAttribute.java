/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.entities.domain.attribute.adapter.IntegerIntervalAttributeAdapter;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

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
