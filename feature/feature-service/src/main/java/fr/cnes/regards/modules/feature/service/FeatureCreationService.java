package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureMetadataEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCollection;
import fr.cnes.regards.modules.feature.dto.FeatureFile;
import fr.cnes.regards.modules.feature.dto.FeatureFileAttributes;
import fr.cnes.regards.modules.feature.dto.FeatureFileLocation;
import fr.cnes.regards.modules.feature.dto.FeatureMetadata;
import fr.cnes.regards.modules.feature.dto.StorageMetadata;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;
import fr.cnes.regards.modules.feature.service.job.FeatureCreationJob;
import fr.cnes.regards.modules.feature.service.job.FeatureJobPriority;
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
public class FeatureCreationService implements IFeatureCreationService {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureCreationService.class);

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
    public List<FeatureCreationRequest> registerRequests(List<FeatureCreationRequestEvent> events) {

        List<FeatureCreationRequest> grantedRequests = new ArrayList<>();
        events.forEach(item -> prepareFeatureCreationRequest(item, grantedRequests));

        // Save a list of validated FeatureCreationRequest from a list of
        // FeatureCreationRequestEvent
        return featureCreationRequestRepo.saveAll(grantedRequests);
    }

    @Override
    public List<FeatureCreationRequest> registerRequests(FeatureCollection collection) {
        // FIXME KMS : sans doute changer l'objet retourné pour avoir la liste des requests DENIED & GRANTED
        return null;
    }

    @Override
    public void scheduleRequests() {

        // Shedule job
        Set<JobParameter> jobParameters = Sets.newHashSet();
        Set<String> featureIdsScheduled = new HashSet<>();
        List<FeatureCreationRequest> requestsToSchedule = new ArrayList<FeatureCreationRequest>();

        Page<FeatureCreationRequest> page = this.featureCreationRequestRepo
                .findByStep(FeatureRequestStep.LOCAL_DELAYED,
                            PageRequest.of(0, properties.getMaxBulkSize(), Sort.by(Order.desc("registrationDate"))));

        if (page.hasContent()) {
            for (FeatureCreationRequest request : page) {
                // we will schedule only one feature request for a feature id
                if (!featureIdsScheduled.contains(request.getFeature().getId())) {
                    requestsToSchedule.add(request);
                    request.setStep(FeatureRequestStep.LOCAL_SCHEDULED);
                    featureIdsScheduled.add(request.getFeature().getId());
                }
            }

            // update FeatureCreationRequest scheduled state
            this.featureCreationRequestRepo
                    .updateState(RequestState.GRANTED,
                                 requestsToSchedule.stream().map(fcr -> fcr.getId()).collect(Collectors.toSet()));
            jobParameters.add(new JobParameter(FeatureCreationJob.IDS_PARAMETER,
                    requestsToSchedule.stream().map(fcr -> fcr.getId()).collect(Collectors.toList())));

            JobInfo jobInfo = new JobInfo(false, FeatureJobPriority.FEATURE_CREATION_JOB_PRIORITY.getPriority(),
                    jobParameters, authResolver.getUser(), FeatureCreationJob.class.getName());
            jobInfoService.createAsQueued(jobInfo);
        }
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
        FeatureMetadataEntity metadata = FeatureMetadataEntity.build(item.getMetadata().getSession(),
                                                                     item.getMetadata().getSessionOwner(),
                                                                     item.getMetadata().getStorages());
        FeatureCreationRequest request = FeatureCreationRequest.build(item.getRequestId(), item.getRequestDate(),
                                                                      RequestState.GRANTED, null, item.getFeature(),
                                                                      metadata, FeatureRequestStep.LOCAL_DELAYED);
        // Publish GRANTED request
        publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
                                                    item.getFeature() != null ? item.getFeature().getId() : null, null,
                                                    RequestState.GRANTED, null));
        // Add to granted request collection
        grantedRequests.add(request);
    }

    @Override
    public void processRequests(List<FeatureCreationRequest> requests) {

        // Register feature to insert
        this.featureRepo
                .saveAll(requests.stream().map(feature -> initFeatureEntity(feature)).collect(Collectors.toList()));
        // update fcr with feature setted for each of them + publish files to storage
        this.featureCreationRequestRepo.saveAll(requests.stream()
                .filter(fcr -> (fcr.getFeature().getFiles() != null) && fcr.getFeature().getFiles().isEmpty())
                .map(fcr -> publishFiles(fcr)).collect(Collectors.toList()));
        // delete fcr without files
        this.featureCreationRequestRepo.deleteByIdIn(requests.stream()
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
                if (!fcr.getMetadata().hasStorage()) {
                    this.storageClient.reference(FileReferenceRequestDTO
                            .build(attribute.getFilename(), attribute.getChecksum(), attribute.getAlgorithm(),
                                   attribute.getMimeType().toString(), fcr.getFeature().getUrn().getOrder(),
                                   loc.getUrl(), loc.getStorage(), loc.getUrl()));
                }
                for (StorageMetadata metadata : fcr.getMetadata().getStorages()) {
                    if (loc.getStorage() == null) {
                        this.storageClient.store(FileStorageRequestDTO
                                .build(attribute.getFilename(), attribute.getChecksum(), attribute.getAlgorithm(),
                                       attribute.getMimeType().toString(), fcr.getFeature().getUrn().toString(),
                                       loc.getUrl(), metadata.getPluginBusinessId(), Optional.of(loc.getUrl())));
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

        UUID uuid = UUID.nameUUIDFromBytes(feature.getId().getBytes());
        feature.setUrn(FeatureUniformResourceName
                .build(FeatureIdentifier.FEATURE, feature.getEntityType(), runtimeTenantResolver.getTenant(), uuid,
                       computeNextVersion(featureRepo
                               .findTop1VersionByProviderIdOrderByVersionAsc(fcr.getFeature().getId()))));

        FeatureEntity created = FeatureEntity.build(fcr.getMetadata().getSession(), fcr.getMetadata().getSessionOwner(),
                                                    feature, OffsetDateTime.now());
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
    public String publishFeature(Feature toPublish, FeatureMetadata metadata) {
        FeatureCreationRequestEvent event = FeatureCreationRequestEvent.build(metadata, toPublish);
        publisher.publish(event);
        return event.getRequestId();
    }

    @Override
    @Deprecated // FIXME à supprimer après recablage vers register
    public List<FeatureCreationRequest> createFeatureRequestEvent(FeatureCollection toHandle) {
        List<FeatureCreationRequestEvent> toTreat = new ArrayList<FeatureCreationRequestEvent>();

        for (Feature feature : toHandle.getFeatures()) {
            toTreat.add(FeatureCreationRequestEvent.build(toHandle.getMetadata(), feature));
        }
        return this.registerRequests(toTreat);
    }
}
