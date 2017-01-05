/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.entities.domain.attribute.value.Interval;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#DOUBLE_INTERVAL} model attribute
 *
 * @author Marc Sordi
 *
 */
public class DoubleIntervalAttribute extends AbstractAttribute<Interval<Double>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.DOUBLE_INTERVAL.equals(pAttributeType);
    }
}
