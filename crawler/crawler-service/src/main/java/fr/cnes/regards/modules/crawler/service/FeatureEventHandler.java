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
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.dam.dto.FeatureEvent;
import fr.cnes.regards.modules.dam.dto.FeatureEventType;
import fr.cnes.regards.modules.dam.gson.entities.DamGsonReadyEvent;

/**
 * Handle {@link FeatureEvent}s to delete features from index.
 *
 * @author Sébastien Binda
 *
 */
@Component
public class FeatureEventHandler implements IHandler<FeatureEvent> {

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IEntityIndexerService entityIndexerService;

    @Autowired
    private ISubscriber subscriber;

    @EventListener
    public void handleApplicationReady(DamGsonReadyEvent event) {
        subscriber.subscribeTo(FeatureEvent.class, this);
    }

    @Override
    public void handle(TenantWrapper<FeatureEvent> wrapper) {
        FeatureEvent event = wrapper.getContent();
        if (event.getType() == FeatureEventType.DELETE) {
            runtimeTenantResolver.forceTenant(wrapper.getTenant());
            try {
                entityIndexerService.deleteDataObject(wrapper.getTenant(), event.getFeatureId());
            } catch (RsRuntimeException e) {
                String msg = String.format("Cannot delete Feature (%s)", event.getFeatureId());
                LOGGER.error(msg, e);
            }
        }
    }
}
