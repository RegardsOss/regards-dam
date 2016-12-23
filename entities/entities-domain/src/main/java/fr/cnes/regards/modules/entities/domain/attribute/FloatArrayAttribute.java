/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#FLOAT_ARRAY} model attribute
 *
 * @author Marc Sordi
 *
 */
public class FloatArrayAttribute extends AbstractAttribute<Double[]> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.FLOAT_ARRAY.equals(pAttributeType);
    }
}
