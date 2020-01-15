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
package fr.cnes.regards.modules.feature.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.authentication.IAuthenticationResolver;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.validation.ErrorTranslator;
import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobParameter;
import fr.cnes.regards.framework.modules.jobs.service.IJobInfoService;
import fr.cnes.regards.modules.feature.dao.IFeatureCopyRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCopyRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureFile;
import fr.cnes.regards.modules.feature.dto.FeatureFileLocation;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;
import fr.cnes.regards.modules.feature.service.job.FeatureCopyJob;

/**
 * Feature copy service management
 *
 * @author Kevin Marchois
 */
@Service
@MultitenantTransactional
public class FeatureCopyService extends AbstractFeatureService implements IFeatureCopyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureCopyService.class);

    @Autowired
    private IFeatureCopyRequestRepository featureCopyRequestRepo;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private IAuthenticationResolver authResolver;

    @Autowired
    private IJobInfoService jobInfoService;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private Validator validator;

    @Autowired
    private FeatureConfigurationProperties properties;

    @Override
    public RequestInfo<FeatureUniformResourceName> registerRequests(List<FeatureCopyRequest> copies) {

        long registrationStart = System.currentTimeMillis();

        List<FeatureCopyRequest> grantedRequests = new ArrayList<>();
        RequestInfo<FeatureUniformResourceName> requestInfo = new RequestInfo<>();

        copies.forEach(item -> validateFeatureCopyRequest(item, grantedRequests, requestInfo));
        LOGGER.trace("------------->>> {} creation requests prepared in {} ms", grantedRequests.size(),
                     System.currentTimeMillis() - registrationStart);

        // Save a list of validated FeatureCreationRequest from a list of FeatureCreationRequestEvent
        featureCopyRequestRepo.saveAll(grantedRequests);
        LOGGER.trace("------------->>> {} creation requests registered in {} ms", grantedRequests.size(),
                     System.currentTimeMillis() - registrationStart);

        return requestInfo;
    }

    /**
     * Validate a {@link FeatureCopyRequest}
     * @param item to validate
     * @param grantedRequests list of validated requests
     * @param requestInfo
     */
    private void validateFeatureCopyRequest(FeatureCopyRequest item, List<FeatureCopyRequest> grantedRequests,
            RequestInfo<FeatureUniformResourceName> requestInfo) {
        // Validate event
        Errors errors = new MapBindingResult(new HashMap<>(), FeatureCopyRequest.class.getName());
        validator.validate(item, errors);
        if (errors.hasErrors()) {
            LOGGER.debug("Error during founded FeatureCopyRequest validation {}", errors.toString());
            requestInfo.addDeniedRequest(item.getUrn(), ErrorTranslator.getErrors(errors));
            // Publish DENIED request (do not persist it in DB)
            publisher.publish(FeatureRequestEvent.build(item.getRequestId(), null, null, RequestState.DENIED,
                                                        ErrorTranslator.getErrors(errors)));
            return;
        }
        // Publish GRANTED request
        publisher.publish(FeatureRequestEvent.build(item.getRequestId(), null, item.getUrn(), RequestState.GRANTED,
                                                    null));

        grantedRequests.add(item);
        requestInfo.addGrantedRequest(item.getUrn(), item.getRequestId());
    }

    @Override
    public int scheduleRequests() {
        long scheduleStart = System.currentTimeMillis();

        // Shedule job
        Set<JobParameter> jobParameters = Sets.newHashSet();
        Set<Long> requestIds = new HashSet<>();

        List<FeatureCopyRequest> requestsToSchedule = this.featureCopyRequestRepo
                .findByStep(FeatureRequestStep.LOCAL_DELAYED, PageRequest
                        .of(0, properties.getMaxBulkSize(), Sort.by(Order.asc("priority"), Order.asc("requestDate"))));
        requestIds.addAll(requestsToSchedule.stream().map(request -> request.getId()).collect(Collectors.toList()));
        if (!requestsToSchedule.isEmpty()) {

            featureCopyRequestRepo.updateStep(FeatureRequestStep.LOCAL_SCHEDULED, requestIds);

            jobParameters.add(new JobParameter(FeatureCopyJob.IDS_PARAMETER, requestIds));

            // the job priority will be set according the priority of the first request to schedule
            JobInfo jobInfo = new JobInfo(false, requestsToSchedule.get(0).getPriority().getPriorityLevel(),
                    jobParameters, authResolver.getUser(), FeatureCopyJob.class.getName());
            jobInfoService.createAsQueued(jobInfo);

            LOGGER.trace("------------->>> {} copy requests scheduled in {} ms", requestsToSchedule.size(),
                         System.currentTimeMillis() - scheduleStart);

            return requestIds.size();
        }

        return 0;
    }

    @Override
    public void processRequests(List<FeatureCopyRequest> requests) {

        long processStart = System.currentTimeMillis();

        Set<FeatureCopyRequest> successCopyRequest = new HashSet<>();
        // map of FeatureEntity by urn
        Map<FeatureUniformResourceName, FeatureEntity> entitiesToUpdate = this.featureRepo
                .findByUrnIn(requests.stream().map(FeatureCopyRequest::getUrn).collect(Collectors.toList())).stream()
                .map(Function.identity()).collect(Collectors.toMap(FeatureEntity::getUrn, Function.identity()));
        for (FeatureCopyRequest request : requests) {
            if (entitiesToUpdate.get(request.getUrn()) != null) {
                updateFeature(entitiesToUpdate.get(request.getUrn()).getFeature(), request, successCopyRequest);
            } else {
                LOGGER.error(String.format("No FeatureEntity found for URN %s", request.getUrn().toString()));
                request.setState(RequestState.ERROR);
            }
        }

        // update those with a error status
        this.featureCopyRequestRepo.saveAll(requests.stream().filter(request -> !successCopyRequest.contains(request))
                .collect(Collectors.toList()));
        // Successful requests are deleted now!
        this.featureCopyRequestRepo.deleteInBatch(successCopyRequest);

        LOGGER.trace("------------->>> {} copy request treated in {} ms", successCopyRequest.size(),
                     System.currentTimeMillis() - processStart);
    }

    /**
     * Add the copied file to the files of the {@link Feature}
     * @param feature to update
     * @param request contain location of copied file
     * @param successCopyRequest list of succeded requests
     */
    private void updateFeature(Feature feature, FeatureCopyRequest request,
            Set<FeatureCopyRequest> successCopyRequest) {
        Optional<FeatureFile> fileToUpdate = feature.getFiles().stream()
                .filter(file -> file.getAttributes().getChecksum().equals(request.getChecksum())).findFirst();
        if (fileToUpdate.isPresent()) {
            successCopyRequest.add(request);
            fileToUpdate.get().getLocations().add(FeatureFileLocation.build(request.getStorage()));
        } else {
            request.setState(RequestState.ERROR);
            LOGGER.error(String.format("No file found for checksum %s", request.getUrn().toString()));
        }
    }
}
