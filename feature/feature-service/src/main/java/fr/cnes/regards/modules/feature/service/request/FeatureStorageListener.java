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

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.storagelight.client.IStorageRequestListener;
import fr.cnes.regards.modules.storagelight.client.RequestInfo;
import fr.cnes.regards.modules.storagelight.domain.dto.request.RequestResultInfoDTO;

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
    public void onRequestGranted(RequestInfo request) {

    }

    @Override
    public void onRequestDenied(RequestInfo request) {

    }

    @Override
    public void onCopySuccess(RequestInfo request, Collection<RequestResultInfoDTO> success) {

    }

    @Override
    public void onCopyError(RequestInfo request, Collection<RequestResultInfoDTO> success,
            Collection<RequestResultInfoDTO> errors) {

    }

    @Override
    public void onAvailable(RequestInfo request, Collection<RequestResultInfoDTO> success) {

    }

    @Override
    public void onAvailabilityError(RequestInfo request, Collection<RequestResultInfoDTO> success,
            Collection<RequestResultInfoDTO> errors) {

    }

    @Override
    public void onDeletionSuccess(RequestInfo request, Collection<RequestResultInfoDTO> success) {

    }

    @Override
    public void onDeletionError(RequestInfo request, Collection<RequestResultInfoDTO> success,
            Collection<RequestResultInfoDTO> errors) {

    }

    @Override
    public void onReferenceSuccess(RequestInfo request, Collection<RequestResultInfoDTO> success) {
        featureRequestService.handleSuccess(request.getGroupId());
    }

    @Override
    public void onReferenceError(RequestInfo request, Collection<RequestResultInfoDTO> success,
            Collection<RequestResultInfoDTO> errors) {
        featureRequestService.handleError(request.getGroupId());

    }

    @Override
    public void onStoreSuccess(RequestInfo request, Collection<RequestResultInfoDTO> success) {
        featureRequestService.handleSuccess(request.getGroupId());
    }

    @Override
    public void onStoreError(RequestInfo request, Collection<RequestResultInfoDTO> success,
            Collection<RequestResultInfoDTO> errors) {
        featureRequestService.handleError(request.getGroupId());
    }

}
