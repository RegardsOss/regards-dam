/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.collections.service.ICollectionsRequestService;
import fr.cnes.regards.modules.collections.signature.ICollectionsSignature;

/**
 * @author lmieulet
 *
 */
@RestController
@ModuleInfo(name = "collections", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
public class CollectionsController implements IResourceController<Collection>, ICollectionsSignature {

    /**
     * Service
     */
    @Autowired
    private ICollectionsRequestService collectionsRequestService;

    @Autowired
    private IResourceService resourceService;

    /**
     * @summary Entry point to retrieve all collections
     */
    @Override
    public HttpEntity<List<Resource<Collection>>> retrieveCollectionList(Long pModelId) {

        final List<Collection> collections = collectionsRequestService.retrieveCollectionList(pModelId);
        final List<Resource<Collection>> resources = collections.stream().map(u -> new Resource<>(u))
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);

    }

    /**
     * @summary Entry point to retrieve a collection using its id
     */
    @Override
    public HttpEntity<Resource<Collection>> retrieveCollection(@PathVariable("collection_id") Long pCollectionId) {
        final Collection collection = collectionsRequestService.retrieveCollectionById(pCollectionId);
        final Resource<Collection> resource = new Resource<>(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    // TODO: retrieve Collection by (S)IP_ID

    /**
     * @throws OperationNotSupportedException
     * @summary Entry point to update a collection using its id
     */
    @Override
    public HttpEntity<Resource<Collection>> updateCollection(Long pCollectionId, Collection pCollection)
            throws EntityInconsistentIdentifierException {
        final Collection collection = collectionsRequestService.updateCollection(pCollection, pCollectionId);
        final Resource<Collection> resource = new Resource<>(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * @summary Entry point to delete a collection using its id
     */
    @Override
    public HttpEntity<Void> deleteCollection(Long pCollectionId) {
        collectionsRequestService.deleteCollection(pCollectionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * @summary Entry point to create a collection
     */
    @Override
    public HttpEntity<Resource<Collection>> createCollection(@RequestBody Collection pCollection) {
        final Collection collection = collectionsRequestService.createCollection(pCollection);
        final Resource<Collection> resource = new Resource<>(collection);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @Override
    public Resource<Collection> toResource(Collection pElement, Object... pExtras) {
        final Resource<Collection> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "retrieveCollection", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "retrieveCollectionList", LinkRels.LIST);
        resourceService.addLink(resource, this.getClass(), "deleteCollection", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        return resource;
    }

}
