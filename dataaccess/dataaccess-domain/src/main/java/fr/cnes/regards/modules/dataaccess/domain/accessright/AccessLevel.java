/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

/**
 * Access level on a dataset and all its data or one of its subset
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public enum AccessLevel {
    /**
     * no access at all on the dataset
     */
    NO_ACCESS,
    /**
     * only acces to FIXME
     */
    RESTRICTED_ACCESS,
    /**
     * full acces to the dataset( both metadata and the data)
     */
    FULL_ACCESS;
}
