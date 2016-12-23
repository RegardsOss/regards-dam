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
}
