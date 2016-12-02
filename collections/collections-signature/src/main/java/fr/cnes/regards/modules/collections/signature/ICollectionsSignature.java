/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.signature;

import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.modules.collections.domain.Collection;

@RequestMapping(value = "/collections")
public interface ICollectionsSignature {

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<List<Resource<Collection>>> retrieveCollectionList();

    @RequestMapping(value = "/model/{model_id}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<List<Resource<Collection>>> retrieveCollectionListByModelId(@PathVariable("model_id") Long pModelId);

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Resource<Collection>> createCollection(@RequestBody Collection pCollection);

    @RequestMapping(method = RequestMethod.GET, value = "/{collection_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Resource<Collection>> retrieveCollection(@PathVariable("collection_id") Long pCollectionId);

    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Resource<Collection>> updateCollection(@PathVariable("collection_id") Long pCollectionId,
            @RequestBody Collection pCollection) throws EntityInconsistentIdentifierException;

    @RequestMapping(method = RequestMethod.DELETE, value = "/{collection_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Void> deleteCollection(@PathVariable("collection_id") Long pCollectionId);

}
