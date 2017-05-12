/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.framework.jpa.json.JsonBinaryType;
import fr.cnes.regards.framework.jpa.validator.PastOrNow;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.converter.GeometryAdapter;
import fr.cnes.regards.modules.entities.domain.geometry.Geometry;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.urn.converters.UrnConverter;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Base class for all entities(on a REGARDS point of view)
 *
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 */
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
@Entity
@Table(name = "t_entity", indexes = { @Index(name = "idx_entity_ipId", columnList = "ipId") })
@DiscriminatorColumn(name = "dtype", length = 10)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractEntity implements IIdentifiable<Long>, IIndexable {

    private static final int MAX_IPID_SIZE = 128;

    /**
     * Information Package ID for REST request
     */
    @Column(unique = true, nullable = false, length = MAX_IPID_SIZE)
    @Convert(converter = UrnConverter.class)
    @Valid
    protected UniformResourceName ipId;

    @NotNull
    @Column(length = MAX_IPID_SIZE, nullable = false)
    protected String label;

    /**
     * model that this entity is respecting
     */
    @NotNull
    @ManyToOne
    @JoinColumn(name = "model_id", foreignKey = @ForeignKey(name = "fk_entity_model_id"), nullable = false,
            updatable = false)
    protected Model model;

    /**
     * last time the entity was updated
     */
    @PastOrNow
    @Column(name = "update_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    protected OffsetDateTime lastUpdate;

    /**
     * time at which the entity was created
     */
    @PastOrNow
    @NotNull
    @Column(name = "creation_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    protected OffsetDateTime creationDate;

    /**
     * entity id for SGBD purpose mainly and REST request
     */
    @Id
    @SequenceGenerator(name = "EntitySequence", initialValue = 1, sequenceName = "seq_entity")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EntitySequence")
    protected Long id;

    /**
     * Submission Information Package (SIP): which is the information sent from the producer to the archive used for
     * REST request If no SIP ID is there it means it's not an AIP?
     */
    @Column
    protected String sipId;

    /**
     * Input tags: a tag is either an URN to a collection (ie a direct access collection) or a word without business
     * meaning<br/>
     */
    @ElementCollection
    @CollectionTable(name = "t_entity_tag", joinColumns = @JoinColumn(name = "entity_id"))
    @Column(name = "value", length = 200)
    protected Set<String> tags = new HashSet<>();

    /**
     * Computed indirect access groups.<br/>
     * This is a set of group names that the entity can reach (access groups are positionned on datasets and then added
     * to collections that tag the dataset and then added to collections that tag collections containing groups)
     */
    @ElementCollection
    @CollectionTable(name = "t_entity_group", joinColumns = @JoinColumn(name = "entity_id"))
    @Column(name = "name", length = 200)
    protected Set<String> groups = new HashSet<>();

    /**
     * list of attributes associated to this entity
     */
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Valid
    protected Set<AbstractAttribute<?>> properties = new HashSet<>();

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @JsonAdapter(value = GeometryAdapter.class)
    protected Geometry<?> geometry;

    protected AbstractEntity(Model pModel, UniformResourceName pIpId, String pLabel) { // NOSONAR
        model = pModel;
        ipId = pIpId;
        label = pLabel;
    }

    protected AbstractEntity() { // NOSONAR
        this(null, null, null);
    }

    @Override
    public String getDocId() {
        return ipId.toString();
    }

    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(OffsetDateTime pLastUpdate) {
        lastUpdate = pLastUpdate;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime pCreationDate) {
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

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> pTags) {
        tags = pTags;
    }

    public Set<AbstractAttribute<?>> getProperties() { // NOSONAR
        return properties;
    }

    public void setProperties(Set<AbstractAttribute<?>> pAttributes) {
        properties = pAttributes;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String pLabel) {
        label = pLabel;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> pGroups) {
        groups = pGroups;
    }

    public Geometry<?> getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry<?> pGeometry) {
        geometry = pGeometry;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // CHECKSTYLE:OFF
        result = (prime * result) + ((ipId == null) ? 0 : ipId.hashCode());
        // CHECKSTYLE:ON
        return result;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        AbstractEntity other = (AbstractEntity) pObj;
        if (ipId == null) {
            if (other.getIpId() != null) {
                return false;
            }
        } else
            if (!ipId.equals(other.getIpId())) {
                return false;
            }
        return true;
    }

    @Override
    public String toString() {
        return "AbstractEntity [lastUpdate=" + lastUpdate + ", creationDate=" + creationDate + ", id=" + id + ", ipId="
                + ipId + ", sipId=" + sipId + ", label=" + label + ", attributes=" + properties + ", model=" + model
                + "]";
    }
}
