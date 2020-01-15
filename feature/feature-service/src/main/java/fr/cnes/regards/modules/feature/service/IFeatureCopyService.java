package fr.cnes.regards.modules.feature.service;

import java.util.List;

import fr.cnes.regards.modules.feature.domain.request.FeatureCopyRequest;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;

/**
 * Service to register/schedule/treat {@link FeatureCopyRequest}
 * @author Kevin Marchois
 *
 */
public interface IFeatureCopyService {

    /**
     * Register copy requests in database for further processing from incoming request events
     */
    RequestInfo<FeatureUniformResourceName> registerRequests(List<FeatureCopyRequest> requests);

    /**
     * Schedule a job to process a batch of requests<br/>
     * @return number of scheduled requests (0 if no request was scheduled)
     */
    int scheduleRequests();

    /**
     * Process batch of requests during job
     */
    void processRequests(List<FeatureCopyRequest> requests);

}