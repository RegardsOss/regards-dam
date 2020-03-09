/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.crawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.dam.domain.datasources.event.DatasourceEvent;
import fr.cnes.regards.modules.dam.domain.datasources.event.DatasourceEventType;
import fr.cnes.regards.modules.dam.gson.entities.DamGsonReadyEvent;

/**
 * Handler to handle {@link DatasourceEvent} events. Those events are sent when a datasource is deleted.
 *
 * @author Sébastien Binda
 *
 */
@Component
public class DatasourceEventHandler implements IHandler<DatasourceEvent> {

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IEntityIndexerService entityIndexerService;

    @Autowired
    private ISubscriber subscriber;

    @EventListener
    public void handleApplicationReady(DamGsonReadyEvent event) {
        subscriber.subscribeTo(DatasourceEvent.class, this);
    }

    @Override
    public void handle(TenantWrapper<DatasourceEvent> wrapper) {
        DatasourceEvent event = wrapper.getContent();
        if ((event.getType() == DatasourceEventType.DELETED) && (event.getDatasourceId() != null)) {
            runtimeTenantResolver.forceTenant(wrapper.getTenant());
            try {
                entityIndexerService.deleteDataObjectsFromDatasource(wrapper.getTenant(), event.getDatasourceId());
            } finally {
                runtimeTenantResolver.clearTenant();
            }
        }
    }

}
