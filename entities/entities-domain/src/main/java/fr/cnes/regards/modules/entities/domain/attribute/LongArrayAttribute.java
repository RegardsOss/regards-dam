package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * @author oroussel
 */
public class LongArrayAttribute extends AbstractAttribute<Long[]> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.LONG_ARRAY.equals(pAttributeType);
    }

}
