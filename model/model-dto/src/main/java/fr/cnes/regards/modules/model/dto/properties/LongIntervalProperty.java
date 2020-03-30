package fr.cnes.regards.modules.model.dto.properties;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.model.dto.properties.adapter.LongIntervalAttributeAdapter;

/**
 * Represent a {@link PropertyType#INTEGER_INTERVAL} model attribute
 *
 * @author oroussel
 * @author Christophe Mertz
 */
@JsonAdapter(LongIntervalAttributeAdapter.class)
public class LongIntervalProperty extends AbstractProperty<Range<Long>> {

    @Override
    public PropertyType getType() {
        return PropertyType.LONG_INTERVAL;
    }

}
