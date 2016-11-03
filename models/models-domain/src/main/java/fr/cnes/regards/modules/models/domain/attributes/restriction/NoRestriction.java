/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * No restriction
 *
 * @author msordi
 *
 */
@Entity(name = "NoRestriction")
@DiscriminatorValue("No")
public class NoRestriction extends AbstractRestriction {

    public NoRestriction() {
        super();
        setType(RestrictionType.NO_RESTRICTION);
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return Boolean.TRUE;
    }
}
