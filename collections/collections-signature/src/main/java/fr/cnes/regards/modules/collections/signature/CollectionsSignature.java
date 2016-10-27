/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.signature;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.modules.collections.domain.Collection;

public interface CollectionsSignature {

    @GetMapping(value = "/collections", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<List<Resource<Collection>>> retrieveCollectionList();

    @GetMapping(value = "/collections/model/{model_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<List<Resource<Collection>>> retrieveCollectionListByModelId(@PathVariable("model_id") Long pModelId);

    @PostMapping(value = "/collections", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Resource<Collection>> createCollection(@RequestBody Collection pCollection);

    @GetMapping(value = "/collections/{collection_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Resource<Collection>> retrieveCollection(@PathVariable("collection_id") String pCollectionId);

    @PutMapping(value = "/collections/{collection_id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Resource<Collection>> updateCollection(@PathVariable("collection_id") String pCollectionId,
            @RequestBody Collection pCollection) throws OperationNotSupportedException;

    @DeleteMapping(value = "/collections/{collection_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Void> deleteCollection(@PathVariable("collection_id") String pCollectionId);

}
