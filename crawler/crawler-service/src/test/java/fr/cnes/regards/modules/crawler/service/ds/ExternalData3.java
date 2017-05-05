package fr.cnes.regards.modules.crawler.service.ds;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import javax.persistence.*;

import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;

/**
 * External Datasource data.<br/>
 * The aim of this entity is to create a table as if it is an external datasource. It contains a prilary key and a date
 * column to be used by CrawlerService ingestion mechanism.
 * @author oroussel
 */
@Entity
@Table(name = "T_DATA_3")
public class ExternalData3 {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime date;

    public ExternalData3() {
    }

    public ExternalData3(OffsetDateTime pDate) {
        date = pDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime pDate) {
        date = pDate;
    }

}
