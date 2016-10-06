/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.concurrent.ThreadLocalRandom;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.hateoas.Identifiable;
import org.springframework.util.MimeType;

/**
 * @author lmieulet
 *
 */
public class Data implements Identifiable<Long> {

    @NotNull
    private final Long id_;

    @NotNull
    private final String checksum_;

    @Min(0)
    private final int fileSize_;

    @NotNull
    private final MimeType mimeType_;

    @Valid
    private final DataType dataType_;

    @Valid
    private final UniformResourceIdentifier fileRef_;

    /**
     * @param pChecksum
     * @param pFileSize
     * @param pMimeType
     * @param pDataType
     */
    public Data(String pChecksum, int pFileSize, MimeType pMimeType, DataType pDataType,
            UniformResourceIdentifier pFileRef) {
        super();
        id_ = (long) ThreadLocalRandom.current().nextInt(1, 1000000);
        checksum_ = pChecksum;
        fileSize_ = pFileSize;
        mimeType_ = pMimeType;
        dataType_ = pDataType;
        fileRef_ = pFileRef;
    }

    /**
     * @return the checksum
     */
    public String getChecksum() {
        return checksum_;
    }

    /**
     * @return the fileSize
     */
    public int getFileSize() {
        return fileSize_;
    }

    /**
     * @return the mimeType
     */
    public MimeType getMimeType() {
        return mimeType_;
    }

    /**
     * @return the dataType
     */
    public DataType getDataType() {
        return dataType_;
    }

    @Override
    public Long getId() {
        return id_;
    }

}
