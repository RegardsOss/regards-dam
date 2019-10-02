package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.authentication.IAuthenticationResolver;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
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

	@Override
	public void handleFeatureCreationRequestEvents(List<FeatureCreationRequestEvent> items) {

		// save a list of validated FeatureCreationRequest from a list of FeatureCreationRequestEvent
		List<FeatureCreationRequest> savedFCRE = featureCreationRequestRepo.saveAll(
				items.stream().map(fcre ->  FeatureCreationRequest.build(fcre.getFeature(), fcre.getRequestId()))
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
		this.featureRepo.saveAll(features.stream()
				.map(feature -> FeatureEntity.build(feature, OffsetDateTime.now())).collect(Collectors.toList()));
		this.featureCreationRequestRepo
				.deleteByIdIn(featureCreationRequests.stream().map(fcr -> fcr.getId()).collect(Collectors.toList()));
	}
}
