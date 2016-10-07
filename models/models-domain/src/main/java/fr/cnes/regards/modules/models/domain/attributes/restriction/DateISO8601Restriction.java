/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Manage date format restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#DATE_ARRAY}</li>
 * <li>{@link AttributeType#DATE_INTERVAL}</li>
 * <li>{@link AttributeType#DATE_ISO8601}</li>
 * </ul>
 *
 * @author msordi
 *
 */
public class DateISO8601Restriction extends AbstractRestriction {

    /**
     * Constructor
     */
    public DateISO8601Restriction() {
        super();
        setType(RestrictionType.DATE_ISO8601);
    }
}
