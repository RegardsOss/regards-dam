/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#FLOAT} model attribute
 *
 * @author Marc Sordi
 *
 */
public class FloatAttribute extends AbstractAttribute<Float> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.FLOAT.equals(pAttributeType);
    }
}
