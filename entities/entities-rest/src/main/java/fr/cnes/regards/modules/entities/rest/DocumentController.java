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

import com.google.common.net.HttpHeaders;
import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.service.ICollectionService;
import fr.cnes.regards.modules.entities.service.IDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lmieulet
 */
@RestController
@RequestMapping(path = DocumentController.ROOT_MAPPING)
public class DocumentController implements IResourceController<Document> {

    public static final String ROOT_MAPPING = "/documents";

    public static final String DOCUMENT_MAPPING = "/{document_id}";

    public static final String DOCUMENT_ASSOCIATE_MAPPING = DOCUMENT_MAPPING + "/associate";

    public static final String DOCUMENT_DISSOCIATE_MAPPING = DOCUMENT_MAPPING + "/dissociate";

    public static final String DOCUMENT_FILES_MAPPING = DOCUMENT_MAPPING + "/files";

    public static final String DOCUMENT_FILES_DELETE_MAPPING = DOCUMENT_FILES_MAPPING + "/{file_id}";



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
     *
     * @return all {@link Document}s
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResourceAccess(description = "endpoint to retrieve the list fo all documents")
    public HttpEntity<PagedResources<Resource<Document>>> retrieveDocuments(final Pageable pPageable,
                            final PagedResourcesAssembler<Document> pAssembler) {
        final Page<Document> documents = documentService.findAll(pPageable);
        final PagedResources<Resource<Document>> resources = toPagedResources(documents, pAssembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    /**
     * Entry point to retrieve a document using its id
     *
     * @param pDocumentId
     *            {@link Document} id
     * @return {@link Document} as a {@link Resource}
     */
    @RequestMapping(method = RequestMethod.GET, value = DocumentController.DOCUMENT_MAPPING)
    @ResourceAccess(description = "Retrieve a document")
    public HttpEntity<Resource<Document>> retrieveDocument(
            @PathVariable("document_id") final Long pDocumentId) {
        final Document document = documentService.load(pDocumentId);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to update a document using its id
     *
     * @param pDocumentId
     *            {@link Document} id
     * @param pDocument
     *            {@link Document}
     * @param pResult
     *            for validation of entites' properties
     * @return update {@link Document} as a {@link Resource}
     * @throws ModuleException
     *             if error occurs!
     */
    @RequestMapping(method = RequestMethod.PUT, value = DocumentController.DOCUMENT_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Update a document")
    public HttpEntity<Resource<Document>> updateDocument(@PathVariable("document_id") final Long pDocumentId,
                                                           @Valid @RequestBody Document pDocument,
                                                           final BindingResult pResult) throws ModuleException, IOException {

        // Validate dynamic model
        documentService.validate(pDocument, pResult, true);

        final Document document = documentService.update(pDocumentId, pDocument);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to delete a document using its id
     *
     * @param pDocumentId
     *            {@link Document} id
     * @return nothing
     * @throws EntityNotFoundException
     * @
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DocumentController.DOCUMENT_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "delete the document using its id")
    public HttpEntity<Void> deleteDocument(@PathVariable("document_id") final Long pDocumentId)
            throws EntityNotFoundException {
        documentService.delete(pDocumentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to create a document
     *
     * @param pDocument
     *            {@link Document} to create
     * @param pResult
     *            validation errors
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException
     *             if validation fails
     * @throws IOException
     * @
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create a new document according to what is passed as parameter")
    public ResponseEntity<Resource<Document>> createDocument(
            @Valid @RequestBody Document pDocument,
            final BindingResult pResult) throws ModuleException, IOException {

        // Validate dynamic model
        documentService.validate(pDocument, pResult, false);

        final Document document = documentService.create(pDocument, null);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /**
     * Entry point to handle dissociation of {@link Document} specified by its id to other entities
     *
     * @param pDocumentId
     *            {@link Document} id
     * @param pToBeDissociated
     *            entity to dissociate
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException
     *             if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DOCUMENT_DISSOCIATE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Dissociate a document from  a list of entities")
    public HttpEntity<Void> dissociate(@PathVariable("document_id") final Long pDocumentId,
            @Valid @RequestBody final Set<UniformResourceName> pToBeDissociated) throws ModuleException {
        documentService.dissociate(pDocumentId, pToBeDissociated);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to handle association of {@link Document} specified by its id to other entities
     *
     * @param pDocumentId
     *            {@link Document} id
     * @param pToBeAssociatedWith
     *            entities to be associated
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException
     *             if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DOCUMENT_ASSOCIATE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Associate the document of id document_id to the list of entities in parameter")
    public HttpEntity<Void> associate(@PathVariable("document_id") final Long pDocumentId,
            @Valid @RequestBody final Set<UniformResourceName> pToBeAssociatedWith) throws ModuleException {
        documentService.associate(pDocumentId, pToBeAssociatedWith);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    /**
     * Add files to document of given id
     * @param pDocumentId the id of the document
     * @param pResult for validation of entites' properties
     * @return the updated dataset wrapped in an HTTP response
     */
    @RequestMapping(method = RequestMethod.POST, value = DOCUMENT_FILES_MAPPING)
    @ResourceAccess(description = "Updates a Dataset")
    public ResponseEntity<Resource<Document>> addFiles(@PathVariable("document_id") final Long pDocumentId,
           @RequestPart(value = "files",required = false)  final MultipartFile[] files,
           @RequestParam(value = "files", required = false) final MultipartFile[] files2,
           @RequestBody(required = false)  final List<MultipartFile> files3) throws ModuleException, IOException {
        for (MultipartFile file : files) {
            System.out.println(file.getOriginalFilename());
        }
        final Document dataSet = documentService.addFiles(pDocumentId, files);
        final Resource<Document> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to delete a file from a document
     *
     * @param pDocumentId
     *            {@link Document} id
     * @return nothing
     * @throws EntityNotFoundException
     * @
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DocumentController.DOCUMENT_FILES_DELETE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "delete the document using its id")
    public HttpEntity<Void> deleteDocumentFile(@PathVariable("document_id") final Long pDocumentId,
                                               @PathVariable("file_id") final Long pFileId)
            throws EntityNotFoundException {
        documentService.deleteFile(pDocumentId, pFileId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public Resource<Document> toResource(final Document pElement, final Object... pExtras) {
        final Resource<Document> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "retrieveDocument", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "retrieveDocuments", LinkRels.LIST,
                MethodParamFactory.build(Pageable.class), MethodParamFactory.build(PagedResourcesAssembler.class));
        resourceService.addLink(resource, this.getClass(), "deleteDocument", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateDocument", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Document.class),
                                MethodParamFactory.build(BindingResult.class));
        resourceService.addLink(resource, this.getClass(), "dissociate", "dissociate",
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Set.class));
        resourceService.addLink(resource, this.getClass(), "associate", "associate",
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Set.class));
        return resource;
    }
}
