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
import java.util.Set;
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
import com.google.gson.Gson;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.authentication.IAuthenticationResolver;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.validation.ErrorTranslator;
import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobParameter;
import fr.cnes.regards.framework.modules.jobs.service.IJobInfoService;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.dao.INotificationRequestRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureDeletionRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.NotificationRequest;
import fr.cnes.regards.modules.feature.dto.event.in.NotificationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;
import fr.cnes.regards.modules.feature.service.job.FeatureCreationJob;
import fr.cnes.regards.modules.feature.service.job.NotificationRequestJob;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationActionEvent;

/**
 * Service for prepare {@link NotificationActionEvent} from {@link NotificationRequestEvent}
 * @author Kevin Marchois
 *
 */
@Service
@MultitenantTransactional
public class FeatureNotificationService implements IFeatureNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureNotificationService.class);

    @Autowired
    private INotificationRequestRepository notificationRequestRepo;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private Validator validator;

    @Autowired
    private FeatureConfigurationProperties properties;

    @Autowired
    private IJobInfoService jobInfoService;

    @Autowired
    private IAuthenticationResolver authResolver;

    @Autowired
    private Gson gson;

    @Override
    public int registerRequests(List<NotificationRequestEvent> events) {
        long registrationStart = System.currentTimeMillis();

        List<NotificationRequest> notificationsRequest = new ArrayList<>();

        events.forEach(item -> prepareNotificationRequest(item, notificationsRequest));
        LOGGER.trace("------------->>> {} deletion requests prepared in {} ms", notificationsRequest.size(),
                     System.currentTimeMillis() - registrationStart);

        // Save a list of validated FeatureDeletionRequest from a list of
        notificationRequestRepo.saveAll(notificationsRequest);
        LOGGER.debug("------------->>> {} deletion requests registered in {} ms", notificationsRequest.size(),
                     System.currentTimeMillis() - registrationStart);
        return notificationsRequest.size();
    }

    /**
     * Prepare {@link NotificationRequest} from {@link NotificationRequestEvent} to register in database
     * @param item {@link NotificationRequestEvent} source
     * @param notificationsRequest list of {@link NotificationRequest} to prepare
     */
    private void prepareNotificationRequest(NotificationRequestEvent item,
            List<NotificationRequest> notificationsRequest) {
        // Validate event
        Errors errors = new MapBindingResult(new HashMap<>(), FeatureDeletionRequest.class.getName());

        validator.validate(item, errors);
        if (errors.hasErrors()) {
            LOGGER.debug("Error during founded NotificationRequestEvent validation {}", errors.toString());
            publisher.publish(FeatureRequestEvent.build(item.getRequestId(), null, item.getUrn(), RequestState.DENIED,
                                                        ErrorTranslator.getErrors(errors)));
            return;
        }

        NotificationRequest request = NotificationRequest.build(item.getRequestId(), item.getRequestDate(),
                                                                FeatureRequestStep.LOCAL_DELAYED, item.getPriority(),
                                                                item.getUrn());
        // Publish GRANTED request
        publisher.publish(FeatureRequestEvent.build(item.getRequestId(), null, item.getUrn(), RequestState.GRANTED,
                                                    null));
        notificationsRequest.add(request);
    }

    @Override
    public int scheduleRequests() {
        long scheduleStart = System.currentTimeMillis();

        // Shedule job
        Set<JobParameter> jobParameters = Sets.newHashSet();
        Set<Long> requestIds = new HashSet<>();
        List<NotificationRequest> requestsToSchedule = new ArrayList<>();

        Page<NotificationRequest> dbRequests = this.notificationRequestRepo
                .findByStep(FeatureRequestStep.LOCAL_DELAYED, PageRequest
                        .of(0, properties.getMaxBulkSize(), Sort.by(Order.asc("priority"), Order.asc("requestDate"))));

        if (!dbRequests.isEmpty()) {
            for (NotificationRequest request : dbRequests.getContent()) {
                requestsToSchedule.add(request);
                requestIds.add(request.getId());
            }
            notificationRequestRepo.updateStep(FeatureRequestStep.LOCAL_SCHEDULED, requestIds);

            jobParameters.add(new JobParameter(FeatureCreationJob.IDS_PARAMETER, requestIds));

            // the job priority will be set according the priority of the first request to schedule
            JobInfo jobInfo = new JobInfo(false, requestsToSchedule.get(0).getPriority().getPriorityLevel(),
                    jobParameters, authResolver.getUser(), NotificationRequestJob.class.getName());
            jobInfoService.createAsQueued(jobInfo);

            LOGGER.debug("------------->>> {} deletion requests scheduled in {} ms", requestsToSchedule.size(),
                         System.currentTimeMillis() - scheduleStart);
            return requestsToSchedule.size();
        }
        return 0;
    }

    @Override
    public void processRequests(List<NotificationRequest> requests) {

        List<FeatureEntity> features = this.featureRepo
                .findByUrnIn(requests.stream().map(request -> request.getUrn()).collect(Collectors.toList()));
        List<NotificationActionEvent> notifications = new ArrayList<NotificationActionEvent>();
        for (FeatureEntity entity : features) {
            if (entity.getLastUpdate().equals(entity.getCreationDate())) {
                notifications.add(NotificationActionEvent.build(gson.toJsonTree(entity.getFeature()), "CREATION"));
            } else {
                notifications.add(NotificationActionEvent.build(gson.toJsonTree(entity.getFeature()), "UPDATE"));
            }
        }
        publisher.publish(notifications);

        this.notificationRequestRepo.deleteAll(requests);
    }

}
