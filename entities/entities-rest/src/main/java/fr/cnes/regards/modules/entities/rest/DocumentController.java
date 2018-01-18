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
import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
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
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.service.DocumentLSService;
import fr.cnes.regards.modules.entities.service.IDocumentService;
import fr.cnes.regards.modules.indexer.domain.DataFile;

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

    public static final String DOCUMENT_FILES_SINGLE_MAPPING = DOCUMENT_FILES_MAPPING + "/{file_checksum}";

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
    @ResourceAccess(description = "endpoint to retrieve the list of all documents")
    public ResponseEntity<PagedResources<Resource<Document>>> retrieveDocuments(final Pageable pPageable,
                                                                                final PagedResourcesAssembler<Document> pAssembler) {
        final Page<Document> documents = documentService.findAll(pPageable);
        final PagedResources<Resource<Document>> resources = toPagedResources(documents, pAssembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    /**
     * Entry point to retrieve a document using its id
     *
     * @param pDocumentId {@link Document} id
     * @return {@link Document} as a {@link Resource}
     */
    @RequestMapping(method = RequestMethod.GET, value = DocumentController.DOCUMENT_MAPPING)
    @ResourceAccess(description = "Retrieve a document")
    public ResponseEntity<Resource<Document>> retrieveDocument(
            @PathVariable("document_id") final Long pDocumentId) {
        final Document document = documentService.load(pDocumentId);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to update a document using its id
     *
     * @param pDocumentId {@link Document} id
     * @param pDocument   {@link Document}
     * @param pResult     for validation of entites' properties
     * @return update {@link Document} as a {@link Resource}
     * @throws ModuleException if error occurs!
     */
    @RequestMapping(method = RequestMethod.PUT, value = DocumentController.DOCUMENT_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Update a document")
    public ResponseEntity<Resource<Document>> updateDocument(@PathVariable("document_id") final Long pDocumentId,
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
     * @param pDocumentId {@link Document} id
     * @return nothing
     * @throws EntityNotFoundException
     * @
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DocumentController.DOCUMENT_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "delete the document using its id")
    public ResponseEntity<Void> deleteDocument(@PathVariable("document_id") final Long pDocumentId)
            throws EntityNotFoundException, IOException {
        documentService.deleteDocumentAndFiles(pDocumentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to create a document
     *
     * @param pDocument {@link Document} to create
     * @param pResult   validation errors
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException if validation fails
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
     * @param pDocumentId      {@link Document} id
     * @param pToBeDissociated entity to dissociate
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DOCUMENT_DISSOCIATE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Dissociate a document from  a list of entities")
    public ResponseEntity<Void> dissociate(@PathVariable("document_id") final Long pDocumentId,
                                           @Valid @RequestBody final Set<UniformResourceName> pToBeDissociated) throws ModuleException {
        documentService.dissociate(pDocumentId, pToBeDissociated);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to handle association of {@link Document} specified by its id to other entities
     *
     * @param pDocumentId         {@link Document} id
     * @param pToBeAssociatedWith entities to be associated
     * @return {@link Document} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DOCUMENT_ASSOCIATE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Associate the document of id document_id to the list of entities in parameter")
    public ResponseEntity<Void> associate(@PathVariable("document_id") final Long pDocumentId,
                                          @Valid @RequestBody final Set<UniformResourceName> pToBeAssociatedWith) throws ModuleException {
        documentService.associate(pDocumentId, pToBeAssociatedWith);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Add files to document of given id
     *
     * @param pDocumentId the id of the document
     * @param files       The list of files
     * @return the updated document wrapped in an HTTP response
     */
    @RequestMapping(method = RequestMethod.POST, value = DOCUMENT_FILES_MAPPING)
    @ResourceAccess(description = "Add files to a document using its id")
    public ResponseEntity<Resource<Document>> addFiles(@PathVariable("document_id") final Long pDocumentId,
                                                       @RequestPart final MultipartFile[] files) throws ModuleException, IOException, NoSuchMethodException {
        ControllerLinkBuilder controllerLinkBuilder = ControllerLinkBuilder.linkTo(this.getClass(), this.getClass().getMethod("retrieveDocumentFile", String.class, Long.class, String.class, HttpServletResponse.class), pDocumentId, DocumentLSService.FILE_CHECKSUM_URL_TEMPLATE);
        Link link = controllerLinkBuilder.withSelfRel();
        String fileLsUriTemplate = link.getHref();

        final Document document = documentService.addFiles(pDocumentId, files, fileLsUriTemplate);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to retrieve a file from a document with given checksum
     *
     * @param pDocumentId  {@link Document} id
     * @param fileChecksum {String} the checksum of the file to retrieve
     * @param response     {@link HttpServletResponse} inject the response in order to manually set headers and stuff
     * @return the requested file
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.GET, value = DocumentController.DOCUMENT_FILES_SINGLE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "Retrieve the file in given document with given checksum")
    public void retrieveDocumentFile(@RequestParam(name = "origin", required = false) String origin,
                                     @PathVariable("document_id") final Long pDocumentId, @PathVariable("file_checksum") final String fileChecksum,
                                     HttpServletResponse response) throws ModuleException, IOException {
        byte[] fileContent = documentService.retrieveFileContent(pDocumentId, fileChecksum);
        DataFile dataFile = documentService.retrieveDataFile(pDocumentId, fileChecksum);
        if (fileContent != null) {
            if (origin != null) {
                response.setHeader(HttpHeaders.X_FRAME_OPTIONS, "ALLOW-FROM " + origin);
            }
            response.setContentType(dataFile.getMimeType().toString());
            response.setContentLength(fileContent.length);
            response.getOutputStream().write(fileContent);
            response.getOutputStream().flush();
            response.setStatus(HttpStatus.OK.value());
        } else {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        }
    }

    /**
     * Entry point to delete a file from a document
     *
     * @param pDocumentId {@link Document} id
     * @return the deleted document
     * @throws EntityNotFoundException
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DocumentController.DOCUMENT_FILES_SINGLE_MAPPING)
    @ResponseBody
    @ResourceAccess(description = "delete the document file using its id")
    public HttpEntity<Resource<Document>> deleteDocumentFile(@PathVariable("document_id") final Long pDocumentId,
                                                             @PathVariable("file_checksum") final String fileChecksum)
            throws ModuleException, IOException {
        final Document document = documentService.deleteFile(pDocumentId, fileChecksum);
        final Resource<Document> resource = toResource(document);
        return new ResponseEntity<>(resource, HttpStatus.OK);
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
