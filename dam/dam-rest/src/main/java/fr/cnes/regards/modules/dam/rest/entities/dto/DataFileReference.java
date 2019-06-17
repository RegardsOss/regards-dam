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
package fr.cnes.regards.modules.dam.rest.entities.dto;

import javax.validation.constraints.NotNull;

import javax.validation.constraints.NotBlank;
import org.springframework.util.MimeType;

import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * Represents a data file reference.<br/>
 * This class is only used for API and transform to business POJO {@link DataFile} to be store in database.
 *
 * @author Marc Sordi
 *
 */
public class DataFileReference {

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
     * Required filename
     */
    @NotBlank(message = "Filename is required")
    private String filename;

    /**
     * Required width for image file
     */
    private Integer imageWidth;

    /**
     * Required height for image file
     */
    private Integer imageHeight;

    public String getUri() {
        return uri;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Transform DTO in data file
     * @param dataType
     * @return {@link DataFile}
     */
    public DataFile toDataFile(DataType dataType) {
        DataFile dataFile = DataFile.build(dataType, filename, uri, mimeType, Boolean.TRUE, Boolean.TRUE);
        dataFile.setImageWidth(imageWidth);
        dataFile.setImageHeight(imageHeight);
        return dataFile;
    }
}
