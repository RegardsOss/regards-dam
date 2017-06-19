/*
 * LICENSE_PLACEHOLDER
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
@Table(name="t_description_file")
public class DescriptionFile {


    @Id
    @SequenceGenerator(name = "DescriptionFileSequence", initialValue = 1, sequenceName = "seq_description_file")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DescriptionFileSequence")
    protected Long id;

    /**
     * Description URL
     */
    private static final String URL_REGEXP = "^https?://.*$";

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
