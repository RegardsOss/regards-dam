/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 *
 * Manage geometry restriction for attribute of type :
 * <ul>
 * <li>{@link AttributeType#URL}</li>
 * </ul>
 *
 * @author msordi
 *
 */
public class UrlRestriction extends AbstractRestriction {

    public UrlRestriction() {
        super();
        setType(RestrictionType.URL);
    }

    @Override
    public Boolean supports(AttributeType pAttributeType) {
        return AttributeType.URL.equals(pAttributeType);
    }

}
