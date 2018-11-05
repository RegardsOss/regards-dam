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
package fr.cnes.regards.modules.indexer.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.jpa.multitenant.event.spring.TenantConnectionReady;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;

/**
 * Listening for tenant creation and application ready to manage Elasticsearch index creation
 * @author oroussel
 */
@Component
public class IndexerEventsListener {
    @Autowired
    private IEsRepository repository;

    @Autowired
    private ITenantResolver tenantResolver;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @EventListener
    public void handleApplicationReadyEvent(ApplicationReadyEvent event) {
        for (String tenant : tenantResolver.getAllActiveTenants()) {
            runtimeTenantResolver.forceTenant(tenant);
            this.checkIndex(tenant);
        }
    }

    /**
     * A tenant (or project) has been created and is ready to be used => create ElasticSearch index (if id doesn't
     * already exist)
     */
    @EventListener
    public void handleTenantConnectionReady(TenantConnectionReady event) {
        checkIndex(event.getTenant());
    }

    private void checkIndex(String tenant) {
        if (!repository.indexExists(tenant)) {
            boolean created = repository.createIndex(tenant);
            if (created) {
                String[] types = Arrays.stream(EntityType.values()).map(EntityType::toString)
                        .toArray(length -> new String[length]);
                repository.setAutomaticDoubleMapping(tenant, types);
                repository.setGeometryMapping(tenant, types);
            }
        }
    }
}
