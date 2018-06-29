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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.framework.utils.file.ChecksumUtils;
import fr.cnes.regards.modules.entities.dao.ILocalFileRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.LocalFile;
import fr.cnes.regards.modules.entities.service.exception.InvalidCharsetException;
import fr.cnes.regards.modules.entities.service.exception.InvalidContentTypeException;
import fr.cnes.regards.modules.entities.service.exception.InvalidOriginalNameException;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * A service to save document files on disk.</br>
 * @author Léo Mieulet
 */
@Service
@MultitenantTransactional
public class LocalStorageService implements ILocalStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageService.class);

    private static final String DIGEST_ALGORITHM = "MD5";

    /**
     * This constant is used by the controller to create an URL that contains a flag that will be replaced by
     * the real checksum here in the service
     */
    public static final String FILE_CHECKSUM_URL_TEMPLATE = "~checksumFlag~";

    @Autowired
    private ILocalFileRepository localStorageRepo;

    /**
     * Folder where this service will store all document files
     */
    @Value("${regards.dam.local_storage.path:/tmp/rs-dam-ls}")
    private String localStoragePath;

    @PostConstruct
    public void init() {
        // Init localStoragePath folder if necessary
        Path path = Paths.get(this.localStoragePath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                LOGGER.error("Could not create the localStoragePath directory (" + localStoragePath + ")", e);
                throw new RsRuntimeException(e);
            }
        }
    }

    @Override
    public Collection<DataFile> attachFiles(AbstractEntity<?> entity, DataType dataType, MultipartFile[] attachments,
            String fileUriTemplate) throws ModuleException {
        Set<DataFile> docFiles = new HashSet<>();
        try {
            for (MultipartFile file : attachments) {
                if ((file != null) && !file.isEmpty()) {
                    supports(dataType, file);
                    String checksum = ChecksumUtils.computeHexChecksum(file.getInputStream(), DIGEST_ALGORITHM);
                    URI fileRef = new URI(fileUriTemplate.replace(FILE_CHECKSUM_URL_TEMPLATE, checksum));

                    // Build data file
                    DataFile dataFile = new DataFile();
                    dataFile.setDataType(dataType);
                    dataFile.setMimeType(MimeType.valueOf(file.getContentType()));
                    dataFile.setSize(file.getSize());
                    dataFile.setDigestAlgorithm(DIGEST_ALGORITHM);
                    dataFile.setChecksum(checksum);
                    dataFile.setName(file.getOriginalFilename());
                    store(checksum, file, entity);
                    dataFile.setUri(fileRef);
                    docFiles.add(dataFile);
                }
            }
        } catch (URISyntaxException | IOException e) {
            String message = String.format("Error during attaching file");
            LOGGER.error(message, e);
            throw new ModuleException(message, e);
        } catch (NoSuchAlgorithmException e) {
            String message = String.format("No such algorithm (used on checksum computation)");
            LOGGER.error(message, e);
            throw new ModuleException(message, e);
        }
        return docFiles;
    }

    private void supports(DataType dataType, MultipartFile file) throws ModuleException {

        // Original file must be given
        if ((file.getOriginalFilename() == null) || file.getOriginalFilename().isEmpty()) {
            throw new InvalidOriginalNameException();
        }

        switch (dataType) {
            case DESCRIPTION:
                checkFileSupported(file, StandardCharsets.UTF_8.toString(),
                                   Arrays.asList(MediaType.APPLICATION_PDF_VALUE, MediaType.TEXT_MARKDOWN_VALUE));
                break;
            default:
                // No restriction
                break;
        }
    }

    private void checkFileSupported(MultipartFile file, String expectedCharset, Collection<String> expectedContentTypes)
            throws InvalidCharsetException, InvalidContentTypeException {

        // Check charset
        if (expectedCharset != null) {
            String charset = getCharset(file);
            if (!expectedCharset.equals(charset)) {
                throw new InvalidCharsetException(expectedCharset, charset);
            }
        }

        // Check content types
        if (expectedContentTypes != null) {
            String contentType = getContentType(file);
            if (!expectedContentTypes.contains(contentType)) {
                throw new InvalidContentTypeException(expectedContentTypes, contentType);
            }
        }
    }

    private String getContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            int charsetIdx = contentType.indexOf(";charset");
            return (charsetIdx == -1) ? contentType.trim() : contentType.substring(0, charsetIdx).trim();
        }
        return null;
    }

    private String getCharset(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            int charsetIdx = contentType.indexOf("charset=");
            return (charsetIdx == -1) ? null : contentType.substring(charsetIdx + 8).trim().toUpperCase();
        }
        return null;
    }

    /**
     * This methods tries to delete a local file attached to the document if existing
     * But does not raise any error if the file does not exist on rs-dam
     */
    @Override
    public void removeFile(AbstractEntity<?> entity, DataFile dataFile) throws ModuleException {

        // Retrieve reference
        Optional<LocalFile> localFile = localStorageRepo.findOneByEntityAndFileChecksum(entity,
                                                                                           dataFile.getChecksum());
        if (!localFile.isPresent()) {
            throw new EntityNotFoundException(String.format("Failed to remove the file %s for the document %s",
                                                            dataFile.getName(), entity.getIpId().toString()),
                    LocalFile.class);
        }

        Path filePath = getDataFilePath(dataFile.getChecksum());
        if (Files.isRegularFile(filePath)) {
            // Check rights
            if (!Files.isWritable(filePath)) {
                throw new ModuleException(
                        String.format("Failed to remove the file %s : Permission refused", filePath.toString()));
            }
            // Remove file
            // Do not remove the file if its used by another document
            Long countFiles = localStorageRepo.countByFileChecksum(dataFile.getChecksum());
            if (countFiles == 1) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    String message = String.format("Cannot delete file %s", filePath.toAbsolutePath().toString());
                    LOGGER.error(message, e);
                    throw new ModuleException(message);
                }
            } else {
                LOGGER.info("File %s was not removed on disk since another document uses it", filePath.toString());
            }
            // Remove from database
            localStorageRepo.delete(localFile.get().getId());
        }

    }

    /**
     * Check if the database knows the dataFile
     */
    @Override
    public boolean isFileLocallyStored(AbstractEntity<?> entity, DataFile dataFile) {
        Optional<LocalFile> localFile = localStorageRepo.findOneByEntityAndFileChecksum(entity, dataFile.getChecksum());
        return localFile.isPresent();
    }

    @Override
    public void getFileContent(String checksum, OutputStream output) throws ModuleException {
        Path filePath = getDataFilePath(checksum);
        try {
            Files.copy(filePath, output);
        } catch (IOException e) {
            String message = String.format("Cannot stream file %s", filePath.toAbsolutePath().toString());
            LOGGER.error(message, e);
            throw new ModuleException(message);
        }

    }

    /**
     * Store file in local storage and keep a database reference
     */
    private void store(String checksum, MultipartFile file, AbstractEntity<?> entity) throws IOException {
        // We assume that baseStorageLocation already exists on the file system.
        // We just need to create, if required, the directory between localStoragePath and the file.

        // folder = localStoragePath + 2 first char of checksum
        Path folderPath = Paths.get(localStoragePath, checksum.substring(0, 2));
        if (!Files.exists(folderPath)) {
            Files.createDirectory(folderPath);
        }

        // Save file
        Path filePath = getDataFilePath(checksum);
        // Accept a file to be attached to several entities
        if (!Files.exists(filePath)) {
            Files.copy(file.getInputStream(), filePath);
        }

        // Save reference in database
        LocalFile localStorage = LocalFile.build(entity, checksum);
        localStorageRepo.save(localStorage);
    }

    /**
     * The DataFile path is the DataFileFolder + checksum
     */
    private Path getDataFilePath(String checksum) {
        return getDataFolder(checksum).resolve(checksum);
    }

    /**
     * Lets compute the file folder localStoragePath + 2 first char of checksum
     */
    private Path getDataFolder(String checksum) {
        return Paths.get(localStoragePath, checksum.substring(0, 2));
    }
}
