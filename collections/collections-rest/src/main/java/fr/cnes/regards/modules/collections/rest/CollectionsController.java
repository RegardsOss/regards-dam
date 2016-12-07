/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.rest;

import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.collections.service.ICollectionsRequestService;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * @author lmieulet
 *
 */
@RestController
@ModuleInfo(name = "collections", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
@RequestMapping(value = "/collections")
public class CollectionsController implements IResourceController<Collection> {

    /**
     * Service
     */
    @Autowired
    private ICollectionsRequestService collectionsRequestService;

    @Autowired
    private IResourceService resourceService;

    /**
     * Entry point to retrieve {@link Collection}s
     *
     * @return all {@link Collection}s
     */
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "endpoint to retrieve the list fo all collections")
    public HttpEntity<List<Resource<Collection>>> retrieveCollectionList() {

        final List<Collection> collections = collectionsRequestService.retrieveCollectionList();
        final List<Resource<Collection>> resources = toResources(collections);
        return new ResponseEntity<>(resources, HttpStatus.OK);

    }

    /**
     * @summary Entry point to retrieve a collection using its id
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{collection_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "retrieve the collection of id collection_id")
    public HttpEntity<Resource<Collection>> retrieveCollection(@PathVariable("collection_id") Long pCollectionId) {
        final Collection collection = collectionsRequestService.retrieveCollectionById(pCollectionId);
        final Resource<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * @throws OperationNotSupportedException
     * @summary Entry point to update a collection using its id
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(
            description = "update the collection of id collection_id to match  the collection passed in parameter")
    public HttpEntity<Resource<Collection>> updateCollection(@PathVariable("collection_id") Long pCollectionId,
            @Valid @RequestBody Collection pCollection) throws EntityInconsistentIdentifierException {
        final Collection collection = collectionsRequestService.updateCollection(pCollection, pCollectionId);
        final Resource<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * @summary Entry point to delete a collection using its id
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{collection_id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "delete the collection of collection_id")
    public HttpEntity<Void> deleteCollection(@PathVariable("collection_id") Long pCollectionId) {
        collectionsRequestService.deleteCollection(pCollectionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * @summary Entry point to create a collection
     */
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "create a new collection according to what is passed as parameter")
    public HttpEntity<Resource<Collection>> createCollection(@Valid @RequestBody Collection pCollection) {
        final Collection collection = collectionsRequestService.createCollection(pCollection);
        final Resource<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /**
     * @throws OperationNotSupportedException
     * @summary Entry point to update a collection using its id
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}/dissociate",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(
            description = "dissociate the collection of id collection_id from the list of entities in parameter")
    public HttpEntity<Resource<Collection>> dissociateCollection(@PathVariable("collection_id") Long pCollectionId,
            @Valid @RequestBody List<AbstractEntity> pToBeDissociated) throws EntityInconsistentIdentifierException {
        final Collection collection = collectionsRequestService.dissociateCollection(pCollectionId, pToBeDissociated);
        final Resource<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * @throws OperationNotSupportedException
     * @summary Entry point to update a collection using its id
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}/associate",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "associate the collection of id collection_id to the list of entities in parameter")
    public HttpEntity<Resource<Collection>> associateCollections(@PathVariable("collection_id") Long pCollectionId,
            @Valid @RequestBody List<AbstractEntity> pToBeAssociatedWith) throws EntityInconsistentIdentifierException {
        final Collection collection = collectionsRequestService.associateCollection(pCollectionId, pToBeAssociatedWith);
        final Resource<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @Override
    public Resource<Collection> toResource(Collection pElement, Object... pExtras) {
        final Resource<Collection> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "retrieveCollection", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "retrieveCollectionList", LinkRels.LIST);
        resourceService.addLink(resource, this.getClass(), "deleteCollection", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateCollection", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Collection.class));
        return resource;
    }

}
