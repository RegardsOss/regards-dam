package fr.cnes.regards.modules.dam.domain.entities.attribute;

import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;

/**
 * @author oroussel
 */
public class LongAttribute extends AbstractAttribute<Long> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.LONG.equals(pAttributeType);
    }

}
