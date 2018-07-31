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
package fr.cnes.regards.modules.entities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;

/**
 * Scheduled actions to store AIP's entity bu rs-storage
 *
 * @author Christophe Mertz
 */
@Profile("!disable-scheduled-store-aip")
@Component
@EnableScheduling
public class ScheduledStoreAipEntitiesTasks {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledStoreAipEntitiesTasks.class);

    /**
     * Resolver to know all existing tenants of the current REGARDS instance.
     */
    @Autowired
    private ITenantResolver tenantResolver;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IEntitiesService entitiesService;

    /**
     * If true the AIP entities are send to Storage module to be stored
     */
    @Value("${regards.dam.post.aip.entities.to.storage:true}")
    private Boolean postAipEntitiesToStorage;

    @Scheduled(fixedRateString = "${regards.dam.store.aip.entities.delay:60000}",
            initialDelayString = "${regards.dam.store.aip.entities.initial.delay:60000}")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void processStoreAips() {
        if ((postAipEntitiesToStorage == null) || !postAipEntitiesToStorage) {
            return;
        }
        for (String tenant : tenantResolver.getAllActiveTenants()) {
            try {
                LOGGER.info("Scheduled task : Store AIP entities for tenant {}", tenant);
                runtimeTenantResolver.forceTenant(tenant);
                entitiesService.storeAips();
            } finally {
                runtimeTenantResolver.clearTenant();
            }
        }
    }

}
