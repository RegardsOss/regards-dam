/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import fr.cnes.regards.framework.gson.annotation.Gsonable;
import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.json.JsonBinaryType;
import fr.cnes.regards.framework.jpa.validator.PastOrNow;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.modules.crawler.domain.AbstractIndexable;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.urn.converters.UrnConverter;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Base class for all entities(on a REGARDS point of view)
 *
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
@Entity
@Table(name = "T_ENTITY")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Gsonable
public abstract class AbstractEntity extends AbstractIndexable implements IIdentifiable<Long> {

    /**
     * last time the entity was updated
     */
    @PastOrNow
    @Column(name = "last_update")
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
    @Column(unique = true)
    @Convert(converter = UrnConverter.class)
    @NotNull
    protected UniformResourceName ipId;

    /**
     * Submission Information Package (SIP): which is the information sent from the producer to the archive used for
     * REST request
     *
     * If no SIP ID is there it means it's not an AIP?
     */
    @Column
    protected String sipId;

    /**
     *
     * entities list of tags affected to this entity
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "entity_id", foreignKey = @ForeignKey(name = "FK_ENTITY_TAGS_ID"))
    protected Set<Tag> tags;

    /**
     * list of attribute associated to this entity
     */
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Valid
    protected List<AbstractAttribute<?>> attributes;

    /**
     * model that this entity is respecting
     */
    @NotNull
    @ManyToOne
    // CHECKSTYLE:OFF
    @JoinColumn(name = "model_id", foreignKey = @ForeignKey(name = "FK_ENTITY_MODEL_ID"), nullable = false,
            updatable = false)
    // CHECKSTYLE:ON
    protected Model model;

    @Transient
    private EntityType entityType;

    private AbstractEntity(EntityType pEntityType) { // NOSONAR
        this(null, pEntityType);
    }

    public AbstractEntity(Model pModel, EntityType pEntityType) { // NOSONAR
        super(pEntityType.toString());
        ipId = new UniformResourceName(OAISIdentifier.AIP, pEntityType, JWTService.getActualTenant(), UUID.randomUUID(),
                1);
        super.setDocId(ipId.toString());

        model = pModel;
        creationDate = LocalDateTime.now();
        lastUpdate = LocalDateTime.now();
        tags = new HashSet<>();
    }

    /**
     * @return the lastUpdate
     */
    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @param pLastUpdate
     *            the lastUpdate to set
     */
    public void setLastUpdate(LocalDateTime pLastUpdate) {
        lastUpdate = pLastUpdate;
    }

    /**
     * @return the creationDate
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * @param pCreationDate
     *            the creationDate to set
     */
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

    public UniformResourceName getIpId() {
        return ipId;
    }

    public void setIpId(UniformResourceName pIpId) {
        ipId = pIpId;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> pTags) {
        tags = pTags;
    }

    public List<AbstractAttribute<?>> getAttributes() { // NOSONAR
        return attributes;
    }

    public void setAttributes(List<AbstractAttribute<?>> pAttributes) {
        attributes = pAttributes;
    }

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

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean equals(Object pObj) {
        return (pObj instanceof AbstractEntity) && ((AbstractEntity) pObj).getIpId().equals(getIpId());
    }

    @Override
    public int hashCode() {
        return ipId.hashCode();
    }

}
