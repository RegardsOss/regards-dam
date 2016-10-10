/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.rest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;
import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.collections.service.ICollectionsRequestService;
import fr.cnes.regards.modules.collections.signature.CollectionsSignature;
import fr.cnes.regards.modules.core.annotation.ModuleInfo;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;

/**
 * @author lmieulet
 *
 */
@RestController
@ModuleInfo(name = "collections", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
public class CollectionsController implements CollectionsSignature {

    @Autowired
    private ICollectionsRequestService collectionsRequestService_;

    /**
     * @summary Entry point to retrieve all collections
     */
    @Override
    public HttpEntity<List<Resource<Collection>>> retrieveCollectionList() {
        List<Collection> collections = collectionsRequestService_.retrieveCollectionList();
        List<Resource<Collection>> resources = collections.stream().map(u -> new Resource<>(u))
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    /**
     * @summary Entry point to retrieve all collections that uses a specific model
     */
    @Override
    public HttpEntity<List<Resource<Collection>>> retrieveCollectionListByModelId(
            @PathVariable("model_id") Long pModelId) {
        List<Collection> collections = collectionsRequestService_.retrieveCollectionListByModelId(pModelId);
        List<Resource<Collection>> resources = collections.stream().map(u -> new Resource<>(u))
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    /**
     * @summary Entry point to retrieve a collection using its id
     */
    @Override
    public HttpEntity<Resource<Collection>> retrieveCollection(@PathVariable("collection_id") String pCollectionId) {
        Collection collection = collectionsRequestService_.retrieveCollectionById(pCollectionId);
        Resource<Collection> resource = new Resource<>(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * @throws OperationNotSupportedException
     * @summary Entry point to update a collection using its id
     */
    @Override
    public HttpEntity<Resource<Collection>> updateCollection(@PathVariable("collection_id") String pCollectionId,
            @RequestBody Collection pCollection) throws OperationNotSupportedException {
        Collection collection = collectionsRequestService_.updateCollection(pCollection, pCollectionId);
        Resource<Collection> resource = new Resource<>(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * @summary Entry point to delete a collection using its id
     */
    @Override
    public HttpEntity<Void> deleteCollection(@PathVariable("collection_id") String pCollectionId) {
        collectionsRequestService_.deleteCollection(pCollectionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * @summary Entry point to create a collection
     */
    @Override
    public HttpEntity<Resource<Collection>> createCollection(@RequestBody Collection pCollection) {
        Collection collection = collectionsRequestService_.createCollection(pCollection);
        Resource<Collection> resource = new Resource<>(collection);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Data Not Found")
    public void dataNotFound() {
    }

    @ExceptionHandler(AlreadyExistingException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void dataAlreadyExisting() {
    }

    @ExceptionHandler(OperationNotSupportedException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "operation not supported")
    public void operationNotSupported() {
    }

    @ExceptionHandler(InvalidValueException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid Value")
    public void invalidValue() {
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "data does not respect validation constrains")
    public void validation() {
    }

}
