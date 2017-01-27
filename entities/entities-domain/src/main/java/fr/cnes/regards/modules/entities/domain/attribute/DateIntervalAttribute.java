/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import java.time.LocalDateTime;

import com.google.common.collect.Range;
import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.entities.domain.attribute.adapter.DateIntervalAttributeAdapter;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#DATE_INTERVAL} model attribute
 *
 * @author Marc Sordi
 *
 */
@JsonAdapter(DateIntervalAttributeAdapter.class)
public class DateIntervalAttribute extends AbstractAttribute<Range<LocalDateTime>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.DATE_INTERVAL.equals(pAttributeType);
    }
}
