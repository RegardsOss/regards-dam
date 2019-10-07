/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureState;
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

		// update feature status to stored
		this.featureRepo.updateStateByRequestIdIn(FeatureState.STORAGE_OK,
				request.stream().map(fcre -> fcre.getFeatureEntity().getId()).collect(Collectors.toSet()));

		// publish success notification for all request id
		request.stream().forEach(item -> publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
				item.getFeature() != null ? item.getFeature().getId() : null, null, RequestState.SUCCESS, null)));

		this.fcrRepo.deleteAll(request);
	}

	@Override
	public void handleError(String groupId) {
		List<FeatureCreationRequest> request = this.fcrRepo.findByGroupId(groupId);

		// update feature status to stored
		this.featureRepo.updateStateByRequestIdIn(FeatureState.STORAGE_ERROR,
				request.stream().map(fcre -> fcre.getFeatureEntity().getId()).collect(Collectors.toSet()));

		// publish success notification for all request id
		request.stream().forEach(item -> publisher.publish(FeatureRequestEvent.build(item.getRequestId(),
				item.getFeature() != null ? item.getFeature().getId() : null, null, RequestState.ERROR, null)));

		request.stream().forEach(item -> item.setState(RequestState.ERROR));
		this.fcrRepo.saveAll(request);

	}

}
