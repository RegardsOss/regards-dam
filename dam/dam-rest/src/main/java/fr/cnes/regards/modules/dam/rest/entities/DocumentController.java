/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dam.domain.entities.Document;
import fr.cnes.regards.modules.dam.service.entities.IDocumentService;

/**
 * @author lmieulet
 * @author Marc Sordi
 */
@RestController
@RequestMapping(path = DocumentController.TYPE_MAPPING)
public class DocumentController implements IResourceController<Document> {

    public static final String TYPE_MAPPING = "/documents";

    public static final String DOCUMENT_MAPPING = "/{document_id}";

    public static final String DOCUMENT_ASSOCIATE_MAPPING = DOCUMENT_MAPPING + "/associate";

    public static final String DOCUMENT_DISSOCIATE_MAPPING = DOCUMENT_MAPPING + "/dissociate";

    /**
     * Service
     */
    @Autowired
    private IDocumentService documentService;

    /**
     * HATEOAS service
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Entry point to retrieve {@link Document}s
     * @return all {@link Document}s
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResourceAccess(description = "endpoint to retrieve the list of all documents")
    public ResponseEntity<PagedResources<Resource<Document>>> retrieveDocuments(Pageable pageable,
            PagedResourcesAssembler<Document> assembler) {
        final Page<Document> documents = documentService.findAll(pageable);
        final PagedResources<Resource<Document>> resources = toPagedResources(documents, assembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    /**
     * Entry point to retrieve a document using its id
     * @param id {@link Document} id
     * @return {@link Document} as a {@link Resource}
     */
    @RequestMapping(method = RequestMethod.GET, value = DocumentController.DOCUMENT_MAPPING)
    @ResourceAccess(description = "Retrieve a document")
    public ResponseEntity<Resource<Document>> retrieveDocument(@PathVariable("document_id") Long id)
            throws ModuleException {
        final Document document = documentService.load(id);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to update a document using its id
     * @param id {@link Document} id
     * @param inDocument {@link Document}
     * @param result for validation of entites' properties
     * @return update {@link Document} as a {@link Resource}
     * @throws ModuleException if error occurs!
     */
    @RequestMapping(method = RequestMethod.PUT, value = DocumentController.DOCUMENT_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Update a document")
    public ResponseEntity<Resource<Document>> updateDocument(@PathVariable("document_id") Long id,
            @Valid @RequestBody Document inDocument, BindingResult result) throws ModuleException, IOException {
        documentService.checkAndOrSetModel(inDocument);
        // Validate dynamic model
        documentService.validate(inDocument, result, true);
        final Document document = documentService.update(id, inDocument);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to delete a document using its id
     * @param id {@link Document} id
     * @return nothing
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DocumentController.DOCUMENT_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "delete the document using its id")
    public ResponseEntity<Void> deleteDocument(@PathVariable("document_id") Long id)
            throws IOException, ModuleException {
        documentService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to create a document
     * @param inDocument {@link Document} to create
     * @param result validation errors
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException if validation fails
     * @
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create a new document according to what is passed as parameter")
    public ResponseEntity<Resource<Document>> createDocument(@Valid @RequestBody Document inDocument,
            BindingResult result) throws ModuleException, IOException {
        documentService.checkAndOrSetModel(inDocument);
        // Validate dynamic model
        documentService.validate(inDocument, result, false);

        final Document document = documentService.create(inDocument);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /**
     * Entry point to handle dissociation of {@link Document} specified by its id to other entities
     * @param id {@link Document} id
     * @param toBeDissociated entity to dissociate
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DOCUMENT_DISSOCIATE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Dissociate a document from  a list of entities")
    public ResponseEntity<Void> dissociate(@PathVariable("document_id") Long id,
            @Valid @RequestBody Set<String> toBeDissociated) throws ModuleException {
        documentService.dissociate(id, toBeDissociated);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to handle association of {@link Document} specified by its id to other entities
     * @param id {@link Document} id
     * @param toBeAssociatedWith entities to be associated
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DOCUMENT_ASSOCIATE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Associate the document of id document_id to the list of entities in parameter")
    public ResponseEntity<Void> associate(@PathVariable("document_id") Long id,
            @Valid @RequestBody Set<String> toBeAssociatedWith) throws ModuleException {
        documentService.associate(id, toBeAssociatedWith);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public Resource<Document> toResource(Document element, Object... extras) {
        final Resource<Document> resource = resourceService.toResource(element);
        resourceService.addLink(resource, this.getClass(), "retrieveDocument", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, element.getId()));
        resourceService.addLink(resource, this.getClass(), "retrieveDocuments", LinkRels.LIST,
                                MethodParamFactory.build(Pageable.class),
                                MethodParamFactory.build(PagedResourcesAssembler.class));
        resourceService.addLink(resource, this.getClass(), "updateDocument", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Document.class),
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
