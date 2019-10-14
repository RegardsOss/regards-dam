package fr.cnes.regards.modules.feature.service;

import java.util.List;

import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureMetadataDto;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;

public interface IFeatureService {

    /**
     * Prepare a Job to create and save a list of {@link FeatureEntity} and
     * {@link FeatureCreationRequest}
     *
     * @param items list of {@link FeatureCreationRequestEvent}
     */
    void handleFeatureCreationRequestEvents(List<FeatureCreationRequestEvent> items);

    void createFeatures(List<FeatureCreationRequest> featureCreationRequests);

    String publishFeature(Feature toPublish, List<FeatureMetadataDto> metadata);

    /**
     * Schedule a list of {@link FeatureCreationRequest} in a job
     * Inside this list there is only one occurence {@link FeatureCreationRequest} per {@link Feature} id
     */
    void scheduleFeatureCreationRequest();
}