/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.concurrent.ThreadLocalRandom;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 * @author lmieulet
 *
 */
public class DataObject implements IIdentifiable<Long> {

    private final Long id;

    private FileType fileType;

    private UniformResourceIdentifier uri;

    /**
     *
     */
    public DataObject() {
        super();
        id = (long) ThreadLocalRandom.current().nextInt(1, 1000000);
    }

    /**
     * @param pFileType
     * @param pUri
     */
    public DataObject(FileType pFileType, UniformResourceIdentifier pUri) {
        this();
        fileType = pFileType;
        uri = pUri;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return the fileType
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * @param pFileType
     *            the fileType to set
     */
    public void setFileType(FileType pFileType) {
        fileType = pFileType;
    }

    /**
     * @return the uri
     */
    public UniformResourceIdentifier getUri() {
        return uri;
    }

    /**
     * @param pUri
     *            the uri to set
     */
    public void setUri(UniformResourceIdentifier pUri) {
        uri = pUri;
    }

}
