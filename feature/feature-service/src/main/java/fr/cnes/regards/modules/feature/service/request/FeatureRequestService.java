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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 *
 * @author kevin
 *
 */
@Service
@MultitenantTransactional
public class FeatureRequestService implements IFeatureRequestService {

    @Autowired
    private IFeatureCreationRequestRepository fcrRepo;

    @Autowired
    private IPublisher publisher;

    @Override
    public void handleSuccess(String groupId) {
        List<FeatureCreationRequest> request = this.fcrRepo.findByGroupId(groupId);

        // publish success notification for all request id
        request.stream()
                .forEach(item -> publisher.publish(FeatureRequestEvent
                        .build(item.getRequestId(), item.getFeature() != null ? item.getFeature().getId() : null, null,
                               RequestState.SUCCESS, null)));

        // delete useless FeatureCreationRequest
        this.fcrRepo.deleteInBatch(request);
    }

    @Override
    public void handleError(String groupId) {
        List<FeatureCreationRequest> request = this.fcrRepo.findByGroupId(groupId);

        // publish success notification for all request id
        request.stream()
                .forEach(item -> publisher.publish(FeatureRequestEvent
                        .build(item.getRequestId(), item.getFeature() != null ? item.getFeature().getId() : null, null,
                               RequestState.ERROR, null)));
        // set FeatureCreationRequest to error state
        request.stream().forEach(item -> item.setState(RequestState.ERROR));

        this.fcrRepo.saveAll(request);

    }

}
