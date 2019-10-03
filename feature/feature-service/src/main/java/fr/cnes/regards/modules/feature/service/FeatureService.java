package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
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
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.repository.FeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.repository.FeatureEntityRepository;
import fr.cnes.regards.modules.feature.service.job.FeatureCreationJob;
import fr.cnes.regards.modules.feature.service.job.feature.FeatureJobPriority;
import fr.cnes.regards.modules.ingest.domain.request.IngestRequest;
import fr.cnes.regards.modules.ingest.domain.request.IngestRequestStep;

/**
 * Feature service management
 *
 * @author Kevin Marchois
 */
@Service
@MultitenantTransactional
public class FeatureService implements IFeatureService {

    @Autowired
    private FeatureCreationRequestRepository featureCreationRequestRepo;

    @Autowired
    private IAuthenticationResolver authResolver;

    @Autowired
    private IJobInfoService jobInfoService;

    @Autowired
    private FeatureEntityRepository featureRepo;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private IFeatureValidationService validationService;

    @Autowired
    private Validator validator;

    @Override
    public void handleFeatureCreationRequestEvents(List<FeatureCreationRequestEvent> items) {

        // save a list of validated FeatureCreationRequest from a list of FeatureCreationRequestEvent
        List<FeatureCreationRequest> savedFCRE = featureCreationRequestRepo.saveAll(items.stream()
                .map(fcre -> FeatureCreationRequest.build(fcre.getFeature(), fcre.getRequestId()))
                .filter(fcre -> validateFCR(fcre)).collect(Collectors.toList()));

        // TODO remonter validation
        // TODO notifier request DENIED or GRANTED

        Set<JobParameter> jobParameters = Sets.newHashSet();
        jobParameters.add(new JobParameter(FeatureCreationJob.IDS_PARAMETER,
                savedFCRE.stream().map(fcr -> fcr.getId()).collect(Collectors.toList())));

        JobInfo jobInfo = new JobInfo(false, FeatureJobPriority.FEATURE_CREATION_JOB_PRIORITY.getPriority(),
                jobParameters, authResolver.getUser(), FeatureCreationJob.class.getName());
        jobInfoService.createAsQueued(jobInfo);
    }

    /**
     * Validate, save and publish a new request
     * @param item request to manage
     * @param grantedRequests collection of granted requests to populate
     */
    private void registerFeatureCreationRequest(FeatureCreationRequestEvent item,
            Collection<FeatureCreationRequest> grantedRequests) {

        // Validate all elements of the flow item
        Errors errors = new MapBindingResult(new HashMap<>(), FeatureCreationRequestEvent.class.getName());
        validator.validate(item, errors);
        if (errors.hasErrors()) {
            Set<String> errs = ErrorTranslator.getErrors(errors);
            // Publish DENIED request (do not persist it in DB)
            ingestRequestService.handleRequestDenied(IngestRequest
                    .build(item.getRequestId(), metadataMapper.dtoToMetadata(item.getMetadata()), RequestState.DENIED,
                           IngestRequestStep.LOCAL_DENIED, null, errs));
            if (LOGGER.isDebugEnabled()) {
                StringJoiner joiner = new StringJoiner(", ");
                errs.forEach(err -> joiner.add(err));
                LOGGER.debug("Ingest request {} rejected for following reason(s) : {}", item.getRequestId(),
                             joiner.toString());
            }
            return;
        }

        // Save granted ingest request
        IngestRequest request = IngestRequest
                .build(item.getRequestId(), metadataMapper.dtoToMetadata(item.getMetadata()), RequestState.GRANTED,
                       IngestRequestStep.LOCAL_SCHEDULED, item.getSip());
        ingestRequestService.handleRequestGranted(request);
        // Add to granted request collection
        grantedRequests.add(request);
    }

    private boolean validateFCR(FeatureCreationRequest toValidate) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        if (validator.validate(toValidate).isEmpty()) {
            toValidate.setState(RequestState.GRANTED);
            return true;
        }
        toValidate.setState(RequestState.DENIED);
        return false;
    }

    @Override
    public void createFeatures(Set<Feature> features, List<FeatureCreationRequest> featureCreationRequests) {
        // Prepare feature
        // TODO delegate to feature service : validation / détection des doublons

        // Register feature to insert

        //TODO validation
        //TODO notif
        this.featureRepo.saveAll(features.stream().map(feature -> FeatureEntity.build(feature, OffsetDateTime.now()))
                .collect(Collectors.toList()));
        this.featureCreationRequestRepo
                .deleteByIdIn(featureCreationRequests.stream().map(fcr -> fcr.getId()).collect(Collectors.toList()));
    }

    @Override
    public String publishFeature(Feature toPublish) {
        FeatureCreationRequestEvent event = FeatureCreationRequestEvent.builder(toPublish);
        publisher.publish(event);
        return event.getRequestId();
    }
}
