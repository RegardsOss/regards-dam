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
     * no access at all on the dataset and so we do not have access to its data
     */
    NO_ACCESS,
    /**
     * only acces to meta data of the dataset but do not have access to its data at all(meta data and data)
     */
    RESTRICTED_ACCESS,
    /**
     * full acces to the dataset(so the meta data of the dataset and the meta data of its data), the access to the
     * physical data of the datum is constrained by the {@link DataAccessRight}
     */
    FULL_ACCESS;
}
