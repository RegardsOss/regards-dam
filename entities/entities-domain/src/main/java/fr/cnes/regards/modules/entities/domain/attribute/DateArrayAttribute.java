/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import java.time.OffsetDateTime;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#DATE_ARRAY} model attribute
 *
 * @author Marc Sordi
 *
 */
public class DateArrayAttribute extends AbstractAttribute<OffsetDateTime[]> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.DATE_ARRAY.equals(pAttributeType);
    }
}
