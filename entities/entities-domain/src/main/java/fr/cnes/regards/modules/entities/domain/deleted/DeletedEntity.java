package fr.cnes.regards.modules.entities.domain.deleted;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.urn.converters.UrnConverter;

/**
 * Deleted entity (collection, dataset, ...).
 * The purpose of this entity is only to keep some informations about deleted entities.
 */
@Entity
@Table(name = "t_deleted_entity")
public class DeletedEntity {

    /**
     * entity id for SGBD purpose mainly and REST request
     */
    @Id
    @SequenceGenerator(name = "EntitySequence", initialValue = 1, sequenceName = "seq_del_entity")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EntitySequence")
    protected Long id;

    @Column(name = "update_date")
    protected LocalDateTime lastUpdate;

    @Column(name = "deletion_date")
    protected LocalDateTime deletionDate;

    @Column(unique = true, nullable = false)
    @Convert(converter = UrnConverter.class)
    protected UniformResourceName ipId;

    public DeletedEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime pLastUpdate) {
        lastUpdate = pLastUpdate;
    }

    public LocalDateTime getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(LocalDateTime pDeletionDate) {
        deletionDate = pDeletionDate;
    }

    public UniformResourceName getIpId() {
        return ipId;
    }

    public void setIpId(UniformResourceName pIpId) {
        ipId = pIpId;
    }

}
