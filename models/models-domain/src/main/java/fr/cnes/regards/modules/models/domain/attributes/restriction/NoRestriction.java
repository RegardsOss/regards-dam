/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

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

}
