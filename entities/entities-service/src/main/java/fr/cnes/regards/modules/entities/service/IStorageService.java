/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@PluginInterface(
        description = "plugins interface which are responsible for storing entities(Collection, Dataset, Document, Data)")
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
    public <T extends AbstractEntity> T storeAIP(T pToPersist);

    public void deleteAIP(AbstractEntity pToDelete);

    public <T extends AbstractEntity> T updateAIP(T pToUpdate);

}
