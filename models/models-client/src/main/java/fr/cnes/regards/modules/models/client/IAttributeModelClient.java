/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.client;

import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;

/**
 * Feign client handling {@link AttributeModel}s
 * @author Xavier-Alexandre Brochard
 */
@RestClient(name = "rs-dam")
@RequestMapping(value = IAttributeModelClient.PATH, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IAttributeModelClient { //NOSONAR

    /**
     * Mapping
     */
    public static final String PATH = "/models/attributes";

    /**
     * Request parameter : attribute type
     */
    public static final String PARAM_TYPE = "type";

    /**
     * Request parameter : fragement name
     */
    public static final String PARAM_FRAGMENT_NAME = "fragmentName";

    /**
     * Get the list of {@link AttributeModel}
     * @param pType the type to filter on
     * @param pFragmentName the fragment to filter on
     * @return the list wrapped in an HTTP response
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = PARAM_TYPE, required = false) AttributeType pType,
            @RequestParam(value = PARAM_FRAGMENT_NAME, required = false) String pFragmentName);

}
