/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.signature;

import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.modules.collections.domain.Collection;

public interface CollectionsSignature {

    @RequestMapping(value = "/collections", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<List<Resource<Collection>>> retrieveCollectionList();

    @RequestMapping(value = "/collections/model/{model_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<List<Resource<Collection>>> retrieveCollectionListByModelId(@PathVariable("model_id") Long pModelId);

}
