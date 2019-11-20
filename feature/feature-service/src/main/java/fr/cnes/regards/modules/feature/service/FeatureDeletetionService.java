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
package fr.cnes.regards.modules.feature.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import fr.cnes.regards.modules.feature.dao.IFeatureDeletionRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureDeletionRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.FeatureFile;
import fr.cnes.regards.modules.feature.dto.FeatureFileAttributes;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureDeletionRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;
import fr.cnes.regards.modules.feature.service.job.FeatureCreationJob;
import fr.cnes.regards.modules.feature.service.job.FeatureDeletionJob;
import fr.cnes.regards.modules.storagelight.client.IStorageClient;
import fr.cnes.regards.modules.storagelight.domain.dto.request.FileDeletionRequestDTO;

/**
 * @author Kevin Marchois
 *
 */
@Service
@MultitenantTransactional
public class FeatureDeletetionService implements IFeatureDeletionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureDeletetionService.class);

    private static final String ONLINE_CONF = "ONLINE_CONF";

    @Autowired
    private IFeatureDeletionRequestRepository deletionRepo;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private Validator validator;

    @Autowired
    private IJobInfoService jobInfoService;

    @Autowired
    private IAuthenticationResolver authResolver;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private IStorageClient storageClient;

    @Autowired
    private FeatureConfigurationProperties properties;

    @Override
    public RequestInfo<FeatureUniformResourceName> registerRequests(List<FeatureDeletionRequestEvent> events) {
        long registrationStart = System.currentTimeMillis();

        List<FeatureDeletionRequest> grantedRequests = new ArrayList<>();
        RequestInfo<FeatureUniformResourceName> requestInfo = new RequestInfo<>();

        events.forEach(item -> prepareFeatureDeletionRequest(item, grantedRequests, requestInfo));
        LOGGER.trace("------------->>> {} deletion requests prepared in {} ms", grantedRequests.size(),
                     System.currentTimeMillis() - registrationStart);

        // Save a list of validated FeatureDeletionRequest from a list of
        deletionRepo.saveAll(grantedRequests);
        LOGGER.debug("------------->>> {} deletion requests registered in {} ms", grantedRequests.size(),
                     System.currentTimeMillis() - registrationStart);
        return requestInfo;
    }

    private void prepareFeatureDeletionRequest(FeatureDeletionRequestEvent item,
            List<FeatureDeletionRequest> grantedRequests, RequestInfo<FeatureUniformResourceName> requestInfo) {
        // Validate event
        Errors errors = new MapBindingResult(new HashMap<>(), FeatureDeletionRequest.class.getName());

        validator.validate(item, errors);
        if (errors.hasErrors()) {
            LOGGER.debug("Error during founded FeatureDeletionRequest validation {}", errors.toString());
            requestInfo.addDeniedRequest(item.getUrn(), ErrorTranslator.getErrors(errors));
            // Publish DENIED request (do not persist it in DB)
            publisher.publish(FeatureRequestEvent.build(item.getRequestId(), null, item.getUrn(), RequestState.DENIED,
                                                        ErrorTranslator.getErrors(errors)));
            return;
        }

        FeatureDeletionRequest request = FeatureDeletionRequest
                .build(item.getRequestId(), item.getRequestDate(), RequestState.GRANTED, null,
                       FeatureRequestStep.LOCAL_DELAYED, item.getPriority(), item.getUrn());
        // Publish GRANTED request
        publisher.publish(FeatureRequestEvent.build(item.getRequestId(), null, item.getUrn(), RequestState.GRANTED,
                                                    null));

        // Add to granted request collection
        grantedRequests.add(request);
        requestInfo.addGrantedRequest(item.getUrn(), request.getRequestId());
    }

    @Override
    public boolean scheduleRequests() {
        long scheduleStart = System.currentTimeMillis();

        // Shedule job
        Set<JobParameter> jobParameters = Sets.newHashSet();
        Set<Long> requestIds = new HashSet<>();
        List<FeatureDeletionRequest> requestsToSchedule = new ArrayList<>();

        Page<FeatureDeletionRequest> dbRequests = this.deletionRepo
                .findByStep(FeatureRequestStep.LOCAL_DELAYED, PageRequest
                        .of(0, properties.getMaxBulkSize(), Sort.by(Order.asc("priority"), Order.asc("requestDate"))));

        if (!dbRequests.isEmpty()) {
            for (FeatureDeletionRequest request : dbRequests.getContent()) {
                requestsToSchedule.add(request);
                requestIds.add(request.getId());
            }
            deletionRepo.updateStep(FeatureRequestStep.LOCAL_SCHEDULED, requestIds);

            jobParameters.add(new JobParameter(FeatureCreationJob.IDS_PARAMETER, requestIds));

            // the job priority will be set according the priority of the first request to schedule
            JobInfo jobInfo = new JobInfo(false, requestsToSchedule.get(0).getPriority().getPriorityLevel(),
                    jobParameters, authResolver.getUser(), FeatureDeletionJob.class.getName());
            jobInfoService.createAsQueued(jobInfo);

            LOGGER.debug("------------->>> {} deletion requests scheduled in {} ms", requestsToSchedule.size(),
                         System.currentTimeMillis() - scheduleStart);
            return true;
        }
        return false;
    }

    @Override
    public void processRequests(List<FeatureDeletionRequest> requests) {
        Map<FeatureUniformResourceName, FeatureEntity> featureByUrn = this.featureRepo
                .findByUrnIn(requests.stream().map(request -> request.getUrn()).collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(FeatureEntity::getUrn, Function.identity()));
        List<FeatureDeletionRequest> requestsWithFiles = requests.stream()
                .filter(fdr -> haveFiles(fdr, featureByUrn.get(fdr.getUrn())))
                .map(fdr -> publishFiles(fdr, featureByUrn.get(fdr.getUrn()))).collect(Collectors.toList());
        this.deletionRepo.saveAll(requestsWithFiles);
        // delete all FeatureEntityConcerned
        this.featureRepo.deleteByIdIn(requests.stream().filter(fdr -> !haveFiles(fdr, featureByUrn.get(fdr.getUrn())))
                .map(fdr -> featureByUrn.get(fdr.getUrn()).getId()).collect(Collectors.toSet()));
        // delete all FeatureEntityConcerned
        this.deletionRepo.deleteByIdIn(requests.stream().filter(fdr -> !haveFiles(fdr, featureByUrn.get(fdr.getUrn())))
                .map(fdr -> fdr.getId()).collect(Collectors.toSet()));
    }

    private boolean haveFiles(FeatureDeletionRequest fdr, FeatureEntity feature) {
        return feature.getFeature().getFiles() != null && !feature.getFeature().getFiles().isEmpty();
    }

    /**
     * Publish command to delete all contained files inside the {@link FeatureDeletionRequest} to
     * storage
     *
     * @param fdr
     * @return
     */
    private FeatureDeletionRequest publishFiles(FeatureDeletionRequest fdr, FeatureEntity feature) {
        fdr.setStep(FeatureRequestStep.REMOTE_STORAGE_DELETION_REQUESTED);
        for (FeatureFile file : feature.getFeature().getFiles()) {
            FeatureFileAttributes attribute = file.getAttributes();
            fdr.setGroupId(this.storageClient
                    .delete(FileDeletionRequestDTO.build(attribute.getChecksum(), ONLINE_CONF,
                                                         feature.getFeature().getUrn().toString(), false))
                    .getGroupId());
        }
        return fdr;
    }

}
