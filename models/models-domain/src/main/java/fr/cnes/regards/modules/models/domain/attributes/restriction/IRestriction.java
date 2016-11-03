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

    /**
     * Restriction that can be set by users
     *
     * @return {@link Boolean#TRUE} if restriction is public
     */
    default Boolean isPublic() {
        return Boolean.FALSE;
    }
}
