package fr.cnes.regards.modules.entities.service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.dao.IDocumentLSRepository;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.DocumentLS;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.UriTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * A service to save document files on disk.</br>
 * @author Léo Mieulet
 */
@Service
public class DocumentLSService implements IDocumentLSService {

    private final Logger LOGGER = LoggerFactory.getLogger(DocumentLSService.class);


    private static final Charset STORAGE_ENCODING = StandardCharsets.UTF_8;

    private static final String DIGEST_ALGORITHM = "MD5";

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
        if(!Files.exists(path)) {
            try {
               Files.createDirectory(path);
            } catch (IOException e) {
               throw new InvalidPathException(this.localStoragePath, "Could not create the localStoragePath directory");
            }
        }
    }

    private void saveFile(String checksum, MultipartFile file, Document document) throws URISyntaxException, IOException {
        // Lets compute the filename: baseStorageLocation+3 first char of checksum+checksum
        // We assume that baseStorageLocation already exists on the file system.
        // We just need to create, if required, the directory between localStoragePath and the file.
        String storageLocation = this.localStoragePath + "/" + checksum.substring(0, 3);
        Path path = Paths.get(storageLocation);
        if(!Files.exists(path)) {
            Files.createDirectory(path);
        }

        String fullPathToFile = storageLocation + "/" + checksum;
        saveFileOnDisk(file, fullPathToFile);

        DocumentLS documentLS = new DocumentLS();
        documentLS.setDocument(document);
        documentLS.setFileChecksum(checksum);
        documentFileLocalStorageRepo.save(documentLS);
    }

    private void saveFileOnDisk(MultipartFile file, String fullPathToFile) throws IOException {
        File dest = new File(fullPathToFile);
        try(FileOutputStream fos = new FileOutputStream(dest)) {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            FileCopyUtils.copy(file.getInputStream(), bos);
        }
    }

    private String getChecksum(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        byte[] uploadBytes = file.getBytes();
        MessageDigest md5 = MessageDigest.getInstance(this.DIGEST_ALGORITHM);
        byte[] digest = md5.digest(uploadBytes);
        return new BigInteger(1, digest).toString(16);
    }

    @Override
    public Set<DataFile> handleFiles(Document document, MultipartFile[] files, String fileLsUriTemplate) throws ModuleException {
        Set<DataFile> docFiles = new HashSet<>();
        try {
            for (MultipartFile file : files) {
                if ((file != null) && !file.isEmpty()) {
                    String checksum = this.getChecksum(file);
                    URI fileRef = new UriTemplate(fileLsUriTemplate).expand(checksum);
                    DataFile dataFile = new DataFile();
                    dataFile.setMimeType(MimeType.valueOf(file.getContentType()));
                    dataFile.setSize(file.getSize());
                    dataFile.setDigestAlgorithm(DIGEST_ALGORITHM);
                    dataFile.setChecksum(checksum);
                    this.saveFile(checksum, file, document);
                    dataFile.setUri(fileRef);
                    docFiles.add(dataFile);
                }
            }
        } catch (URISyntaxException e) {
            LOGGER.error("An error occurred while saving documents files", e);
            throw new ModuleException(e.getMessage());
        } catch (IOException e) {
            LOGGER.error("An error occurred while saving documents files", e);
            throw new ModuleException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("No such algorithm (used on checksum computation)", e);
            throw new ModuleException(e.getMessage());
        }
        return docFiles;
    }

    /**
     * This methods tries to delete a local file attached to the document if existing
     * But does not raise any error if the file does not exist on rs-dam
     * @param dataFile
     */
    @Override
    public void removeFile(DataFile dataFile)  {

    }
}
