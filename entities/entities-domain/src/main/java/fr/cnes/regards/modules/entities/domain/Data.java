/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.concurrent.ThreadLocalRandom;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.util.MimeType;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 * This class manages physical data reference
 *
 * @author lmieulet
 *
 */
public class Data implements IIdentifiable<Long> {

    @NotNull
    private final Long id;

    @NotNull
    private final String checksum;

    @Min(0)
    private final int fileSize;

    @NotNull
    private final MimeType mimeType;

    @Valid
    private final DataType dataType;

    @Valid
    private final UniformResourceIdentifier fileRef;

    public Data(String pChecksum, int pFileSize, MimeType pMimeType, DataType pDataType,
            UniformResourceIdentifier pFileRef) {
        super();
        id = (long) ThreadLocalRandom.current().nextInt(1, 1000000);
        checksum = pChecksum;
        fileSize = pFileSize;
        mimeType = pMimeType;
        dataType = pDataType;
        fileRef = pFileRef;
    }

    /**
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @return the fileSize
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * @return the mimeType
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /**
     * @return the dataType
     */
    public DataType getDataType() {
        return dataType;
    }

    @Override
    public Long getId() {
        return id;
    }

}
