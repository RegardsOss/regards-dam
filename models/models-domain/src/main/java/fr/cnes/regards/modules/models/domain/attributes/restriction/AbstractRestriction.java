/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

/**
 * @author msordi
 *
 */
public abstract class AbstractRestriction implements IRestriction {

    /**
     * Attribute restriction type
     */
    private RestrictionType type;

    @Override
    public RestrictionType getType() {
        return type;
    }

    public void setType(RestrictionType pType) {
        type = pType;
    }

}
