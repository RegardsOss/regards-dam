/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute;

import fr.cnes.regards.modules.entities.domain.validator.CheckGeometry;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Represent a {@link AttributeType#GEOMETRY} model attribute
 *
 * @author Marc Sordi
 *
 */
@CheckGeometry
public class GeometryAttribute extends AbstractAttribute<String> {

    @Override
    public boolean represents(AttributeType pAttributeType) {
        return AttributeType.GEOMETRY.equals(pAttributeType);
    }
}
