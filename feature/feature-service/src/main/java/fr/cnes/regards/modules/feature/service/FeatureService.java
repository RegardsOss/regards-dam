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
import fr.cnes.regards.modules.feature.dto.FeatureMetadataDto;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureState;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.repository.FeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.repository.FeatureEntityRepository;
import fr.cnes.regards.modules.feature.service.job.FeatureCreationJob;
import fr.cnes.regards.modules.feature.service.job.feature.FeatureJobPriority;
import fr.cnes.regards.modules.model.service.validation.ValidationMode;
import fr.cnes.regards.modules.storagelight.client.IStorageClient;

/**
 * Feature service management
 *
 * @author Kevin Marchois
 */
@Service
@MultitenantTransactional
public class FeatureService implements IFeatureService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureService.class);

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

	@Autowired
	private IStorageClient storageClient;

	@Override
	public void handleFeatureCreationRequestEvents(List<FeatureCreationRequestEvent> items) {

		List<FeatureCreationRequest> grantedRequests = new ArrayList<>();
		items.forEach(item -> prepareFeatureCreationRequest(item, grantedRequests));

		// Save a list of validated FeatureCreationRequest from a list of
		// FeatureCreationRequestEvent
		List<FeatureCreationRequest> savedFCRE = featureCreationRequestRepo.saveAll(grantedRequests);

		// Shedule job
		Set<JobParameter> jobParameters = Sets.newHashSet();
		jobParameters.add(new JobParameter(FeatureCreationJob.IDS_PARAMETER,
				savedFCRE.stream().map(fcr -> fcr.getId()).collect(Collectors.toList())));

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
					item.getFeature() != null ? item.getFeature().getId() : null, null, RequestState.DENIED,
					ErrorTranslator.getErrors(errors)));
			return;
		}

		// Validate feature according to the data model
		errors = validationService.validate(item.getFeature(), ValidationMode.CREATION);
		if (errors.hasErrors()) {
			publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
					item.getFeature() != null ? item.getFeature().getId() : null, null, RequestState.DENIED,
					ErrorTranslator.getErrors(errors)));
			return;
		}

		// Manage granted request
		FeatureCreationRequest request = FeatureCreationRequest.build(item.getRequestId(), item.getRequestTime(),
				RequestState.DENIED, null, item.getFeature(), item.getMetadata());
		// Publish GRANTED request
		publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
				item.getFeature() != null ? item.getFeature().getId() : null, null, RequestState.GRANTED, null));
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
		if ((fcr.getMetadata() != null) && fcr.getMetadata().isEmpty()) {

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
		FeatureEntity created = FeatureEntity.build(fcr.getFeature(), OffsetDateTime.now(),
				FeatureState.STORAGE_REQUESTED);
		fcr.setFeatureEntity(created);
		return created;
	}

	@Override
	public String publishFeature(Feature toPublish, List<FeatureMetadataDto> metadata) {
		FeatureCreationRequestEvent event = FeatureCreationRequestEvent.builder(toPublish, metadata);
		publisher.publish(event);
		return event.getRequestId();
	}

}
