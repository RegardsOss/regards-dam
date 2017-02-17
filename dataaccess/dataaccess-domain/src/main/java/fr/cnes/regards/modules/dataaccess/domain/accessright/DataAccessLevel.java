/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

/**
 *
 * describe the level of access to the physical data of a datum in a dataset
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public enum DataAccessLevel {
    /**
     * despite the full access on the dataset, we still do no have access to the physical data of the datum(still have
     * access to the meta data of the datum)
     */
    NO_ACCESS,
    /**
     * default level, the access to the physical data is inherited from the access to the meta data of the datum
     */
    INHERITED_ACCESS,
    /**
     * access is defined thanks to a plugin of type {@link ICheckDataAccess}
     */
    CUSTOM_ACCESS
}
