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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
import fr.cnes.regards.modules.feature.dao.IFeatureUpdateRequestRepository;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.FeatureUpdateRequest;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureUpdateRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;
import fr.cnes.regards.modules.feature.service.job.FeatureCreationJob;
import fr.cnes.regards.modules.feature.service.job.FeatureUpdateJob;
import fr.cnes.regards.modules.feature.service.job.feature.FeatureJobPriority;
import fr.cnes.regards.modules.model.service.validation.ValidationMode;

/**
 * @author Marc SORDI
 */
@Service
@MultitenantTransactional
public class FeatureUpdateService implements IFeatureUpdateService {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureUpdateService.class);

    @Autowired
    private Validator validator;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private IFeatureValidationService validationService;

    @Autowired
    private IFeatureUpdateRequestRepository updateRepo;

    @Autowired
    private IJobInfoService jobInfoService;

    @Autowired
    private IAuthenticationResolver authResolver;

    @Autowired
    private FeatureConfigurationProperties properties;

    @Override
    public void registerUpdateRequests(List<FeatureUpdateRequestEvent> items) {
        List<FeatureUpdateRequest> grantedRequests = new ArrayList<>();
        items.forEach(item -> prepareFeatureUpdateRequest(item, grantedRequests));

        // Batch save
        updateRepo.saveAll(grantedRequests);
    }

    /**
     * Validate, save and publish a new request
     *
     * @param item            request to manage
     * @param grantedRequests collection of granted requests to populate
     */
    private void prepareFeatureUpdateRequest(FeatureUpdateRequestEvent item,
            List<FeatureUpdateRequest> grantedRequests) {

        // Validate event
        Errors errors = new MapBindingResult(new HashMap<>(), FeatureCreationRequestEvent.class.getName());
        validator.validate(item, errors);

        if (errors.hasErrors()) {
            // Publish DENIED request (do not persist it in DB)
            publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
                                                        item.getFeature() != null ? item.getFeature().getId() : null,
                                                        null, RequestState.DENIED, ErrorTranslator.getErrors(errors)));
            return;
        }

        // Validate feature according to the data model
        errors = validationService.validate(item.getFeature(), ValidationMode.UPDATE);

        if (errors.hasErrors()) {
            publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
                                                        item.getFeature() != null ? item.getFeature().getId() : null,
                                                        null, RequestState.DENIED, ErrorTranslator.getErrors(errors)));
            return;
        }

        // Manage granted request
        FeatureUpdateRequest request = FeatureUpdateRequest.build(item.getRequestId(), item.getRequestDate(),
                                                                  RequestState.GRANTED, null, item.getFeature());
        request.setStep(FeatureRequestStep.LOCAL_DELAYED);

        // Publish GRANTED request
        publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
                                                    item.getFeature() != null ? item.getFeature().getId() : null, null,
                                                    RequestState.GRANTED, null));
        // Add to granted request collection
        grantedRequests.add(request);
    }

    @Override
    public void scheduleUpdateRequestProcessing() {

        Set<JobParameter> jobParameters = Sets.newHashSet();
        List<FeatureUpdateRequest> delayedRequests = this.updateRepo
                .findRequestToSchedule(PageRequest.of(0, this.properties.getMaxBulkSize()),
                                       OffsetDateTime.now().minusSeconds(this.properties.getDelayBeforeProcessing()));

        if (!delayedRequests.isEmpty()) {
            List<FeatureUpdateRequest> toSchedule = new ArrayList<FeatureUpdateRequest>();
            FeatureUpdateRequest currentRequest;

            for (int i = 0; i < delayedRequests.size(); i++) {
                currentRequest = delayedRequests.get(i);
                currentRequest.setStep(FeatureRequestStep.LOCAL_SCHEDULED);
                toSchedule.add(currentRequest);
            }

            this.updateRepo.saveAll(toSchedule);

            jobParameters.add(new JobParameter(FeatureUpdateJob.IDS_PARAMETER,
                    toSchedule.stream().map(fcr -> fcr.getId()).collect(Collectors.toList())));

            JobInfo jobInfo = new JobInfo(false, FeatureJobPriority.FEATURE_CREATION_JOB_PRIORITY.getPriority(),
                    jobParameters, authResolver.getUser(), FeatureCreationJob.class.getName());
            jobInfoService.createAsQueued(jobInfo);
        }
    }

    @Override
    public void updateFeatures(List<FeatureUpdateRequest> featureUpdateRequests) {
        // TODO Auto-generated method stub

    }

}
