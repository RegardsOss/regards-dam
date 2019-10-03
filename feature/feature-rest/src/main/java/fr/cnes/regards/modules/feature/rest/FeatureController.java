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
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.service.IFeatureService;

/**
 * Controller REST handling requests about {@link Feature}s 
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
	 * @param toPublish {@link Feature} to publish
	 * @return a request id
	 */
	@RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "Public a feature and return the request id")
    public ResponseEntity<Resource<Feature>> createAccessGroup(@Valid @RequestBody Feature toPublish) {
        return new ResponseEntity<>(toResource(toPublish, featureService.publishFeature(toPublish)), HttpStatus.CREATED);
    }
	
	@Override
	public Resource<Feature> toResource(Feature element, Object... extras) {
        Resource<Feature> resource = resourceService.toResource(element);
        return resource;
	}

}
