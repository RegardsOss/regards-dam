package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * Document LS (Local Storage)
 * Aims at providing a local storage for Document files before rs-archival-storage
 * saves it permently
 *
 * @author Léo Mieulet
 */
public interface ILocalStorageService {

    /**
     * Save several files on disk
     *
     * @param entity the entity which we are editing
     * @param dataType data type
     * @param attachments the files to attach to the document
     * @param fileUriTemplate the file's uri template in order to generate each file's uri
     * @return the set of files
     */
    Collection<DataFile> attachFiles(AbstractEntity<?> entity, DataType dataType, MultipartFile[] attachments,
            String fileUriTemplate) throws ModuleException;

    /**
     * @param entity the entity which we are editing
     * @param dataFile the file we're removing
     * @throws IOException
     * @throws EntityNotFoundException
     */
    void removeFile(AbstractEntity<?> entity, DataFile dataFile) throws ModuleException;

    /**
     * @return true when the database knows this entity
     */
    boolean isFileLocallyStored(AbstractEntity<?> entity, DataFile dataFile);

    /**
     * Write the file in the output stream.<br/>
     * {@link OutputStream} has to be flush after this method completes.
     */
    void getFileContent(String checksum, OutputStream output) throws ModuleException;
}
