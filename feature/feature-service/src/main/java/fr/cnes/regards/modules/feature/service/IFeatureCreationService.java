package fr.cnes.regards.modules.feature.service;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Multimap;

import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCreationCollection;
import fr.cnes.regards.modules.feature.dto.FeatureMetadata;
import fr.cnes.regards.modules.feature.dto.FeatureUpdateCollection;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

public interface IFeatureCreationService {

    /**
     * Register creation requests in database for further processing from incoming request events
     */
    List<FeatureCreationRequest> registerRequests(List<FeatureCreationRequestEvent> events,
            Set<String> grantedRequestId, Multimap<String, String> errorByRequestId);

    /**
     * Schedule a job to process a batch of requests<br/>
     * Inside this list there is only one occurence of {@link FeatureCreationRequest} per {@link Feature} id
     */
    void scheduleRequests();

    /**
     * Process batch of requests during job
     */
    void processRequests(List<FeatureCreationRequest> requests);

    String publishFeature(Feature toPublish, FeatureMetadata metadata);

    /**
     * Create a list of {@link FeatureCreationRequest} from a list of {@link Feature} stored in a {@link FeatureCreationCollection}
     * and return a {@link RequestInfo} full of request ids and occured errors
     * @param toHandle {@link FeatureUpdateCollection} it contain all {@link Feature} to handle
     * @return {@link RequestInfo}
     */
    RequestInfo<String> registerScheduleProcess(FeatureCreationCollection toHandle);
}