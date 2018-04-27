/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.indexer.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.jpa.multitenant.event.spring.TenantConnectionReady;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;

/**
 * Listening for tenant creation
 * @author oroussel
 */
@Component
public class IndexerEventListener {
    @Autowired
    private IEsRepository repository;

    /**
     * A tenant (or project) has been created and is ready to be used => create ElasticSearch index (if id doesn't
     * already exist)
     */
    @EventListener
    public void handleTenantConnectionReady(TenantConnectionReady event) {
        if (!repository.indexExists(event.getTenant())) {
            boolean created = repository.createIndex(event.getTenant());
            if (created) {
                String[] types = Arrays.stream(EntityType.values()).map(EntityType::toString)
                        .toArray(length -> new String[length]);
                repository.setAutomaticDoubleMapping(event.getTenant(), types);
                repository.setGeometryMapping(event.getTenant(), types);
            }
        }
    }
}
