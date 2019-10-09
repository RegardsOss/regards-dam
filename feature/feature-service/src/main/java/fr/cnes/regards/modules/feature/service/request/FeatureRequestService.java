/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.repository.FeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.repository.FeatureEntityRepository;

/**
 *
 * @author kevin
 *
 */
@Service
@MultitenantTransactional
public class FeatureRequestService implements IFeatureRequestService {

	@Autowired
	private FeatureCreationRequestRepository fcrRepo;

	@Autowired
	private FeatureEntityRepository featureRepo;

	@Autowired
	private IPublisher publisher;

	@Override
	public void handleSuccess(String groupId) {
		List<FeatureCreationRequest> request = this.fcrRepo.findByGroupId(groupId);

		// publish success notification for all request id
		request.stream().forEach(item -> publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
				item.getFeature() != null ? item.getFeature().getId() : null, null, RequestState.SUCCESS, null)));
		Set<FeatureEntity> entitiesToUpdate = request.stream().map(fcre -> fcre.getFeatureEntity())
				.collect(Collectors.toSet());
		// set succes state to all FeatureEntity
		entitiesToUpdate.stream().forEach(feature -> feature.setState(FeatureRequestStep.REMOTE_STORAGE_SUCCESS));

		// delete useless FeatureCreationRequest
		this.fcrRepo.deleteAll(request);
		this.featureRepo.saveAll(entitiesToUpdate);
	}

	@Override
	public void handleError(String groupId) {
		List<FeatureCreationRequest> request = this.fcrRepo.findByGroupId(groupId);

		// publish success notification for all request id
		request.stream().forEach(item -> publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
				item.getFeature() != null ? item.getFeature().getId() : null, null, RequestState.ERROR, null)));
		// set FeatureCreationRequest to error state
		request.stream().forEach(item -> item.setState(RequestState.ERROR));
		Set<FeatureEntity> entitiesToUpdate = request.stream().map(fcre -> fcre.getFeatureEntity())
				.collect(Collectors.toSet());
		// set FeatureEntity to error state
		entitiesToUpdate.stream().forEach(feature -> feature.setState(FeatureRequestStep.REMOTE_STORAGE_ERROR));

		this.fcrRepo.saveAll(request);
		this.featureRepo.saveAll(entitiesToUpdate);

	}

}
