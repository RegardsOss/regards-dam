/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.MimeType;

import fr.cnes.regards.framework.oais.urn.DataType;

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
    @NotBlank(message = "Data type is required")
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
    private Integer imageWidth;

    /**
     * Required height if image file
     */
    private Integer imageHeight;

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

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Boolean isOnline() {
        return online;
    }

    /**
     * Please use {@link #isOnline()}
     */
    @Deprecated
    public Boolean getOnline() {
        return isOnline();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        DataFile dataFile = (DataFile) o;

        return uri.equals(dataFile.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
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

    public static DataFile build(DataType dataType, String filename, URI uri, MimeType mimeType, Boolean online,
            Boolean external) {
        return DataFile.build(dataType, filename, uri.toString(), mimeType, online, external);
    }
}
