package fr.cnes.regards.modules.dam.domain.entities.attribute;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.dam.domain.entities.attribute.adapter.LongIntervalAttributeAdapter;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#INTEGER_INTERVAL} model attribute
 * 
 * @author oroussel
 * @author Christophe Mertz
 */
@JsonAdapter(LongIntervalAttributeAdapter.class)
public class LongIntervalAttribute extends AbstractAttribute<Range<Long>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.INTEGER_INTERVAL.equals(pAttributeType);
    }

}
