/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.net.URI;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 * @author lmieulet
 *
 */
public class DataObject extends DataEntity implements IIdentifiable<Long> {

    private FileType fileType;

    private URI uri;

    /**
     *
     */
    public DataObject() {
        super(EntityType.DATA);
    }

    /**
     * @param pFileType
     * @param pUri
     */
    public DataObject(FileType pFileType, URI pUri) {
        this();
        fileType = pFileType;
        uri = pUri;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType pFileType) {
        fileType = pFileType;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI pUri) {
        uri = pUri;
    }

}
