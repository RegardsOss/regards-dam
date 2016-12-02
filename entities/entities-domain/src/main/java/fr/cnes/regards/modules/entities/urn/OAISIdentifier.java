/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.urn;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public enum OAISIdentifier {
    /**
     * Submission Information Package
     */
    SIP,
    /**
     * Archival Information Package
     */
    AIP,
    /**
     * Dissemination Information Package
     */
    DIP;

    @Override
    public String toString() {
        return name();
    }
}
