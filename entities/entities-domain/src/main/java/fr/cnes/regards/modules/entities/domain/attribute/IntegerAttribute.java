/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#INTEGER} model attribute
 *
 * @author Marc Sordi
 *
 */
public class IntegerAttribute extends AbstractAttribute<Integer> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.INTEGER.equals(pAttributeType);
    }
}
