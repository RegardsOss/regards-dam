package fr.cnes.regards.modules.dam.domain.entities;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.framework.urn.converters.UrnConverter;

/**
 * Deleted entity (collection, dataset, ...).
 * The purpose of this entity is only to keep some informations about deleted entities.
 */
@Entity
@Table(name = "t_deleted_entity",
        uniqueConstraints = @UniqueConstraint(name = "uk_deleted_entity_ipId", columnNames = { "ipId" }))
public class DeletedEntity {

    /**
     * entity id for SGBD purpose mainly and REST request
     */
    @Id
    @SequenceGenerator(name = "EntitySequence", initialValue = 1, sequenceName = "seq_del_entity")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EntitySequence")
    private Long id;

    @Column(name = "creation_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime creationDate;

    @Column(name = "update_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime lastUpdate;

    @Column(name = "deletion_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime deletionDate;

    @Column(nullable = false)
    @Convert(converter = UrnConverter.class)
    private UniformResourceName ipId;

    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(OffsetDateTime pLastUpdate) {
        lastUpdate = pLastUpdate;
    }

    public OffsetDateTime getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(OffsetDateTime pDeletionDate) {
        deletionDate = pDeletionDate;
    }

    public UniformResourceName getIpId() {
        return ipId;
    }

    public void setIpId(UniformResourceName pIpId) {
        ipId = pIpId;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
