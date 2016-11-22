/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.utils.deserializer.LocalDateTimeDeserializer;
import fr.cnes.regards.framework.jpa.utils.serializer.LocalDateTimeSerializer;
import fr.cnes.regards.framework.jpa.validator.PastOrNow;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Base class for all entities(on a REGARDS point of view)
 *
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
// @TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
// @MappedSuperclass
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractEntity implements IIdentifiable<Long> {

    /**
     * last time the entity was updated
     */
    @PastOrNow
    @Column
    protected LocalDateTime lastUpdate;

    /**
     * time at which the entity was created
     */
    @PastOrNow
    @Column
    protected LocalDateTime creationDate;

    /**
     * entity id for SGBD purpose mainly and REST request
     */
    @Id
    @SequenceGenerator(name = "EntitySequence", initialValue = 1, sequenceName = "SEQ_ENTITY")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EntitySequence")
    protected Long id;

    /**
     * Information Package ID for REST request
     */
    @Column
    protected String ipId;

    /**
     * Submission Information Package (SIP): which is the information sent from the producer to the archive used for
     * REST request
     */
    @NotNull
    @Column
    protected String sipId;

    /**
     * FIXME: element collection means a table of tags for collection, another one for datasets etc, or creating an
     * entity Tag that would mean same table for all those entities knowing that most of tags will be on several type of
     * them
     *
     * entities list of tags affected to this entity
     */
    @Column
    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "ID", foreignKey = @ForeignKey(name = "FK_TAGS_ID")))
    protected List<String> tags;

    /**
     * list of attribute associated to this entity
     */
    // @Type(type = "jsonb")
    // @Column(columnDefinition = "jsonb")
    // protected List<IAttribute<T>> attributes;

    /**
     * model that this entity is respecting
     */
    @ManyToOne(targetEntity = Model.class)
    @JoinColumn(name = "model_id", foreignKey = @ForeignKey(name = "FK_ENTITY_MODEL_ID"), nullable = false,
            updatable = false)
    protected Model model;

    public AbstractEntity() {
        creationDate = LocalDateTime.now();
        lastUpdate = LocalDateTime.now();
    }

    public AbstractEntity(Long pId) {
        this();
        id = pId;
    }

    public AbstractEntity(Model pModel, Long pId) {
        this(pId);
        model = pModel;
    }

    /**
     * @return the lastUpdate
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param pLastUpdate
     *            the lastUpdate to set
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setLastUpdate(LocalDateTime pLastUpdate) {
        lastUpdate = pLastUpdate;
    }

    /**
     * @return the creationDate
     */
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @param pCreationDate
     *            the creationDate to set
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public void setCreationDate(LocalDateTime pCreationDate) {
        creationDate = pCreationDate;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public String getIpId() {
        return ipId;
    }

    public void setIpId(String pIpId) {
        ipId = pIpId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> pTags) {
        tags = pTags;
    }

    // public List<IAttribute<T>> getAttributes() {
    // return attributes;
    // }
    //
    // public void setAttributes(List<IAttribute<T>> pAttributes) {
    // attributes = pAttributes;
    // }

    public Model getModel() {
        return model;
    }

    public void setModel(Model pModel) {
        model = pModel;
    }

    public String getSipId() {
        return sipId;
    }

    public void setSipId(String pSipId) {
        sipId = pSipId;
    }

}
