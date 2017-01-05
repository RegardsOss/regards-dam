/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#DOUBLE} model attribute
 *
 * @author Marc Sordi
 *
 */
public class DoubleAttribute extends AbstractAttribute<Double> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.DOUBLE.equals(pAttributeType);
    }
}
