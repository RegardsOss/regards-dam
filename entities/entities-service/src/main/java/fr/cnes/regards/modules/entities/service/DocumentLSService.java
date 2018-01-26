package fr.cnes.regards.modules.entities.service;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.entities.dao.IDocumentLSRepository;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.DocumentLS;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * A service to save document files on disk.</br>
 * @author Léo Mieulet
 */
@Service
public class DocumentLSService implements IDocumentLSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentLSService.class);

    private static final String DIGEST_ALGORITHM = "MD5";

    /**
     * This constant is used by the controller to create an URL that contains a flag that will be replaced by
     * the real checksum here in the service
     */
    public static final String FILE_CHECKSUM_URL_TEMPLATE = "~checksumFlag~";

    @Autowired
    private IDocumentLSRepository documentFileLocalStorageRepo;

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
    public Set<DataFile> handleFiles(Document document, MultipartFile[] files, String fileLsUriTemplate)
            throws ModuleException {
        Set<DataFile> docFiles = new HashSet<>();
        try {
            for (MultipartFile file : files) {
                if ((file != null) && !file.isEmpty()) {
                    String checksum = this.getChecksum(file);
                    URI fileRef = new URI(fileLsUriTemplate.replace(FILE_CHECKSUM_URL_TEMPLATE, checksum));
                    DataFile dataFile = new DataFile();
                    dataFile.setMimeType(MimeType.valueOf(file.getContentType()));
                    dataFile.setSize(file.getSize());
                    dataFile.setDigestAlgorithm(DIGEST_ALGORITHM);
                    dataFile.setChecksum(checksum);
                    dataFile.setName(file.getOriginalFilename());
                    this.saveFile(checksum, file, document);
                    dataFile.setUri(fileRef);
                    docFiles.add(dataFile);
                }
            }
        } catch (URISyntaxException | IOException e) {
            throw new ModuleException("An error occurred while saving documents files", e);
        } catch (NoSuchAlgorithmException e) {
            throw new ModuleException("No such algorithm (used on checksum computation)", e);
        }
        return docFiles;
    }

    /**
     * This methods tries to delete a local file attached to the document if existing
     * But does not raise any error if the file does not exist on rs-dam
     */
    @Override
    public void removeFile(Document document, DataFile dataFile) throws IOException, EntityNotFoundException {
        DocumentLS documentFileLS = getDocumentLS(document, dataFile);
        String dataFilePath = this.getDataFilePath(dataFile.getChecksum());
        File dataFileOnDisk = new File(dataFilePath);
        if (dataFileOnDisk.exists() && dataFileOnDisk.isFile()) {
            if (!dataFileOnDisk.canWrite()) {
                throw new IOException(String.format("Failed to remove the file %s : Permission refused", dataFilePath));
            }
            // Do not remove the file if its used by another document
            Long countFileUsedByDocuments = documentFileLocalStorageRepo.countByFileChecksum(dataFile.getChecksum());
            if (countFileUsedByDocuments == 1) {
                boolean isDeleted = dataFileOnDisk.delete();
                if (isDeleted) {
                    LOGGER.info("File %s removed for document %d", dataFilePath, document.getId());
                    documentFileLocalStorageRepo.delete(documentFileLS.getId());
                } else {
                    throw new IOException(
                            String.format("File %s not removed for document %d", dataFilePath, document.getId()));
                }
            } else {
                LOGGER.info("File %s was removed on filedisk since another document uses it", dataFilePath);
                documentFileLocalStorageRepo.delete(documentFileLS.getId());
            }
        }
    }

    /**
     * Check if the database knows the dataFile
     */
    @Override
    public boolean isFileLocallyStored(Document document, DataFile dataFile) {
        Optional<DocumentLS> documentFileLS = documentFileLocalStorageRepo
                .findOneByDocumentAndFileChecksum(document, dataFile.getChecksum());
        return documentFileLS.isPresent();
    }

    @Override
    public byte[] getDocumentLSContent(Document document, DataFile dataFile)
            throws EntityNotFoundException, IOException {
        DocumentLS documentLS = getDocumentLS(document, dataFile);
        String pathToFile = getDataFilePath(documentLS.getFileChecksum());
        return com.google.common.io.Files.asByteSource(new File(pathToFile)).read();
    }

    private DocumentLS getDocumentLS(Document document, DataFile dataFile) throws EntityNotFoundException {
        Optional<DocumentLS> documentFileLS = documentFileLocalStorageRepo
                .findOneByDocumentAndFileChecksum(document, dataFile.getChecksum());
        if (!documentFileLS.isPresent()) {
            throw new EntityNotFoundException(
                    String.format("Failed to remove the file %s for the document %d", dataFile.getName(),
                                  document.getId()), DocumentLS.class);
        }
        return documentFileLS.get();
    }

    private void saveFile(String checksum, MultipartFile file, Document document) throws IOException {
        // We assume that baseStorageLocation already exists on the file system.
        // We just need to create, if required, the directory between localStoragePath and the file.
        String dataFileFolder = getDataFileFolder(checksum);
        Path path = Paths.get(dataFileFolder);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        String fullPathToFile = getDataFilePath(checksum);
        saveFileOnDisk(file, fullPathToFile);

        DocumentLS documentLS = new DocumentLS();
        documentLS.setDocument(document);
        documentLS.setFileChecksum(checksum);
        documentFileLocalStorageRepo.save(documentLS);
    }

    /**
     * The DataFile path is the DataFileFolder + checksum
     */
    private String getDataFilePath(String checksum) {
        String storageLocation = getDataFileFolder(checksum);
        return storageLocation + "/" + checksum;
    }

    /**
     * Lets compute the file folder localStoragePath + 3 first char of checksum
     */
    private String getDataFileFolder(String checksum) {
        return this.localStoragePath + "/" + checksum.substring(0, 3);
    }

    /**
     * Save the file into the disk
     */
    private void saveFileOnDisk(MultipartFile file, String fullPathToFile) throws IOException {
        File dest = new File(fullPathToFile);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            FileCopyUtils.copy(file.getInputStream(), bos);
        }
    }

    /**
     * Compute the checksum without requiring to save the file
     * Because the checksum is the path to the file
     */
    private String getChecksum(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        byte[] uploadBytes = file.getBytes();
        MessageDigest md5 = MessageDigest.getInstance(DIGEST_ALGORITHM);
        byte[] digest = md5.digest(uploadBytes);
        return new BigInteger(1, digest).toString(16);
    }
}
