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
package fr.cnes.regards.modules.dam.listener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.notification.NotificationLevel;
import fr.cnes.regards.framework.notification.client.INotificationClient;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.dam.service.entities.ICollectionService;
import fr.cnes.regards.modules.storage.client.IStorageRequestListener;
import fr.cnes.regards.modules.storage.client.RequestInfo;

/**
 * Listener for storage callback requests
 * @author Kevin Marchois
 *
 */
@Component
public class StorageListener implements IStorageRequestListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageListener.class);

    @Autowired
    private ICollectionService entityService;

    @Autowired
    private INotificationClient notificationClient;

    @Override
    public void onRequestGranted(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onRequestDenied(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCopySuccess(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCopyError(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAvailable(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAvailabilityError(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeletionSuccess(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeletionError(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReferenceSuccess(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReferenceError(Set<RequestInfo> requests) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStoreSuccess(Set<RequestInfo> requests) {
        this.entityService.storeSucces(requests);
    }

    @Override
    public void onStoreError(Set<RequestInfo> requests) {
        requests.stream().forEach(request -> {
            this.notificationClient.notify("Storage request with groupId {} failed", "Storage request failed",
                                           NotificationLevel.ERROR, DefaultRole.PROJECT_ADMIN);

            LOGGER.error("Storage request with groupId {} failed", request.getGroupId());
        });
    }

}
