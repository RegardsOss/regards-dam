/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.hateoas.Identifiable;

/**
 * @author lmieulet
 *
 */
public class DataObject implements Identifiable<Long> {

    private final Long id_;

    private FileType fileType_;

    private UniformResourceIdentifier uri_;

    @Override
    public Long getId() {
        return id_;
    }

    /**
     *
     */
    public DataObject() {
        super();
        id_ = (long) ThreadLocalRandom.current().nextInt(1, 1000000);
    }

    /**
     * @param pFileType
     * @param pUri
     */
    public DataObject(FileType pFileType, UniformResourceIdentifier pUri) {
        this();
        fileType_ = pFileType;
        uri_ = pUri;
    }

    /**
     * @return the fileType
     */
    public FileType getFileType() {
        return fileType_;
    }

    /**
     * @param pFileType
     *            the fileType to set
     */
    public void setFileType(FileType pFileType) {
        fileType_ = pFileType;
    }

    /**
     * @return the uri
     */
    public UniformResourceIdentifier getUri() {
        return uri_;
    }

    /**
     * @param pUri
     *            the uri to set
     */
    public void setUri(UniformResourceIdentifier pUri) {
        uri_ = pUri;
    }

}
