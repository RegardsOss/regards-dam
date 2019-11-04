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
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.IInstancePublisher;
import fr.cnes.regards.framework.jpa.multitenant.event.TenantConnectionFailed;
import fr.cnes.regards.framework.jpa.multitenant.event.spring.TenantConnectionReady;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;

/**
 * Listener for {@link IAccessGroupService} on a new {@link TenantConnectionReady}.
 * Initialize the default Roles for a new tenant.
 * @author Sébastien Binda
 *
 */
@Component
public class AccessGroupEventListener {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupEventListener.class);

    @Value("${spring.application.name}")
    private String microserviceName;

    @Autowired
    private IAccessGroupService accessGroupService;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * AMQP instance message publisher
     */
    @Autowired
    private IInstancePublisher instancePublisher;

    @EventListener
    public void processEvent(TenantConnectionReady event) {
        // Init default role for this tenant
        try {
            // Set working tenant
            runtimeTenantResolver.forceTenant(event.getTenant());
            accessGroupService.initDefaultAccessGroup();
        } catch (final ListenerExecutionFailedException e) {
            LOGGER.error("Cannot initialize connection  for tenant " + event.getTenant(), e);
            instancePublisher.publish(new TenantConnectionFailed(event.getTenant(), microserviceName));
        } finally {
            runtimeTenantResolver.clearTenant();
        }

    }

}
