/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureState;
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

	@Override
	public void handleSuccess(String groupId) {
		Set<FeatureCreationRequest> request = this.fcrRepo.findByGroupId(groupId);
		this.featureRepo.updateStateByRequestIdIn(FeatureState.STORAGE_OK,
				request.stream().map(fcre -> fcre.getFeatureEntity().getId()).collect(Collectors.toSet()));

		this.fcrRepo.deleteAll(request);
	}

}
