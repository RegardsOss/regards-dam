/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.xml.IXmlisable;
import fr.cnes.regards.modules.models.schema.Restriction;

/**
 * Restriction interface
 *
 * @author msordi
 *
 */
public interface IRestriction extends IXmlisable<Restriction> {

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
