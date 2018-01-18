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

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Arrays;

import org.hibernate.annotations.Type;
import org.springframework.http.MediaType;

import fr.cnes.regards.framework.jpa.converter.MediaTypeConverter;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@Table(name = "t_description_file")
public class DescriptionFile {

    /**
     * Description URL
     */
    private static final String URL_REGEXP = "^https?://.*$";

    /**
     * The id
     */
    @Id
    @SequenceGenerator(name = "DescriptionFileSequence", initialValue = 1, sequenceName = "seq_description_file")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DescriptionFileSequence")
    protected Long id;

    /**
     * The description file url
     */
    @Column
    @Type(type = "text")
    @Pattern(regexp = URL_REGEXP,
            message = "Description url must conform to regular expression \"" + URL_REGEXP + "\".")
    protected String url;

    /**
     * The description file content
     */
    @Column(name = "description_file_content")
    private byte[] content;

    /**
     * The description file mime type
     */
    @Column(name = "description_file_type")
    @Convert(converter = MediaTypeConverter.class)
    private MediaType type;

    /**
     * Default constructor
     */
    public DescriptionFile() {
    }

    /**
     * Constructor setting the parameter as attribute
     * @param url
     */
    public DescriptionFile(String url) {
        super();
        this.url = url;
    }

    /**
     * Constructor setting the parameters as attributes
     * @param pContent
     * @param pType
     */
    public DescriptionFile(byte[] pContent, MediaType pType) {
        super();
        content = pContent;
        type = pType;
    }

    /**
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Set the content
     * @param pContent
     */
    public void setContent(byte[] pContent) {
        content = pContent;
    }

    /**
     * @return the type
     */
    public MediaType getType() {
        return type;
    }

    /**
     * Set the type
     * @param pType
     */
    public void setType(MediaType pType) {
        type = pType;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the url
     * @param pDescription
     */
    public void setUrl(String pDescription) {
        url = pDescription;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the id
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass().getSuperclass())) {
            return false;
        }

        DescriptionFile that = (DescriptionFile) o;

        if (getUrl() != null ? !getUrl().equals(that.getUrl()) : that.getUrl() != null) {
            return false;
        }
        if (!Arrays.equals(getContent(), that.getContent())) {
            return false;
        }
        return getType() != null ? getType().equals(that.getType()) : that.getType() == null;
    }

    @Override
    public int hashCode() {
        int result = getUrl() != null ? getUrl().hashCode() : 0;
        result = (31 * result) + Arrays.hashCode(getContent());
        result = (31 * result) + (getType() != null ? getType().hashCode() : 0);
        return result;
    }
}
