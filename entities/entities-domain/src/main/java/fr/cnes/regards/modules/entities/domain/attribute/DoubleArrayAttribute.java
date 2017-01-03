/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#DOUBLE_ARRAY} model attribute
 *
 * @author Marc Sordi
 *
 */
public class DoubleArrayAttribute extends AbstractAttribute<Double[]> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.DOUBLE_ARRAY.equals(pAttributeType);
    }
}
