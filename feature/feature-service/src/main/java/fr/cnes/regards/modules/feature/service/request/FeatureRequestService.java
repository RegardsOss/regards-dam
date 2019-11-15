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
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureDeletionRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureDeletionRequest;
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
    private IFeatureDeletionRequestRepository fdrRepo;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private IPublisher publisher;

    @Override
    public void handleStorageSuccess(Set<String> groupIds) {
        Set<FeatureCreationRequest> request = this.fcrRepo.findByGroupIdIn(groupIds);

        // publish success notification for all request id
        request.stream()
                .forEach(item -> publisher.publish(FeatureRequestEvent
                        .build(item.getRequestId(), item.getFeature() != null ? item.getFeature().getId() : null, null,
                               RequestState.SUCCESS, null)));

        // delete useless FeatureCreationRequest
        this.fcrRepo.deleteAll(request);
    }

    @Override
    public void handleStorageError(Set<String> groupIds) {
        Set<FeatureCreationRequest> request = this.fcrRepo.findByGroupIdIn(groupIds);

        // publish success notification for all request id
        request.stream()
                .forEach(item -> publisher.publish(FeatureRequestEvent
                        .build(item.getRequestId(), item.getFeature() != null ? item.getFeature().getId() : null, null,
                               RequestState.ERROR, null)));
        // set FeatureCreationRequest to error state
        request.stream().forEach(item -> item.setState(RequestState.ERROR));

        this.fcrRepo.saveAll(request);

    }

    @Override
    public void handleDeletionSuccess(Set<String> groupIds) {
        Set<FeatureDeletionRequest> request = this.fdrRepo.findByGroupIdIn(groupIds);

        // publish success notification for all request id
        request.stream().forEach(item -> publisher.publish(FeatureRequestEvent
                .build(item.getRequestId(), null, item.getUrn(), RequestState.SUCCESS, null)));

        // delete all FeatureEntity with the same urn of a FeatureDeletionRequest
        this.featureRepo.deleteByUrnIn(request.stream().map(fdr -> fdr.getUrn()).collect(Collectors.toSet()));
        // delete useless FeatureDeletionRequest
        this.fdrRepo.deleteAll(request);
    }

    @Override
    public void handleDeletionError(Set<String> groupIds) {
        Set<FeatureDeletionRequest> request = this.fdrRepo.findByGroupIdIn(groupIds);

        // publish success notification for all request id
        request.stream().forEach(item -> publisher.publish(FeatureRequestEvent
                .build(item.getRequestId(), null, item.getUrn(), RequestState.ERROR, null)));
        // set FeatureDeletionRequest to error state
        request.stream().forEach(item -> item.setState(RequestState.ERROR));

        this.fdrRepo.saveAll(request);
    }

}
