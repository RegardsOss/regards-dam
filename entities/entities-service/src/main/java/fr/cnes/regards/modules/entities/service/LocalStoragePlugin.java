/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@Plugin(description = "Plugin responsible for the local storing of entities as AIP ", id = "LocalStoragePlugin",
        version = "1.0.0", author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class LocalStoragePlugin implements IStorageService {

    @Override
    public <T extends AbstractEntity> T storeAIP(T pToPersist) {
        // nothing to do because we don't create AIP without storage and description is inside the database not on file
        // system
        return pToPersist;
    }

    @Override
    public void deleteAIP(AbstractEntity pToDelete) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T extends AbstractEntity> T updateAIP(T pToUpdate) {
        // TODO Auto-generated method stub
        return pToUpdate;
    }

}
