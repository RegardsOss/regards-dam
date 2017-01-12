/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Service
public class StorageService implements IStorageService {

    @Override
    public <T extends AbstractEntity> T persist(T pToPersist) {
        // TODO real implementation
        return pToPersist;
    }

    @Override
    public void delete(AbstractEntity pToDelete) {
        // TODO Auto-generated method stub

    }

}
