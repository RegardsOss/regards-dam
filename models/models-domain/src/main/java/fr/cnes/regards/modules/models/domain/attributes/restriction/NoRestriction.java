/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * No restriction
 *
 * @author msordi
 *
 */
public class NoRestriction extends AbstractRestriction {

    public NoRestriction() {
        super();
        setType(RestrictionType.NO_RESTRICTION);
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return Boolean.FALSE;
    }
}
