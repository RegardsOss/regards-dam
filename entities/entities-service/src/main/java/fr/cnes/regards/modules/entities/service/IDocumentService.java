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

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Qualified interface for Document entity service
 * @author Léo Mieulet
 */
public interface IDocumentService extends IEntityService<Document> {

    Document addFiles(Long pDocumentId, MultipartFile [] files, String fileLsUriTemplate) throws ModuleException;

    Document deleteFile(Long pDocumentId, String fileChecksum) throws ModuleException, IOException;

    void deleteDocumentAndFiles(Long pDocumentId) throws EntityNotFoundException, IOException;

    byte[] retrieveFileContent(Long pDocumentId, String fileChecksum) throws IOException, EntityNotFoundException;

    DataFile retrieveDataFile(Long pDocumentId, String fileChecksum) throws EntityNotFoundException;
}
