/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.attributes.restriction;

/**
 *
 * Available restriction type
 * 
 * @author msordi
 *
 */
public enum RestrictionType {

    /**
     * Acceptable restriction type
     */
    NO_RESTRICTION, PATTERN, ENUMERATION, DATE_ISO8601, INTEGER_RANGE, DOUBLE_RANGE, URL, GEOMETRY;
}
