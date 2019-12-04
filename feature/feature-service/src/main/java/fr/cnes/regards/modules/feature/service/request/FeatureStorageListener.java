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
package fr.cnes.regards.modules.feature.service.request;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.storage.client.IStorageRequestListener;
import fr.cnes.regards.modules.storage.client.RequestInfo;

/**
 *
 * This class offers callbacks from storage events
 *
 * @author kevin
 *
 */
@Component
public class FeatureStorageListener implements IStorageRequestListener {

    @Autowired
    private IFeatureRequestService featureRequestService;

    @Override
    public void onRequestGranted(Set<RequestInfo> requests) {
        // FIXME
    }

    @Override
    public void onRequestDenied(Set<RequestInfo> requests) {
        // FIXME
    }

    @Override
    public void onCopySuccess(Set<RequestInfo> requests) {
        throw new UnsupportedOperationException("onCopySuccess");
    }

    @Override
    public void onCopyError(Set<RequestInfo> requests) {
        throw new UnsupportedOperationException("onCopyError");
    }

    @Override
    public void onAvailable(Set<RequestInfo> requests) {
        throw new UnsupportedOperationException("onAvailable");

    }

    @Override
    public void onAvailabilityError(Set<RequestInfo> requests) {
        throw new UnsupportedOperationException("onAvailabilityError");
    }

    @Override
    public void onDeletionSuccess(Set<RequestInfo> requests) {
        this.featureRequestService.handleDeletionSuccess(requests.stream().map(request -> request.getGroupId())
                .collect(Collectors.toSet()));

    }

    @Override
    public void onDeletionError(Set<RequestInfo> requests) {
        this.featureRequestService
                .handleStorageError(requests.stream().map(request -> request.getGroupId()).collect(Collectors.toSet()));
    }

    @Override
    public void onReferenceSuccess(Set<RequestInfo> requests) {
        this.featureRequestService.handleStorageSuccess(requests.stream().map(request -> request.getGroupId())
                .collect(Collectors.toSet()));
    }

    @Override
    public void onReferenceError(Set<RequestInfo> requests) {
        this.featureRequestService
                .handleStorageError(requests.stream().map(request -> request.getGroupId()).collect(Collectors.toSet()));
    }

    @Override
    public void onStoreSuccess(Set<RequestInfo> requests) {
        this.featureRequestService.handleStorageSuccess(requests.stream().map(request -> request.getGroupId())
                .collect(Collectors.toSet()));
    }

    @Override
    public void onStoreError(Set<RequestInfo> requests) {
        this.featureRequestService
                .handleStorageError(requests.stream().map(request -> request.getGroupId()).collect(Collectors.toSet()));
    }

}
