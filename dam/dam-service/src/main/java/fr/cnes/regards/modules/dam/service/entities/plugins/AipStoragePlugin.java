/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.entities.plugins;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.service.entities.IStorageService;
import fr.cnes.regards.modules.storage.client.IStorageClient;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 */
@Plugin(description = "This plugin allows to POST AIP entities to storage unit", id = "AipStoragePlugin",
        version = "1.0.0", author = "REGARDS Team", contact = "regards@c-s.fr", license = "GPLv3", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class AipStoragePlugin implements IStorageService {

    @Autowired
    private IStorageClient storageClient;

    @Value("${zuul.prefix}")
    private String gatewayPrefix;

    @Value("${spring.application.name}")
    private String microserviceName;

    @Override
    public <T extends AbstractEntity<?>> T storeAIP(T toPersist) {
        throw new UnsupportedOperationException("Storage of AIP created by Datamanagement is not implemented yet ....");
    }

    @SuppressWarnings("serial")
    @Override
    public <T extends AbstractEntity<?>> T updateAIP(T toUpdate) {
        throw new UnsupportedOperationException("Storage of AIP created by Datamanagement is not implemented yet ....");
    }

    @Override
    public void deleteAIP(AbstractEntity<?> toDelete) {
        throw new UnsupportedOperationException("Storage of AIP created by Datamanagement is not implemented yet ....");
    }

}
