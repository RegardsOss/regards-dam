/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.indexer.domain;

import javax.persistence.*;
import javax.validation.Valid;
import java.net.URI;

import fr.cnes.regards.framework.jpa.converter.MimeTypeConverter;
import fr.cnes.regards.framework.urn.DataType;
import org.springframework.util.MimeType;

/**
 * This class manages physical data reference
 * @author lmieulet
 */
@Entity
@Table(name = "t_data_file")
public class DataFile {

    @Id
    @SequenceGenerator(name = "DataFileSequence", initialValue = 1, sequenceName = "seq_data_file")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DataFileSequence")
    protected Long id;

    /**
     * File reference
     */
    @Valid
    @Column(name = "data_file_ref")
    private URI fileRef;

    /**
     * File checksum
     */
    @Column(name = "checksum")
    private String checksum;

    /**
     * Digest algorithm used to compute file checksum
     */
    @Column(name = "checksum_digest_algorithm")
    private String digestAlgorithm;

    /**
     * File size
     */
    @Column(name = "size")
    private Long fileSize;

    /**
     * {@link MimeType}
     */
    @Column(name = "data_file_mine_type")
    @Convert(converter = MimeTypeConverter.class)
    private MimeType mimeType;


    /**
     * Data type
     */
    @Column(name = "data_file_type")
    private DataType dataType;

    public URI getFileRef() {
        return fileRef;
    }

    public void setFileRef(URI pFileRef) {
        fileRef = pFileRef;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String pChecksum) {
        checksum = pChecksum;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long pFileSize) {
        fileSize = pFileSize;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType pMimeType) {
        mimeType = pMimeType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public DataType getDataType() {
        return dataType;
    }
}
