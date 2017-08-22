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

import javax.persistence.*;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.Type;
import org.springframework.http.MediaType;

import fr.cnes.regards.modules.entities.domain.converter.MediaTypeConverter;

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

    @Id
    @SequenceGenerator(name = "DescriptionFileSequence", initialValue = 1, sequenceName = "seq_description_file")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DescriptionFileSequence")
    protected Long id;

    @Column
    @Type(type = "text")
    @Pattern(regexp = URL_REGEXP,
            message = "Description url must conform to regular expression \"" + URL_REGEXP + "\".")
    protected String url;

    @Column(name = "description_file_content")
    private byte[] content;

    @Column(name = "description_file_type")
    @Convert(converter = MediaTypeConverter.class)
    private MediaType type;

    public DescriptionFile() {
    }

    public DescriptionFile(String url) {
        super();
        this.url = url;
    }

    public DescriptionFile(byte[] pContent, MediaType pType) {
        super();
        content = pContent;
        type = pType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] pContent) {
        content = pContent;
    }

    public MediaType getType() {
        return type;
    }

    public void setType(MediaType pType) {
        type = pType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String pDescription) {
        url = pDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
