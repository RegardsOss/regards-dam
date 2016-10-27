/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Restriction interface
 *
 * @author msordi
 *
 */
public interface IRestriction {

    RestrictionType getType();

    Boolean supports(AttributeType pAttributeType);
}
