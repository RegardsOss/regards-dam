/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain;

/**
 * Quality Level of an AIP, it is determined by a human
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public enum QualityLevel {

    /**
     * considered OK
     */
    ACCEPTED,
    /**
     * considered acceptable
     */
    ACCEPTED_WITH_WARNINGS,
    /**
     * considered unacceptable
     */
    REJECTED;

}
