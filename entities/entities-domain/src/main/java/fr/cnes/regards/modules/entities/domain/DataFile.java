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
package fr.cnes.regards.modules.entities.domain;

import javax.validation.Valid;
import java.net.URI;

import org.springframework.util.MimeType;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 * This class manages physical data reference
 *
 * @author lmieulet
 *
 */
public class DataFile implements IIdentifiable<Long> {

    /**
     * Entity identifier
     */
    private Long id;

    /**
     * File checksum
     */
    private String checksum;

    /**
     * File size
     */
    private Long fileSize;

    /**
     * {@link MimeType}
     */
    private MimeType mimeType;

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

    public URI getFileRef() {
        return fileRef;
    }

    public void setFileRef(URI pFileRef) {
        fileRef = pFileRef;
    }
}
