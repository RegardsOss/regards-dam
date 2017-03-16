/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.client;

import java.util.List;
import java.util.Set;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Léo Mieulet
 */
@RestClient(name = "rs-dam")
@RequestMapping(value = "/collections", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface ICollectionsClient {

    /**
     * @summary Entry point to retrieve all collections
     * @return list of all {@link Collection}
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<List<Resource<Collection>>> retrieveCollections();

    /**
     * @summary Entry point to retrieve a {@link Collection} using its id
     * @param pCollectionId
     *            Identifier of the {@link Collection} wanted
     * @return the specified {@link Collection}
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{collection_id}")
    @ResponseBody
    public HttpEntity<Resource<Collection>> retrieveCollection(@PathVariable("collection_id") Long pCollectionId);

    /**
     *
     * @summary Entry point to update a {@link Collection} using its id
     * @param pCollectionId
     *            id of the {@link Collection} to update
     * @param pCollection
     *            updated {@link Collection}
     * @return Updated {@link Collection}
     * @throws EntityInconsistentIdentifierException
     *             thrown if Ids mismatch
     *
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}")
    @ResponseBody
    public HttpEntity<Resource<Collection>> updateCollection(@PathVariable("collection_id") Long pCollectionId,
            @RequestBody Collection pCollection);

    /**
     * @summary Entry point to delete a collection using its id
     * @param pCollectionId
     *            id of the {@link Collection} to delete
     * @return Void
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{collection_id}")
    @ResponseBody
    public HttpEntity<Void> deleteCollection(@PathVariable("collection_id") Long pCollectionId);

    /**
     * @summary Entry point to create a collection
     * @param pCollection
     *            {@link Collection} to create
     * @return created {@link Collection}
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<Resource<Collection>> createCollection(@RequestBody Collection pCollection);

    /**
     * Entry point to handle dissociation of {@link Collection} specified by its id to other entities
     *
     * @param pCollectionId
     *            {@link Collection} id
     * @param pToBeDissociated
     *            entity to dissociate
     * @return {@link Collection} as a {@link Resource}
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}/dissociate")
    @ResponseBody
    public HttpEntity<Resource<Collection>> dissociateCollection(@PathVariable("collection_id") Long pCollectionId,
            @RequestBody Set<UniformResourceName> pToBeDissociated);

    /**
     * Entry point to handle association of {@link Collection} specified by its id to other entities
     *
     * @param pCollectionId
     *            {@link Collection} id
     * @param pToBeAssociatedWith
     *            entities to be associated
     * @return {@link Collection} as a {@link Resource}
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}/associate")
    @ResponseBody
    public HttpEntity<Resource<Collection>> associateCollections(@PathVariable("collection_id") Long pCollectionId,
            @RequestBody Set<UniformResourceName> pToBeAssociatedWith);
}
