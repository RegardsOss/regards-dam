/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.dam.rest.entities;

import java.io.IOException;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.service.entities.ICollectionService;
import fr.cnes.regards.modules.model.service.validation.ValidationMode;

/**
 * Collection API
 * @author lmieulet
 */
@RestController
@RequestMapping(path = CollectionController.TYPE_MAPPING)
public class CollectionController implements IResourceController<Collection> {

    public static final String TYPE_MAPPING = "/collections";

    public static final String COLLECTION_MAPPING = "/{collection_id}";

    public static final String COLLECTION_ASSOCIATE_MAPPING = COLLECTION_MAPPING + "/associate";

    public static final String COLLECTION_DISSOCIATE_MAPPING = COLLECTION_MAPPING + "/dissociate";

    /**
     * Service
     */
    @Autowired
    private ICollectionService collectionService;

    /**
     * HATEOAS service
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Entry point to retrieve {@link Collection}s
     * @param label
     * @param pageable
     * @param assembler
     * @return all {@link Collection}s
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResourceAccess(description = "endpoint to retrieve the list fo all collections")
    public ResponseEntity<PagedModel<EntityModel<Collection>>> retrieveCollections(
            @RequestParam(name = "label", required = false) String label,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            PagedResourcesAssembler<Collection> assembler) {
        final Page<Collection> collections = collectionService.search(label, pageable);
        final PagedModel<EntityModel<Collection>> resources = toPagedResources(collections, assembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    /**
     * Entry point to retrieve a collection using its id
     * @param id {@link Collection} id
     * @return {@link Collection} as a {@link EntityModel}
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.GET, value = COLLECTION_MAPPING)
    @ResourceAccess(description = "Retrieve a collection")
    public HttpEntity<EntityModel<Collection>> retrieveCollection(@PathVariable("collection_id") Long id)
            throws ModuleException {
        final Collection collection = collectionService.load(id);
        final EntityModel<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to update a collection using its id
     * @param id {@link Collection} id
     * @param inCollection {@link Collection}
     * @param result for validation of entites' properties
     * @return update {@link Collection} as a {@link EntityModel}
     * @throws ModuleException if error occurs! @
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.PUT, value = COLLECTION_MAPPING)
    @ResourceAccess(description = "Update a collection")
    public HttpEntity<EntityModel<Collection>> updateCollection(@PathVariable("collection_id") Long id,
            @Valid @RequestBody Collection inCollection, BindingResult result) throws ModuleException, IOException {
        collectionService.checkAndOrSetModel(inCollection);
        // Validate dynamic model
        collectionService.validate(inCollection, result, ValidationMode.UPDATE);

        final Collection collection = collectionService.update(id, inCollection);
        final EntityModel<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to delete a collection using its id
     * @param id {@link Collection} id
     * @return nothing
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.DELETE, value = COLLECTION_MAPPING)
    @ResourceAccess(description = "delete the collection of collection_id")
    public HttpEntity<Void> deleteCollection(@PathVariable("collection_id") final Long id) throws ModuleException {
        collectionService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to create a collection
     * @param inCollection {@link Collection} to create
     * @param result validation errors
     * @return {@link Collection} as a {@link EntityModel}
     * @throws ModuleException if validation fails
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create a new collection according to what is passed as parameter")
    public ResponseEntity<EntityModel<Collection>> createCollection(@Valid @RequestBody Collection inCollection,
            BindingResult result) throws ModuleException, IOException {
        collectionService.checkAndOrSetModel(inCollection);
        // Validate dynamic model
        collectionService.validate(inCollection, result, ValidationMode.CREATION);

        final Collection collection = collectionService.create(inCollection);
        final EntityModel<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /**
     * Entry point to handle dissociation of {@link Collection} specified by its id to other entities
     * @param id {@link Collection} id
     * @param toBeDissociated entity to dissociate
     * @return {@link Collection} as a {@link EntityModel}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = COLLECTION_DISSOCIATE_MAPPING)
    @ResourceAccess(description = "Dissociate a collection from  a list of entities")
    public HttpEntity<Void> dissociate(@PathVariable("collection_id") final Long id,
            @Valid @RequestBody final Set<String> toBeDissociated) throws ModuleException {
        collectionService.dissociate(id, toBeDissociated);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to handle association of {@link Collection} specified by its id to other entities
     * @param id {@link Collection} id
     * @param toBeAssociatedWith entities to be associated
     * @return {@link Collection} as a {@link EntityModel}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = COLLECTION_ASSOCIATE_MAPPING)
    @ResourceAccess(description = "Associate the collection of id collection_id to the list of entities in parameter")
    public HttpEntity<Void> associate(@PathVariable("collection_id") final Long id,
            @Valid @RequestBody final Set<String> toBeAssociatedWith) throws ModuleException {
        collectionService.associate(id, toBeAssociatedWith);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public EntityModel<Collection> toResource(final Collection element, final Object... extras) {
        final EntityModel<Collection> resource = resourceService.toResource(element);
        resourceService.addLink(resource, this.getClass(), "retrieveCollection", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, element.getId()));
        resourceService.addLink(resource, this.getClass(), "retrieveCollections", LinkRels.LIST,
                                MethodParamFactory.build(String.class, element.getLabel()),
                                MethodParamFactory.build(Pageable.class),
                                MethodParamFactory.build(PagedResourcesAssembler.class));
        resourceService.addLink(resource, this.getClass(), "deleteCollection", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, element.getId()));
        resourceService.addLink(resource, this.getClass(), "updateCollection", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Collection.class),
                                MethodParamFactory.build(BindingResult.class));
        resourceService.addLink(resource, this.getClass(), "dissociate", LinkRelation.of("dissociate"),
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Set.class));
        resourceService.addLink(resource, this.getClass(), "associate", LinkRelation.of("associate"),
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Set.class));
        return resource;
    }
}
