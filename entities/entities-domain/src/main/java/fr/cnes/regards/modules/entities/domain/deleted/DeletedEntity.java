package fr.cnes.regards.modules.entities.domain.deleted;

import javax.persistence.*;
import java.time.OffsetDateTime;

import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.oais.urn.converters.UrnConverter;

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

    public DeletedEntity() {
    }

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
