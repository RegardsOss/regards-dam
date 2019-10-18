package fr.cnes.regards.modules.feature.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCollection;
import fr.cnes.regards.modules.feature.dto.FeatureMetadataWrapper;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.service.IFeatureService;

/**
 * Controller REST handling requests about {@link Feature}s
 *
 * @author Kevin Marchois
 */
@RestController
@RequestMapping(FeatureController.PATH_FEATURE)
public class FeatureController implements IResourceController<Feature> {

    public final static String PATH_FEATURE = "/feature";

    @Autowired
    private IFeatureService featureService;

    @Autowired
    private IResourceService resourceService;

    /**
     * Receive a feature publish it and return the request id
     *
     * @param toPublish {@link FeatureMetadataWrapper} to publish
     * @return a request id
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "Public a feature and return the request id")
    public ResponseEntity<Resource<Feature>> publishFeature(@Valid @RequestBody FeatureMetadataWrapper toPublish) {
        return new ResponseEntity<>(
                toResource(toPublish.getFeature(), featureService
                        .publishFeature(toPublish.getFeature(), toPublish.getMetada(), toPublish.getSession())),
                HttpStatus.CREATED);
    }

    /**
     * Receive a {@link FeatureCollection} and create for each of them a {@link FeatureCreationRequestEvent}
     * used to create a {@link FeatureCreationRequest} we will return all {@link Feature} of those
     * {@link FeatureCreationRequest} with their request id
     * @param toHandle {@link FeatureCollection} contain a list of {@link Feature}
     * @return list of created request ids
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "Public a feature and return the request id")
    public ResponseEntity<List<Resource<Feature>>> createFeatures(@Valid @RequestBody FeatureCollection toHandle) {
        List<Feature> createdFeature = new ArrayList<Feature>();
        List<String> requestIds = new ArrayList<String>();

        List<FeatureCreationRequest> createdEvents = this.featureService.createFeatureRequestEvent(toHandle);

        // extract the list of feature concerned by a feature creation request
        // and extract their request id in the same time
        createdEvents.stream().forEach(fcr -> {
            createdFeature.add(fcr.getFeature());
            requestIds.add(fcr.getRequestId());
        });

        return new ResponseEntity<>(toResources(createdFeature, requestIds), HttpStatus.CREATED);
    }

    @Override
    public Resource<Feature> toResource(Feature element, Object... extras) {
        Resource<Feature> resource = resourceService.toResource(element);
        return resource;
    }

    @Override
    public List<Resource<Feature>> toResources(final Collection<Feature> features, final Object... extras) {
        return features.stream().map(resource -> toResource(resource, extras)).collect(Collectors.toList());
    }
}
