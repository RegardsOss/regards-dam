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
package fr.cnes.regards.modules.entities.service;

import com.google.common.io.Files;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.dao.*;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Specific EntityService for documents
 * @author Léo Mieulet
 */
@Service
@MultitenantTransactional
public class DocumentService extends AbstractEntityService<Document> implements IDocumentService {


    private final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);


    /**
     * Attribute {@link PluginService}
     */
    private final IDocumentLSService documentFilesService;

    public DocumentService(IModelAttrAssocService pModelAttributeService,
                           IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
                           IDeletedEntityRepository pDeletedEntityRepository, ICollectionRepository pCollectionRepository,
                           IDatasetRepository pDatasetRepository, IDocumentRepository pDocumentRepository,
                           EntityManager pEm, IPublisher pPublisher, IRuntimeTenantResolver runtimeTenantResolver,
                           IDescriptionFileRepository descriptionFileRepository, IDocumentLSService documentFilesService) {
        super(pModelAttributeService, pEntityRepository, pModelService, pDeletedEntityRepository, pCollectionRepository,
                pDatasetRepository, pDocumentRepository, pEm, pPublisher, runtimeTenantResolver, descriptionFileRepository);
        this.documentFilesService = documentFilesService;
    }

    /**
     *
     * @param documentId the document id that you are editing
     * @param files the list of new file to attach to the document
     * @param fileLsUriTemplate Template of the file's uri
     * @return
     * @throws ModuleException
     */
    @Override
    public Document addFiles(Long documentId, MultipartFile [] files, String fileLsUriTemplate) throws ModuleException {
        Document doc = this.load(documentId);
        if (doc == null) {
            throw new EntityNotFoundException(documentId.toString(), Document.class);
        }
        Set<DataFile> docFiles = documentFilesService.handleFiles(doc, files, fileLsUriTemplate);
        if (!doc.getDocuments().isEmpty()) {
            docFiles.addAll(doc.getDocuments());
        }
        doc.setDocuments(docFiles);
        this.update(doc);
        return doc;
    }

    /**
     * Allows to remove a single file from a document, then save the entity
     * @param documentId the document id
     * @param fileChecksum the checksum of the file
     * @throws ModuleException
     */
    @Override
    public Document deleteFile(Long documentId, String fileChecksum) throws ModuleException, IOException {
        // Check if the query is valid
        Document doc = this.load(documentId);
        if (doc == null) {
             throw new EntityNotFoundException(documentId.toString(), Document.class);
        }
        Set<DataFile> docFiles = doc.getDocuments();
        Optional<DataFile> dataFileToRemove = docFiles.stream().filter(dataFile -> fileChecksum.equals(dataFile.getChecksum())).findFirst();
        if (!dataFileToRemove.isPresent()) {
            throw new EntityNotFoundException(fileChecksum.toString(), DataFile.class);
        }
        DataFile dataFile = dataFileToRemove.get();
        // Try to remove the file if locally stored, otherwise the file is not stored on this microservice
        if (documentFilesService.isFileLocallyStored(doc, dataFile)) {
            documentFilesService.removeFile(doc, dataFile);
        }
        doc.getDocuments().remove(dataFile);
        this.update(doc);
        return doc;
    }

    /**
     * Ensure that all files that refers the document from DocumentLS have been removed before removing the entity
     * @param documentId
     * @throws ModuleException
     */
    @Override
    public void deleteDocumentAndFiles(Long documentId) throws EntityNotFoundException, IOException {
        Document doc = this.load(documentId);
        if (doc == null) {
            throw new EntityNotFoundException(documentId.toString(), Document.class);
        }
        Set<DataFile> docFiles = doc.getDocuments();
        for (DataFile docFile : docFiles) {
            documentFilesService.removeFile(doc, docFile);
        }
        this.delete(documentId);
    }

    @Override
    public byte[] retrieveFileContent(Long pDocumentId, String fileChecksum) throws IOException {
        return Files.asByteSource(new File("dfgsdfg")).read();
    }

    @Override
    public DataFile retrieveDataFile(Long pDocumentId, String fileChecksum) {
        return null;
    }
}
