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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private ICollectionService entityService;

    @Override
    public void onRequestGranted(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onRequestDenied(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onCopySuccess(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onCopyError(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onAvailable(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onAvailabilityError(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onDeletionSuccess(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onDeletionError(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onReferenceSuccess(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onReferenceError(Set<RequestInfo> requests) {
        // Nothing to do
    }

    @Override
    public void onStoreSuccess(Set<RequestInfo> requests) {
        this.entityService.storeSucces(requests);
    }

    @Override
    public void onStoreError(Set<RequestInfo> requests) {
        this.entityService.storeError(requests);
    }

}
