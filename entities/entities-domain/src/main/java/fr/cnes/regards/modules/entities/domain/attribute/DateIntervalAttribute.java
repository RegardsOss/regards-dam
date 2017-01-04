/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import java.time.LocalDateTime;

import fr.cnes.regards.modules.entities.domain.attribute.value.Interval;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#DATE_INTERVAL} model attribute
 *
 * @author Marc Sordi
 *
 */
public class DateIntervalAttribute extends AbstractAttribute<Interval<LocalDateTime>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.DATE_INTERVAL.equals(pAttributeType);
    }
}
