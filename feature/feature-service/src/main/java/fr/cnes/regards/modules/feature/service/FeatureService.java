package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.FeatureSession;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureFile;
import fr.cnes.regards.modules.feature.dto.FeatureFileAttributes;
import fr.cnes.regards.modules.feature.dto.FeatureFileLocation;
import fr.cnes.regards.modules.feature.dto.FeatureMetadataDto;
import fr.cnes.regards.modules.feature.dto.FeatureSessionDto;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;
import fr.cnes.regards.modules.feature.service.job.FeatureCreationJob;
import fr.cnes.regards.modules.feature.service.job.feature.FeatureJobPriority;
import fr.cnes.regards.modules.model.service.validation.ValidationMode;
import fr.cnes.regards.modules.storagelight.client.IStorageClient;
import fr.cnes.regards.modules.storagelight.domain.dto.request.FileReferenceRequestDTO;
import fr.cnes.regards.modules.storagelight.domain.dto.request.FileStorageRequestDTO;

/**
 * Feature service management
 *
 * @author Kevin Marchois
 */
@Service
@MultitenantTransactional
public class FeatureService implements IFeatureService {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureService.class);

    @Autowired
    private IFeatureCreationRequestRepository featureCreationRequestRepo;

    @Autowired
    private IAuthenticationResolver authResolver;

    @Autowired
    private IJobInfoService jobInfoService;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private IFeatureValidationService validationService;

    @Autowired
    private Validator validator;

    @Autowired
    private IStorageClient storageClient;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    FeatureConfigurationProperties properties;

    @Override
    public void handleFeatureCreationRequestEvents(List<FeatureCreationRequestEvent> items) {

        List<FeatureCreationRequest> grantedRequests = new ArrayList<>();
        items.forEach(item -> prepareFeatureCreationRequest(item, grantedRequests));

        // Save a list of validated FeatureCreationRequest from a list of
        // FeatureCreationRequestEvent
        featureCreationRequestRepo.saveAll(grantedRequests);

        scheduleFeatureCreationRequest();
    }

    @Override
    public void scheduleFeatureCreationRequest() {

        // Shedule job
        Set<JobParameter> jobParameters = Sets.newHashSet();
        Set<String> featureIdsScheduled = new HashSet<>();
        List<FeatureCreationRequest> requestsToSchedule = new ArrayList<FeatureCreationRequest>();
        for (FeatureCreationRequest request : this.featureCreationRequestRepo
                .findAll(PageRequest.of(0, properties.getMaxBulkSize(), Sort.by(Order.desc("registrationDate"))))) {
            // we will schedule only one feature request for a feature id
            if (!featureIdsScheduled.contains(request.getFeature().getId())) {
                requestsToSchedule.add(request);
                featureIdsScheduled.add(request.getFeature().getId());
            }
        }
        jobParameters.add(new JobParameter(FeatureCreationJob.IDS_PARAMETER,
                requestsToSchedule.stream().map(fcr -> fcr.getId()).collect(Collectors.toList())));

        JobInfo jobInfo = new JobInfo(false, FeatureJobPriority.FEATURE_CREATION_JOB_PRIORITY.getPriority(),
                jobParameters, authResolver.getUser(), FeatureCreationJob.class.getName());
        jobInfoService.createAsQueued(jobInfo);
    }

    /**
     * Validate, save and publish a new request
     *
     * @param item            request to manage
     * @param grantedRequests collection of granted requests to populate
     */
    private void prepareFeatureCreationRequest(FeatureCreationRequestEvent item,
            List<FeatureCreationRequest> grantedRequests) {

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
        errors = validationService.validate(item.getFeature(), ValidationMode.CREATION);
        if (errors.hasErrors()) {
            publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
                                                        item.getFeature() != null ? item.getFeature().getId() : null,
                                                        null, RequestState.DENIED, ErrorTranslator.getErrors(errors)));
            return;
        }

        // Manage granted request
        FeatureCreationRequest request = FeatureCreationRequest
                .build(item.getRequestId(), item.getRequestDate(), RequestState.GRANTED, null, item.getFeature(),
                       item.getMetadata(), FeatureRequestStep.LOCAL_DELAYED,
                       FeatureSession.builder(item.getSession().getSessionOwner(), item.getSession().getSession()));
        // Publish GRANTED request
        publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
                                                    item.getFeature() != null ? item.getFeature().getId() : null, null,
                                                    RequestState.GRANTED, null));
        // Add to granted request collection
        grantedRequests.add(request);
    }

    @Override
    public void createFeatures(List<FeatureCreationRequest> featureCreationRequests) {

        // Register feature to insert
        this.featureRepo.saveAll(featureCreationRequests.stream().map(feature -> initFeatureEntity(feature))
                .collect(Collectors.toList()));
        // update fcr with feature setted for each of them + publish files to storage
        this.featureCreationRequestRepo.saveAll(featureCreationRequests.stream()
                .filter(fcr -> (fcr.getFeature().getFiles() != null) && fcr.getFeature().getFiles().isEmpty())
                .map(fcr -> publishFiles(fcr)).collect(Collectors.toList()));
        // delete fcr without files
        this.featureCreationRequestRepo.deleteByIdIn(featureCreationRequests.stream()
                .filter(fcr -> (fcr.getFeature().getFiles() == null)
                        || ((fcr.getFeature().getFiles() != null) && fcr.getFeature().getFiles().isEmpty()))
                .map(fcr -> fcr.getId()).collect(Collectors.toList()));
    }

    /**
     * Publish all contained files inside the {@link FeatureCreationRequest} to
     * storage
     *
     * @param fcr
     * @return
     */
    private FeatureCreationRequest publishFiles(FeatureCreationRequest fcr) {
        for (FeatureFile file : fcr.getFeature().getFiles()) {
            FeatureFileAttributes attribute = file.getAttributes();
            for (FeatureFileLocation loc : file.getLocations()) {
                // there is no metadata but a file location so we will update reference
                if (fcr.getMetadata().isEmpty()) {
                    this.storageClient.reference(FileReferenceRequestDTO
                            .build(attribute.getFilename(), attribute.getChecksum(), attribute.getAlgorithm(),
                                   attribute.getMimeType().toString(), fcr.getFeature().getUrn().getOrder(),
                                   loc.getUrl(), loc.getStorage(), loc.getUrl()));
                }
                for (FeatureMetadataDto metadata : fcr.getMetadata()) {
                    if (loc.getStorage() == null) {
                        this.storageClient.store(FileStorageRequestDTO
                                .build(attribute.getFilename(), attribute.getChecksum(), attribute.getAlgorithm(),
                                       attribute.getMimeType().toString(), fcr.getFeature().getUrn().toString(),
                                       loc.getUrl(), metadata.getStorageIdentifier(), Optional.of(loc.getUrl())));
                    } else {
                        this.storageClient.reference(FileReferenceRequestDTO
                                .build(attribute.getFilename(), attribute.getChecksum(), attribute.getAlgorithm(),
                                       attribute.getMimeType().toString(), fcr.getFeature().getUrn().getOrder(),
                                       loc.getUrl(), loc.getStorage(), loc.getUrl()));
                    }
                }
            }
        }
        return fcr;
    }

    /**
     * Init a {@link FeatureEntity} from a {@link FeatureCreationRequest} and set it
     * as feature entity
     *
     * @param feature
     * @return
     */
    private FeatureEntity initFeatureEntity(FeatureCreationRequest fcr) {

        Feature feature = fcr.getFeature();

        feature.setUrn(FeatureUniformResourceName
                .pseudoRandomUrn(FeatureIdentifier.FEATURE, feature.getEntityType(), runtimeTenantResolver.getTenant(),
                                 computeNextVersion(featureRepo
                                         .findTop1VersionByProviderIdOrderByVersionAsc(fcr.getFeature().getId()))));

        FeatureEntity created = FeatureEntity.build(feature, OffsetDateTime.now(),
                                                    FeatureRequestStep.REMOTE_STORAGE_REQUESTED);
        created.setVersion(feature.getUrn().getVersion());
        fcr.setFeatureEntity(created);
        return created;
    }

    /**
     * Compute the next version for a specific provider id we will increment the version passed in parameter
     * a null parameter mean a first version
     * @param featureEntity last version for a provider id null if it not exist
     * @return the next version
     */
    private int computeNextVersion(FeatureEntity featureEntity) {
        return featureEntity == null ? 1 : featureEntity.getVersion() + 1;
    }

    @Override
    public String publishFeature(Feature toPublish, List<FeatureMetadataDto> metadata, FeatureSessionDto session) {
        FeatureCreationRequestEvent event = FeatureCreationRequestEvent.builder(toPublish, metadata,
                                                                                OffsetDateTime.now(), session);
        publisher.publish(event);
        return event.getRequestId();
    }
}
