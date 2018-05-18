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

import org.springframework.util.MimeType;

/**
 * This class manages physical data reference
 * @author lmieulet
 */
public class DataFile {

    /**
     * File reference
     */
    protected String uri;

    /**
     * File checksum
     */
    private String checksum;

    /**
     * Digest algorithm used to compute file checksum
     */
    private String digestAlgorithm;

    /**
     * File size
     */
    private Long size;

    /**
     * File name
     */
    private String name;

    /**
     * Is the file online ? (if not, it is NEARLINE)
     * Boolean is used better than boolean because in case DataObject is external (not managed by rs_storage), this
     * concept doesn't exist and so online is null
     */
    private Boolean online;

    /**
     * {@link MimeType}
     */
    private MimeType mimeType;

    private Integer imageWidth;

    private Integer imageHeight;

    /**
     * This field only exists for Gson serialization (used by frontent), it is filled by Catalog after a search through
     * DataObject updating.
     */
    private Boolean downloadable = null;

    public URI getUri() {
        return URI.create(uri);
    }

    public void setUri(URI fileRef) {
        uri = fileRef.toString();
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

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long fileSize) {
        size = fileSize;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
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

    /**
     * Is this file managed internally by regards (in fact storage) and so can be downloaded (regardless user rights) ?
     * Its size must be present if yes
     * @return true if associated file can be downloaded/ordered from Regards (online or nearline)
     */
    public boolean isPhysicallyAvailable() {
        return (online != null) && (size != null) && (size > 0l);
    }

    /**
     * Is this file not managed internally by regards can be downloaded ?
     * @return true is associated file url starts with http or https
     */
    public boolean canBeExternallyDownloaded() {
        return (online == null) && (uri != null) && (uri.startsWith("http") || uri.startsWith("https"));
    }

    /**
     * A DataFile is downloadable IF it is managed by storage (=> online != null) and is ONLINE
     * OR external (ie not managed by storage) and uri starts with http
     */
    public boolean isDownloadable() {
        downloadable = ((online != null) && online) || canBeExternallyDownloaded();
        return downloadable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataFile dataFile = (DataFile) o;

        return uri.equals(dataFile.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
