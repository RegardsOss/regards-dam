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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.EntityAipState;
import fr.cnes.regards.modules.storage.domain.AIPState;
import fr.cnes.regards.modules.storage.domain.event.AIPEvent;

/**
 * Handler to update {@link AbstractEntity} state when a {@link AIPEvent} is received from storage.
 *
 * @author Christophe Mertz
 */
@Component
public class EntityEventHandler implements ApplicationListener<ApplicationReadyEvent> {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityEventHandler.class);

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * {@link ISubscriber} instance
     */
    @Autowired
    private ISubscriber subscriber;

    @Autowired
    private IDatasetService dasService;

    @Autowired
    private IDocumentService docService;

    @Autowired
    private ICollectionService colService;

    /**
     * Subscribe to {@link AIPEvent} in order to update {@link AbstractEntity} for each successfully stored AIP.
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent pEvent) {
        subscriber.subscribeTo(AIPEvent.class, new AIPEventHandler());
    }

    /**
     * {@link AIPEvent} handler
     *
     * @author Christophe Mertz
     */
    private class AIPEventHandler implements IHandler<AIPEvent> {

        @Override
        public void handle(TenantWrapper<AIPEvent> wrapper) {
            try {
                AIPEvent event = wrapper.getContent();
                if (event.getAipState() == AIPState.STORED) {
                    runtimeTenantResolver.forceTenant(wrapper.getTenant());
                    UniformResourceName urn = UniformResourceName.fromString(event.getIpId());

                    AbstractEntity<?> entity = getService(urn.getEntityType()).loadWithRelations(urn);

                    FeignSecurityManager.asSystem();
                    entity.setIpId(urn);
                    entity.setStateAip(EntityAipState.AIP_STORE_OK);
                    getService(urn.getEntityType()).save(entity);

                    LOGGER.info("AIP with IP_ID <" + urn.toString() + "> state set to <" + event.getAipState() + ">");
                }

            } catch (Exception e) {
                LOGGER.error("Error occurs during AIP event handling", e);
            } finally {
                runtimeTenantResolver.clearTenant();
                FeignSecurityManager.reset();
            }
        }

        @SuppressWarnings("rawtypes")
        private IEntityService getService(EntityType type) {
            if (type.equals(EntityType.DATASET)) {
                return dasService;
            } else if (type.equals(EntityType.DOCUMENT)) {
                return docService;
            } else if (type.equals(EntityType.COLLECTION)) {
                return colService;
            } else {
                throw new IllegalArgumentException("Unsupported entity type");
            }
        }
    }

}
