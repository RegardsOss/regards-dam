/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.entities.domain.attribute.value.Interval;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#INTEGER_INTERVAL} model attribute
 *
 * @author Marc Sordi
 *
 */
public class IntegerIntervalAttribute extends AbstractAttribute<Interval<Integer>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.INTEGER_INTERVAL.equals(pAttributeType);
    }
}
