/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Manage geometry restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#GEOMETRY}</li>
 * </ul>
 *
 * @author msordi
 *
 */
public class GeometryRestriction extends AbstractRestriction {

    public GeometryRestriction() {
        super();
        setType(RestrictionType.GEOMETRY);
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.GEOMETRY.equals(pAttributeType);
    }
}
