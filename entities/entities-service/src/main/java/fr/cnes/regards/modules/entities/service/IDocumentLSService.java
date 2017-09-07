package fr.cnes.regards.modules.entities.service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Document LS (Local Storage)
 * Aims at providing a local storage for Document files before rs-archival-storage
 * saves it permently
 * @author Léo Mieulet
 */
public interface IDocumentLSService {

    /**
     * TODO
     * @param document the document which we are editing
     * @param files the files to attach to the document
     * @param fileLsUriTemplate the file's uri template in order to generate each file's uri
     * @return the set of files
     * @throws ModuleException
     */
    Set<DataFile> handleFiles(Document document, MultipartFile[] files, String fileLsUriTemplate) throws ModuleException;

    void removeFile(DataFile dataFile);
}
