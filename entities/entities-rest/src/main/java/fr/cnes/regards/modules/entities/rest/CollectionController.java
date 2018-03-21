/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.entities.rest;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;
import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.service.ICollectionService;

/**
 * @author lmieulet
 */
@RestController
@ModuleInfo(name = "collections", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
@RequestMapping(path = CollectionController.ROOT_MAPPING)
public class CollectionController implements IResourceController<Collection> {

    public static final String ROOT_MAPPING = "/collections";

    /**
     * Controller path for description file of a collection using its ip id as path variable
     */
    public static final String COLLECTION_IPID_PATH_FILE = "/{collection_ipId}/file";

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
     * @return all {@link Collection}s
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "endpoint to retrieve the list fo all collections")
    public ResponseEntity<PagedResources<Resource<Collection>>> retrieveCollections(
            @RequestParam(name = "label", required = false) String label, final Pageable pageable,
            final PagedResourcesAssembler<Collection> assembler) {
        Page<Collection> collections = collectionService.search(label, pageable);
        final PagedResources<Resource<Collection>> resources = toPagedResources(collections, assembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    /**
     * Entry point to retrieve a collection using its id
     * @param id {@link Collection} id
     * @return {@link Collection} as a {@link Resource}
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{collection_id}")
    @ResponseBody
    @ResourceAccess(description = "Retrieve a collection")
    public HttpEntity<Resource<Collection>> retrieveCollection(@PathVariable("collection_id") final Long id) {
        final Collection collection = collectionService.load(id);
        final Resource<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Retrieve the description file of a collection, represented by its ip id
     * @param origin origin to be allowed for X-FRAME-OPTIONS header
     */
    @RequestMapping(method = RequestMethod.GET, value = COLLECTION_IPID_PATH_FILE)
    @ResourceAccess(description = "Retrieves a collection description file content", role = DefaultRole.PUBLIC)
    public void retrieveCollectionDescription(@RequestParam(name = "origin", required = false) String origin,
            @PathVariable("collection_ipId") String ipId, HttpServletResponse response)
            throws EntityNotFoundException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DescriptionFile file = collectionService.retrieveDescription(UniformResourceName.fromString(ipId));
        if (file == null) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            if (origin != null) {
                response.setHeader(HttpHeaders.X_FRAME_OPTIONS, "ALLOW-FROM " + origin);
            }
            String filename = ipId;
            if (MediaType.APPLICATION_PDF.equals(file.getType())) {
                filename += ".pdf";
            }
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);

            out.write(file.getContent());
            response.setContentType(file.getType().toString());
            response.setContentLength(out.size());
            response.getOutputStream().write(out.toByteArray());
            response.getOutputStream().flush();
            response.setStatus(HttpStatus.OK.value());
        }
    }

    /**
     * Remove the description file of a collection, represented by its ip id
     */
    @RequestMapping(method = RequestMethod.DELETE, value = COLLECTION_IPID_PATH_FILE)
    @ResourceAccess(description = "remove a dataset description file content")
    public ResponseEntity<Void> removeCollectionDescription(@PathVariable("collection_ipId") String collectionIpId)
            throws EntityNotFoundException {
        collectionService.removeDescription(UniformResourceName.fromString(collectionIpId));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to update a collection using its id
     * @param id {@link Collection} id
     * @param inCollection {@link Collection}
     * @param result for validation of entites' properties
     * @return update {@link Collection} as a {@link Resource}
     * @throws ModuleException if error occurs! @
     */
    @RequestMapping(method = RequestMethod.POST, value = "/{collection_id}")
    @ResponseBody
    @ResourceAccess(description = "Update a collection")
    public HttpEntity<Resource<Collection>> updateCollection(@PathVariable("collection_id") final Long id,
            @Valid @RequestPart(name = "collection") final Collection inCollection,
            @RequestPart(name = "file", required = false) final MultipartFile descriptionFile,
            final BindingResult result) throws ModuleException, IOException {
        collectionService.checkAndOrSetModel(inCollection);
        // Validate dynamic model
        collectionService.validate(inCollection, result, true);

        final Collection collection = collectionService.update(id, inCollection, descriptionFile);
        final Resource<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to delete a collection using its id
     * @param id {@link Collection} id
     * @return nothing
     * @
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{collection_id}")
    @ResponseBody
    @ResourceAccess(description = "delete the collection of collection_id")
    public HttpEntity<Void> deleteCollection(@PathVariable("collection_id") final Long id)
            throws EntityNotFoundException {
        collectionService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to create a collection
     * @param inCollection {@link Collection} to create
     * @param result validation errors
     * @return {@link Collection} as a {@link Resource}
     * @throws ModuleException if validation fails
     * @
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create a new collection according to what is passed as parameter")
    public ResponseEntity<Resource<Collection>> createCollection(
            @Valid @RequestPart(name = "collection") final Collection inCollection,
            @RequestPart(name = "file", required = false) final MultipartFile descriptionFile,
            final BindingResult result) throws ModuleException, IOException {
        collectionService.checkAndOrSetModel(inCollection);
        // Validate dynamic model
        collectionService.validate(inCollection, result, false);

        final Collection collection = collectionService.create(inCollection, descriptionFile);
        final Resource<Collection> resource = toResource(collection);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /**
     * Entry point to handle dissociation of {@link Collection} specified by its id to other entities
     * @param id {@link Collection} id
     * @param toBeDissociated entity to dissociate
     * @return {@link Collection} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}/dissociate")
    @ResponseBody
    @ResourceAccess(description = "Dissociate a collection from  a list of entities")
    public HttpEntity<Void> dissociate(@PathVariable("collection_id") final Long id,
            @Valid @RequestBody final Set<UniformResourceName> toBeDissociated) throws ModuleException {
        collectionService.dissociate(id, toBeDissociated);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to handle association of {@link Collection} specified by its id to other entities
     * @param id {@link Collection} id
     * @param toBeAssociatedWith entities to be associated
     * @return {@link Collection} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{collection_id}/associate")
    @ResponseBody
    @ResourceAccess(description = "Associate the collection of id collection_id to the list of entities in parameter")
    public HttpEntity<Void> associate(@PathVariable("collection_id") final Long id,
            @Valid @RequestBody final Set<UniformResourceName> toBeAssociatedWith) throws ModuleException {
        collectionService.associate(id, toBeAssociatedWith);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public Resource<Collection> toResource(final Collection element, final Object... extras) {
        final Resource<Collection> resource = resourceService.toResource(element);
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
                                MethodParamFactory.build(MultipartFile.class),
                                MethodParamFactory.build(BindingResult.class));
        resourceService.addLink(resource, this.getClass(), "dissociate", "dissociate",
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Set.class));
        resourceService.addLink(resource, this.getClass(), "associate", "associate",
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Set.class));
        return resource;
    }
}
