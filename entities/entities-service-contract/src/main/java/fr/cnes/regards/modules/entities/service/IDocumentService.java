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
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * Qualified interface for Document entity service
 *
 * @author Léo Mieulet
 */
public interface IDocumentService extends IEntityService<Document> {

    /**
     * Saves several files in the document
     *
     * @param pDocumentId       document id
     * @param files             list of MultipartFile files
     * @param fileLsUriTemplate the template of files locally stored
     * @return updated document
     * @throws ModuleException
     */
    Document addFiles(Long pDocumentId, MultipartFile[] files, String fileLsUriTemplate) throws ModuleException;

    /**
     * Delete one file of the Document. If the file is locally stored, remove it
     *
     * @param pDocumentId  document id
     * @param fileChecksum document file checksum
     * @return updated document
     * @throws ModuleException
     * @throws IOException
     */
    Document deleteFile(Long pDocumentId, String fileChecksum) throws ModuleException, IOException;

    /**
     * Delete everything related to the document
     *
     * @param pDocumentId document id
     * @throws EntityNotFoundException
     * @throws IOException
     */
    void deleteDocumentAndFiles(Long pDocumentId) throws EntityNotFoundException, IOException;

    /**
     * Return the content of the file if the file is locally stored
     *
     * @param pDocumentId  document id
     * @param fileChecksum file checksum
     * @return
     * @throws IOException
     * @throws EntityNotFoundException
     */
    byte[] retrieveFileContent(Long pDocumentId, String fileChecksum) throws IOException, EntityNotFoundException;

    /**
     * Retrieve file information
     *
     * @param pDocumentId  document id
     * @param fileChecksum file checksum
     * @return
     * @throws EntityNotFoundException
     */
    DataFile retrieveDataFile(Long pDocumentId, String fileChecksum) throws EntityNotFoundException;
}
