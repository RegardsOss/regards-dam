/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IStorageService {

    /**
     * method to call whenever an {@link AbstractEntity} has changed or is created and modifications has to be
     * communicated to the storage unit
     *
     * @param <T>
     *            one of {@link AbstractEntity} sub class
     * @param pToPersist
     *            {@link AbstractEntity} to be persisted
     * @return persisted {@link AbstractEntity}
     */
    public <T extends AbstractEntity> T persist(T pToPersist);

    public void delete(AbstractEntity pToDelete);

}
