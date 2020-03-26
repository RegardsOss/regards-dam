/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.net.URI;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.util.MimeType;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.urn.DataType;

/**
 * This class manages data reference. Use {@link #build(DataType, String, String, MimeType, Boolean)} to instanciate it.
 *
 * @author lmieulet
 * @author Marc Sordi
 */
public class DataFile {

    /**
     * Required data type
     */
    @NotNull(message = "Data type is required")
    private DataType dataType;

    /**
     * Required flag to indicate a data file is just a reference (whether physical file is managed internally or not)
     */
    @NotNull(message = "Reference flag is required")
    private Boolean reference;

    /**
     * Required file reference
     */
    @NotBlank(message = "URI is required")
    private String uri;

    /**
     * Required {@link MimeType}
     */
    @NotNull(message = "MIME type is required")
    private MimeType mimeType;

    /**
     * Required width if image file
     */
    private Double imageWidth;

    /**
     * Required height if image file
     */
    private Double imageHeight;

    /**
     * Required field to know if the file is online ? (if not, it is NEARLINE)
     * Boolean is used better than boolean because in case DataObject is external (not managed by rs_storage), this
     * concept doesn't exist and so online is null
     */
    @NotNull(message = "Online flag is required")
    private Boolean online;

    /**
     * Optional file checksum
     */
    private String checksum;

    /**
     * Optional digest algorithm used to compute file checksum
     */
    private String digestAlgorithm;

    /**
     * Optional file size
     */
    private Long filesize;

    /**
     * Required filename
     */
    @NotBlank(message = "Filename is required")
    private String filename;

    /**
     * Custom data file types
     */
    private Set<String> types = Sets.newHashSet();

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Boolean isReference() {
        return reference;
    }

    public void setReference(Boolean reference) {
        this.reference = reference;
    }

    public String getUri() {
        return uri;
    }

    public URI asUri() {
        return URI.create(uri);
    }

    public void setUri(URI uri) {
        this.uri = uri.toString();
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public Double getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Double imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Double getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Double imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Boolean isOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public Long getFilesize() {
        return filesize;
    }

    public void setFilesize(Long filesize) {
        this.filesize = filesize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((checksum == null) ? 0 : checksum.hashCode());
        result = (prime * result) + ((dataType == null) ? 0 : dataType.hashCode());
        result = (prime * result) + ((digestAlgorithm == null) ? 0 : digestAlgorithm.hashCode());
        result = (prime * result) + ((filename == null) ? 0 : filename.hashCode());
        result = (prime * result) + ((mimeType == null) ? 0 : mimeType.hashCode());
        result = (prime * result) + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataFile other = (DataFile) obj;
        if (checksum == null) {
            if (other.checksum != null) {
                return false;
            }
        } else if (!checksum.equals(other.checksum)) {
            return false;
        }
        if (dataType != other.dataType) {
            return false;
        }
        if (digestAlgorithm == null) {
            if (other.digestAlgorithm != null) {
                return false;
            }
        } else if (!digestAlgorithm.equals(other.digestAlgorithm)) {
            return false;
        }
        if (filename == null) {
            if (other.filename != null) {
                return false;
            }
        } else if (!filename.equals(other.filename)) {
            return false;
        }
        if (mimeType == null) {
            if (other.mimeType != null) {
                return false;
            }
        } else if (!mimeType.equals(other.mimeType)) {
            return false;
        }
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }

    /**
     * Base builder with required properties.<br/>
     * For image, size is required, use {@link #setImageWidth(Integer)} and {@link #setImageHeight(Integer)}.<br/>
     * Additional file properties can be supplied using :
     *
     * <ul>
     * <li>{@link #setFilesize(Long)}</li>
     * <li>{@link #setChecksum(String)}</li>
     * <li>{@link #setDigestAlgorithm(String)}</li>
     * </ul>
     *
     * @param dataType the file {@link DataType}
     * @param filename the original filename
     * @param uri the file uri as string
     * @param online true if file can be downloaded
     * @param reference true if file is not managed by REGARDS storage process
     *
     */
    public static DataFile build(DataType dataType, String filename, String uri, MimeType mimeType, Boolean online,
            Boolean reference) {
        DataFile datafile = new DataFile();
        datafile.setDataType(dataType);
        datafile.setFilename(filename);
        datafile.setUri(uri);
        datafile.setMimeType(mimeType);
        datafile.setOnline(online);
        datafile.setReference(reference);
        return datafile;
    }

    /**
     * Base builder with required properties.<br/>
     * For image, size is required, use {@link #setImageWidth(Integer)} and {@link #setImageHeight(Integer)}.<br/>
     * Additional file properties can be supplied using :
     *
     * <ul>
     * <li>{@link #setFilesize(Long)}</li>
     * <li>{@link #setChecksum(String)}</li>
     * <li>{@link #setDigestAlgorithm(String)}</li>
     * </ul>
     *
     * @param dataType the file {@link DataType}
     * @param filename the original filename
     * @param uri the file uri
     * @param online true if file can be downloaded
     * @param reference true if file is not managed by REGARDS storage process
     *
     */
    public static DataFile build(DataType dataType, String filename, URI uri, MimeType mimeType, Boolean online,
            Boolean reference) {
        return DataFile.build(dataType, filename, uri.toString(), mimeType, online, reference);
    }
}
