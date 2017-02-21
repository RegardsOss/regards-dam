/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;

import org.springframework.http.MediaType;

import fr.cnes.regards.modules.entities.domain.converter.MediaTypeConverter;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Embeddable
public class DescriptionFile {

    @Column(name = "description_file_content")
    @Basic(fetch = FetchType.LAZY)
    // this content can be heavy so we don't want to get it all the time so LAZY loading. To do so, this maven plugin is
    // needed : org.hibernate.orm.tooling:hibernate-enhance-maven-plugin
    private byte[] content;

    @Column(name = "description_file_type")
    @Convert(converter = MediaTypeConverter.class)
    @Basic(fetch = FetchType.LAZY)
    // this content can be heavy so we don't want to get it all the time so LAZY loading. To do so, this maven plugin is
    // needed : org.hibernate.orm.tooling:hibernate-enhance-maven-plugin
    private MediaType type;

    protected DescriptionFile() {
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

}
