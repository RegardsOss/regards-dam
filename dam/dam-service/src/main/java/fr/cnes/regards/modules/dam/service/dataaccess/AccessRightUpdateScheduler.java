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
package fr.cnes.regards.modules.dam.service.dataaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.plugins.IDataObjectAccessFilterPlugin;

/**
 * Scheduler to handle dynamic modification of access rights.
 * Dynamic modification are done by {@link IDataObjectAccessFilterPlugin} plugins.
 * @author Sébastien Binda
 */
@Component
public class AccessRightUpdateScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessRightUpdateScheduler.class);

    /**
     * All tenants resolver
     */
    @Autowired
    private ITenantResolver tenantResolver;

    @Autowired
    private IAccessRightService accessRightService;

    /**
     * Current tenant resolver
     */
    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Scheduled to be executed once a day
     */
    @Scheduled(cron = "${regards.access.rights.update.cron:0 0 1 ? * *}")
    public void updateDynamicRights() {
        LOGGER.info("AccessRightUpdateScheduler.updateDynamicRights() called...");
        for (String tenant : tenantResolver.getAllActiveTenants()) {
            runtimeTenantResolver.forceTenant(tenant);
            accessRightService.updateDynamicAccessRights();
            runtimeTenantResolver.clearTenant();
        }
        LOGGER.info("...AccessRightUpdateScheduler.updateDynamicRights() ended.");
    }

}
