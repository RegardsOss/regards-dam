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
package fr.cnes.regards.modules.feature.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.modules.locks.service.ILockService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;

/**
 * Enable feature task scheduling
 *
 * @author Marc SORDI
 *
 */
@Component
@Profile("!noscheduler")
@EnableScheduling
public class FeatureTaskScheduler {

    private static final String LOCK_REQUEST_UPDATE = "Update_Request";

    private static final String LOCK_REQUEST_INSERT = "Insert_Request";

    @Autowired
    private ITenantResolver tenantResolver;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private ILockService lockService;

    @Scheduled(fixedDelayString = "${regards.feature.request.update.scheduling.delay:1000}")
    public void scheduleUpdateRequests() {
        for (String tenant : tenantResolver.getAllActiveTenants()) {
            try {
                runtimeTenantResolver.forceTenant(tenant);
                if (lockService.obtainLockOrSkip(LOCK_REQUEST_UPDATE, this, 60)) {
                    // TODO delegate to update request service ...
                    // TODO find update request in state DELAYED and with a registration date < now - waiting delay to avoid update concurrency
                    // TODO chech update concurrency ... only select first request for a distinct URN chronologically speaking!
                }
            } finally {
                lockService.releaseLock(LOCK_REQUEST_UPDATE, this);
                runtimeTenantResolver.clearTenant();
            }
        }
    }

    @Scheduled(fixedDelayString = "${regards.feature.request.update.scheduling.delay:1000}")
    public void scheduleInsertRequests() {
        for (String tenant : tenantResolver.getAllActiveTenants()) {
            try {
                runtimeTenantResolver.forceTenant(tenant);
                if (lockService.obtainLockOrSkip(LOCK_REQUEST_INSERT, this, 60)) {

                }
            } finally {
                lockService.releaseLock(LOCK_REQUEST_INSERT, this);
                runtimeTenantResolver.clearTenant();
            }
        }
    }
}
