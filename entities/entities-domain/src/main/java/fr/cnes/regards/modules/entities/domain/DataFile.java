/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.Valid;

import org.springframework.util.MimeType;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 * This class manages physical data reference
 *
 * @author lmieulet
 *
 */
@Entity
@Table(name = "t_data_file")
public class DataFile implements IIdentifiable<Long> {

    /**
     * Entity identifier
     */
    @Id
    @SequenceGenerator(name = "DataFileSequence", initialValue = 1, sequenceName = "seq_data_file")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DataFileSequence")
    private Long id;

    /**
     * File checksum
     */
    private String checksum;

    /**
     * File size
     */
    private int fileSize;

    /**
     * {@link MimeType}
     */
    private MimeType mimeType;

    /**
     * {@link DataType}
     */
    @Valid
    private DataType dataType;

    /**
     * File reference
     */
    @Valid
    private URI fileRef;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String pChecksum) {
        checksum = pChecksum;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int pFileSize) {
        fileSize = pFileSize;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType pMimeType) {
        mimeType = pMimeType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType pDataType) {
        dataType = pDataType;
    }

    public URI getFileRef() {
        return fileRef;
    }

    public void setFileRef(URI pFileRef) {
        fileRef = pFileRef;
    }
}
