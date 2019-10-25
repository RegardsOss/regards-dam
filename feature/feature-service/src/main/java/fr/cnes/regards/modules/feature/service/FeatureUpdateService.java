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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.authentication.IAuthenticationResolver;
import fr.cnes.regards.framework.geojson.GeoJsonType;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.validation.ErrorTranslator;
import fr.cnes.regards.framework.modules.jobs.domain.JobInfo;
import fr.cnes.regards.framework.modules.jobs.domain.JobParameter;
import fr.cnes.regards.framework.modules.jobs.service.IJobInfoService;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureUpdateRequestRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.FeatureUpdateRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCollection;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureUpdateRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;
import fr.cnes.regards.modules.feature.service.job.FeatureJobPriority;
import fr.cnes.regards.modules.feature.service.job.FeatureUpdateJob;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
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

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private IFeatureUpdateRequestRepository featureUpdateRequestRepo;

    @Override
    public List<FeatureUpdateRequest> registerRequests(List<FeatureUpdateRequestEvent> events,
            Set<FeatureUniformResourceName> grantedUrn, Multimap<FeatureUniformResourceName, String> errorByUrn) {
        List<FeatureUpdateRequest> grantedRequests = new ArrayList<>();
        events.forEach(item -> prepareFeatureUpdateRequest(item, grantedRequests, errorByUrn));

        grantedRequests.stream().forEach(request -> grantedUrn.add(request.getUrn()));

        // Batch save
        return updateRepo.saveAll(grantedRequests);

    }

    /**
     * Validate, save and publish a new request
     *
     * @param item            request to manage
     * @param grantedRequests collection of granted requests to populate
     * @param errorByUrn
     */
    private void prepareFeatureUpdateRequest(FeatureUpdateRequestEvent item, List<FeatureUpdateRequest> grantedRequests,
            Multimap<FeatureUniformResourceName, String> errorByUrn) {

        // Validate event
        Errors errors = new MapBindingResult(new HashMap<>(), FeatureUpdateRequestEvent.class.getName());
        validator.validate(item, errors);

        if (errors.hasErrors()) {
            // Publish DENIED request (do not persist it in DB)
            publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
                                                        item.getFeature() != null ? item.getFeature().getId() : null,
                                                        null, RequestState.DENIED, ErrorTranslator.getErrors(errors)));
            errorByUrn.putAll(item.getFeature().getUrn(), ErrorTranslator.getErrors(errors));
            return;
        }

        // Validate feature according to the data model
        errors = validationService.validate(item.getFeature(), ValidationMode.PATCH);

        if (errors.hasErrors()) {
            publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
                                                        item.getFeature() != null ? item.getFeature().getId() : null,
                                                        item.getFeature() != null ? item.getFeature().getUrn() : null,
                                                        RequestState.DENIED, ErrorTranslator.getErrors(errors)));
            errorByUrn.putAll(item.getFeature().getUrn(), ErrorTranslator.getErrors(errors));
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
    public void scheduleRequests() {

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

            JobInfo jobInfo = new JobInfo(false, FeatureJobPriority.FEATURE_UPDATE_JOB_PRIORITY.getPriority(),
                    jobParameters, authResolver.getUser(), FeatureUpdateJob.class.getName());
            jobInfoService.createAsQueued(jobInfo);
        }
    }

    @Override
    public void processRequests(List<FeatureUpdateRequest> requests) {

        List<FeatureEntity> entities = new ArrayList<>();

        // Update feature
        for (FeatureUpdateRequest request : requests) {

            Feature patch = request.getFeature();

            // Retrieve feature from db
            // Note : entity is attached to transaction manager so all changes will be reflected in the db!
            FeatureEntity entity = featureRepo.findByUrn(patch.getUrn());
            entity.setLastUpdate(OffsetDateTime.now());

            // Merge properties handling null property values to unset properties
            IProperty.mergeProperties(entity.getFeature().getProperties(), patch.getProperties());

            // Geometry cannot be unset but can be mutated
            if (!GeoJsonType.UNLOCATED.equals(patch.getGeometry().getType())) {
                entity.getFeature().setGeometry(patch.getGeometry());
            }

            // Publish request success
            // FIXME does not manage storage metadata at the moment
            publisher.publish(FeatureRequestEvent.build(request.getRequestId(), entity.getProviderId(), entity.getUrn(),
                                                        RequestState.SUCCESS));

            // Register
            entities.add(entity);
        }

        featureRepo.saveAll(entities);
        featureUpdateRequestRepo.deleteInBatch(requests);
    }

    @Override
    public RequestInfo<FeatureUniformResourceName> registerScheduleProcess(@Valid FeatureCollection toHandle) {
        List<FeatureUpdateRequestEvent> toTreat = new ArrayList<FeatureUpdateRequestEvent>();
        Set<FeatureUniformResourceName> grantedRequestId = new HashSet<FeatureUniformResourceName>();
        Multimap<FeatureUniformResourceName, String> errorbyRequestId = ArrayListMultimap.create();
        Map<String, FeatureUniformResourceName> requestIdByFeature = new HashMap<String, FeatureUniformResourceName>();
        RequestInfo<FeatureUniformResourceName> requestInfo = new RequestInfo<FeatureUniformResourceName>();

        // build FeatureUpdateEvent
        for (Feature feature : toHandle.getFeatures()) {
            toTreat.add(FeatureUpdateRequestEvent.build(feature, OffsetDateTime.now()));
        }

        // extract from generated FeatureUpdaterequest a map feature id => URN
        this.registerRequests(toTreat, grantedRequestId, errorbyRequestId).stream()
                .forEach(fcr -> requestIdByFeature.put(fcr.getFeature().getId(), fcr.getFeature().getUrn()));

        requestInfo.setIdByFeatureId(requestIdByFeature);
        requestInfo.setGrantedId(grantedRequestId);
        requestInfo.setErrorById(errorbyRequestId);

        return requestInfo;
    }
}
