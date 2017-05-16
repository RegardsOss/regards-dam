/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.client;

import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@RestClient(name = "rs-dam")
@RequestMapping(IModelClient.TYPE_MAPPING)
public interface IModelClient {

    public static final String TYPE_MAPPING = "/models";

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<Model>>> getModels(
            @RequestParam(value = "type", required = false) EntityType pType);
}
