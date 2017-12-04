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

import javax.validation.Valid;
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
    @Valid
    private URI uri;

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
     * Is the file online ?
     */
    private Boolean online;

    /**
     * {@link MimeType}
     */
    private MimeType mimeType;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI pFileRef) {
        uri = pFileRef;
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long pFileSize) {
        size = pFileSize;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType pMimeType) {
        mimeType = pMimeType;
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
