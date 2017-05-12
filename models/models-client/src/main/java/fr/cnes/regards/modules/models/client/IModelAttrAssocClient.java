/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.client;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@RestClient(name = "rs-dam")
@RequestMapping(IModelAttrAssocClient.BASE_MAPPING)
public interface IModelAttrAssocClient {

    public static final String BASE_MAPPING = "/models";

    public static final String TYPE_MAPPING = "/{pModelId}/attributes";

    public static final String ASSOCS_MAPPING = "/assocs";

    @RequestMapping(method = RequestMethod.GET, path = TYPE_MAPPING)
    public ResponseEntity<List<Resource<ModelAttrAssoc>>> getModelAttrAssocs(@PathVariable Long pModelId);

    @RequestMapping(path = ASSOCS_MAPPING, method = RequestMethod.GET)
    public ResponseEntity<Collection<ModelAttrAssoc>> getModelAttrAssocsFor(
            @RequestParam(name = "type", required = false) EntityType type);

}
