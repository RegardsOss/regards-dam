/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 *
 * @author kevin
 *
 */
@Service
@MultitenantTransactional
public class FeatureRequestService implements IFeatureRequestService {

    @Autowired
    private IFeatureCreationRequestRepository fcrRepo;

    @Autowired
    private IFeatureEntityRepository featureRepo;

    @Autowired
    private IPublisher publisher;

    @Override
    public void handleSuccess(String groupId) {
        List<FeatureCreationRequest> request = this.fcrRepo.findByGroupId(groupId);

        // publish success notification for all request id
        request.stream()
                .forEach(item -> publisher.publish(FeatureRequestEvent
                        .build(item.getRequestId(), item.getFeature() != null ? item.getFeature().getId() : null, null,
                               RequestState.SUCCESS, null)));

        // delete useless FeatureCreationRequest
        this.fcrRepo.deleteAll(request);
    }

    @Override
    public void handleError(String groupId) {
        List<FeatureCreationRequest> request = this.fcrRepo.findByGroupId(groupId);

        // publish success notification for all request id
        request.stream()
                .forEach(item -> publisher.publish(FeatureRequestEvent
                        .build(item.getRequestId(), item.getFeature() != null ? item.getFeature().getId() : null, null,
                               RequestState.ERROR, null)));
        // set FeatureCreationRequest to error state
        request.stream().forEach(item -> item.setState(RequestState.ERROR));

        this.fcrRepo.saveAll(request);

    }

}
