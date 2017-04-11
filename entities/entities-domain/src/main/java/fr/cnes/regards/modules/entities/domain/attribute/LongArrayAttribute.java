/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.entities.domain.attribute.adapter.LongIntervalAttributeAdapter;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author oroussel
 */
@JsonAdapter(LongIntervalAttributeAdapter.class)
public class LongArrayAttribute extends AbstractAttribute<Long[]> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.LONG_ARRAY.equals(pAttributeType);
    }

}
