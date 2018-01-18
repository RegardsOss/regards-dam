package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.util.Collection;

import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * Document LS (Local Storage)
 * Aims at providing a local storage for Document files before rs-archival-storage
 * saves it permently
 *
 * @author Léo Mieulet
 */
public interface IDocumentLSService {

    /**
     * Save several files on disk
     *
     * @param document          the document which we are editing
     * @param files             the files to attach to the document
     * @param fileLsUriTemplate the file's uri template in order to generate each file's uri
     * @return the set of files
     * @throws ModuleException
     */
    Collection<DataFile> handleFiles(Document document, MultipartFile[] files, String fileLsUriTemplate) throws ModuleException;

    /**
     * @param document the document which we are editing
     * @param dataFile the file we're removing
     * @throws IOException
     * @throws EntityNotFoundException
     */
    void removeFile(Document document, DataFile dataFile) throws IOException, EntityNotFoundException;

    /**
     * Return
     *
     * @param document
     * @param dataFile
     * @return true when the database knows this entity
     */
    boolean isFileLocallyStored(Document document, DataFile dataFile);

    /**
     * Read the content of the file and returns it
     *
     * @param doc      document containing the file
     * @param dataFile DataFile concerned
     * @return the file content
     * @throws EntityNotFoundException
     * @throws IOException
     */
    byte[] getDocumentLSContent(Document doc, DataFile dataFile) throws EntityNotFoundException, IOException;
}
