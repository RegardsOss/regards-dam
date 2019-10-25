package fr.cnes.regards.modules.feature.rest;

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
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.service.IFeatureCreationService;

/**
 * Controller REST handling requests about {@link Feature}s
 *
 * @author Kevin Marchois
 */
@RestController
@RequestMapping(FeatureController.PATH_FEATURES)
public class FeatureController implements IResourceController<RequestInfo> {

    public final static String PATH_FEATURES = "/features";

    @Autowired
    private IFeatureCreationService featureCreationService;

    @Autowired
    private IResourceService resourceService;

    /**
     * Create a list of {@link FeatureCreationRequest} from a list of {@link Feature} stored in a {@link FeatureCollection}
     * and return a {@link RequestInfo} full of request ids and occured errors
     * @param toHandle {@link FeatureCollection} it contain all {@link Feature} to handle
     * @return {@link RequestInfo}
     */
    // FIXME KMS revoir API + documenter avec des tests IT
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "Public a feature and return the request id")
    public ResponseEntity<Resource<RequestInfo>> createFeatures(@Valid @RequestBody FeatureCollection toHandle) {

        RequestInfo infos = featureCreationService.registerScheduleProcess(toHandle);

        return new ResponseEntity<>(toResource(infos),
                infos.getGrantedRequestId().isEmpty() ? HttpStatus.CONFLICT : HttpStatus.CREATED);
    }

    // FIXME KMS ajouter API update

    @Override
    public Resource<RequestInfo> toResource(RequestInfo element, Object... extras) {
        Resource<RequestInfo> resource = resourceService.toResource(element);
        return resource;
    }
}
