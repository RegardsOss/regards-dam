/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.entities.service.plugins;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.service.IStorageService;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 */
@Plugin(description = "With plugins does not POST AIP entities to Storrage module", id = "NohupStoragePlugin",
        version = "1.0.0", author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class NohupStoragePlugin implements IStorageService {

    @Override
    public <T extends AbstractEntity<?>> T storeAIP(T pToPersist) {
        // nothing to do because we don't create AIP without storage and description is inside the database not on file
        // system
        return pToPersist;
    }

    @Override
    public void deleteAIP(AbstractEntity<?> pToDelete) {
        // Nothing to do
    }

    @Override
    public <T extends AbstractEntity<?>> T updateAIP(T pToUpdate) {
        // Nothing to do
        return pToUpdate;
    }

}
