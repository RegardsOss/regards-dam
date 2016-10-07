/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

/**
 * @author msordi
 *
 */
public interface IRestriction {

    /**
     *
     * @return restriction type
     */
    RestrictionType getType();

    /**
     *
     * @return {@link Boolean#TRUE} if restriction is set
     */
    Boolean hasRestriction();

}
