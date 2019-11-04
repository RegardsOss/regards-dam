package fr.cnes.regards.modules.feature.service;

import java.util.List;

import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCreationCollection;
import fr.cnes.regards.modules.feature.dto.FeatureUpdateCollection;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

public interface IFeatureCreationService {

    /**
     * Register creation requests in database for further processing from incoming request events
     */
    RequestInfo<String> registerRequests(List<FeatureCreationRequestEvent> events);

    /**
     * Create a list of {@link FeatureCreationRequest} from a list of {@link Feature} stored in a {@link FeatureCreationCollection}
     * and return a {@link RequestInfo} full of request ids and occured errors
     * @param toHandle {@link FeatureUpdateCollection} it contain all {@link Feature} to handle
     * @return {@link RequestInfo}
     */
    RequestInfo<String> registerRequests(FeatureCreationCollection collection);

    /**
     * Schedule a job to process a batch of requests<br/>
     * Inside this list there is only one occurence of {@link FeatureCreationRequest} per {@link Feature} id
     * @return true if at least one request has been scheduled
     */
    boolean scheduleRequests();

    /**
     * Process batch of requests during job
     */
    void processRequests(List<FeatureCreationRequest> requests);

}