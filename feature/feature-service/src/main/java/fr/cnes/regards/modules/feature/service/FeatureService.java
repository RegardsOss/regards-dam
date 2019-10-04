package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

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
	private Validator validator;

	@Override
	public void handleFeatureCreationRequestEvents(List<FeatureCreationRequestEvent> items) {

		// save a list of validated FeatureCreationRequest from a list of
		// FeatureCreationRequestEvent
		List<FeatureCreationRequest> savedFCRE = featureCreationRequestRepo.saveAll(
				items.stream().map(fcre -> FeatureCreationRequest.build(fcre.getFeature(), fcre.getRequestId()))
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
		Errors errors = new MapBindingResult(new HashMap<>(), "searchContext");
		validator.validate(toValidate, errors);
		if (!errors.hasErrors()) {
			toValidate.setState(RequestState.GRANTED);
			return true;
		}
		toValidate.setState(RequestState.DENIED);
		return false;
	}

    @Override
    public void handleFeatureCreationRequestEvents(List<FeatureCreationRequestEvent> items) {

        // save a list of validated FeatureCreationRequest from a list of FeatureCreationRequestEvent
        List<FeatureCreationRequest> savedFCRE = featureCreationRequestRepo.saveAll(items.stream()
                .map(fcre -> FeatureCreationRequest.build(fcre.getFeature(), fcre.getRequestId()))
                .filter(fcre -> validateFCR(fcre)).collect(Collectors.toList()));

		// TODO validation
		// TODO notif
		this.featureRepo.saveAll(features.stream().map(feature -> FeatureEntity.build(feature, OffsetDateTime.now()))
				.collect(Collectors.toList()));
		this.featureCreationRequestRepo.deleteByIdIn(featureCreationRequests.stream()
				.filter(fcr -> (fcr.getFeature().getFiles() != null) && fcr.getFeature().getFiles().isEmpty())
				.map(fcr -> fcr.getId()).collect(Collectors.toList()));
	}

	@Override
	public String publishFeature(Feature toPublish) {
		FeatureCreationRequestEvent event = FeatureCreationRequestEvent.builder(toPublish);
		publisher.publish(event);
		return event.getRequestId();
	}
}
