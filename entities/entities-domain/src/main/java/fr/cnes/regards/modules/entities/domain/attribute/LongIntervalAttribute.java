package fr.cnes.regards.modules.entities.domain.attribute;

import com.google.common.collect.Range;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author oroussel
 */
public class LongIntervalAttribute extends AbstractAttribute<Range<Long>> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.INTEGER_INTERVAL.equals(pAttributeType);
    }

}
