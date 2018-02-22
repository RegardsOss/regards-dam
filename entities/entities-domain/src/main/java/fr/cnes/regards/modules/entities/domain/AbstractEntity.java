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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.annotations.JsonAdapter;
import fr.cnes.regards.framework.gson.annotation.GsonIgnore;
import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.framework.jpa.json.JsonBinaryType;
import fr.cnes.regards.framework.jpa.json.JsonTypeDescriptor;
import fr.cnes.regards.framework.jpa.validator.PastOrNow;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.oais.urn.converters.UrnConverter;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.converter.GeometryAdapter;
import fr.cnes.regards.modules.entities.domain.geometry.Geometry;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Base class for all entities(on a REGARDS point of view)
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 */
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
@Entity
@Table(name = "t_entity", indexes = { @Index(name = "idx_entity_ipId", columnList = "ipId") },
        uniqueConstraints = @UniqueConstraint(name = "uk_entity_ipId", columnNames = { "ipId" }))
@DiscriminatorColumn(name = "dtype", length = 10)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractEntity implements IIdentifiable<Long>, IIndexable {

    /**
     * Information Package ID for REST request
     */
    @Column(nullable = false, length = UniformResourceName.MAX_SIZE)
    @Convert(converter = UrnConverter.class)
    @Valid
    protected UniformResourceName ipId;

    /**
     * The entity label
     */
    @NotNull
    @Column(length = 128, nullable = false)
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
    @Column(name = "creation_date", nullable = false)
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
     * REST request.
     */
    @Column
    protected String sipId;

    /**
     * Input tags: a tag is either an URN to a collection (ie a direct access collection) or a word without business
     * meaning<br/>
     */
    @ElementCollection
    @CollectionTable(name = "t_entity_tag", joinColumns = @JoinColumn(name = "entity_id"),
            foreignKey = @javax.persistence.ForeignKey(name = "fk_entity_tag_entity_id"))
    @Column(name = "value", length = 200)
    protected Set<String> tags = new HashSet<>();

    /**
     * Computed indirect access groups.<br/>
     * This is a set of group names that the entity can reach (access groups are positionned on datasets and then added
     * to collections that tag the dataset and then added to collections that tag collections containing groups)
     */
    @ElementCollection
    @CollectionTable(name = "t_entity_group", joinColumns = @JoinColumn(name = "entity_id"),
            foreignKey = @javax.persistence.ForeignKey(name = "fk_entity_group_entity_id"))
    @Column(name = "name", length = 200)
    protected Set<String> groups = new HashSet<>();

    /**
     * list of attributes associated to this entity
     */
    @Type(type = "jsonb", parameters = { @Parameter(name = JsonTypeDescriptor.ARG_TYPE,
            value = "fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute") })
    @Column(columnDefinition = "jsonb")
    @Valid
    protected Set<AbstractAttribute<?>> properties = new HashSet<>();

    // To perform quick access to attribute from its name
    @Transient
    @GsonIgnore
    private Map<String, AbstractAttribute<?>> propertyMap = Collections.emptyMap();

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @JsonAdapter(value = GeometryAdapter.class)
    protected Geometry<?> geometry;

    protected AbstractEntity(Model model, UniformResourceName ipId, String label) { // NOSONAR
        this.model = model;
        this.ipId = ipId;
        this.label = label;
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

    public void setLastUpdate(OffsetDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the ip id
     */
    public UniformResourceName getIpId() {
        return ipId;
    }

    /**
     * Set the ip id
     */
    public void setIpId(UniformResourceName ipId) {
        this.ipId = ipId;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * Get an immutable copy of properties.
     * If this set should be modified, please use addPorperty or removeProperty
     */
    public ImmutableSet<AbstractAttribute<?>> getProperties() { // NOSONAR
        return ImmutableSet.copyOf(properties);
    }

    public void addProperty(AbstractAttribute<?> property) {
        properties.add(property);
        // If property key is null, it is not a valid property and so it may not pass validation process
        if (property.getName() != null) {
            propertyMap = Maps.uniqueIndex(properties, AbstractAttribute::getName);
        }
    }

    public void removeProperty(AbstractAttribute<?> property) {
        properties.remove(property);
        propertyMap = Maps.uniqueIndex(properties, AbstractAttribute::getName);
    }

    public AbstractAttribute<?> getProperty(String name) {
        if (!name.contains(".")) {
            return this.propertyMap.get(name);
        } else {
            ObjectAttribute fragment = (ObjectAttribute) this.propertyMap.get(name.substring(0, name.indexOf('.')));
            String propName = name.substring(name.indexOf('.') + 1);
            if (fragment != null) {
                Optional<AbstractAttribute<?>> attOpt = fragment.getValue().stream()
                        .filter(p -> p.getName().equals(propName)).findFirst();
                return attOpt.isPresent() ? attOpt.get() : null;
            }
            return null;
        }
    }

    /**
     * Set the properties
     */
    public void setProperties(Set<AbstractAttribute<?>> attributes) {
        properties = attributes;
        propertyMap = Maps.uniqueIndex(properties, AbstractAttribute::getName);
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Set the model
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * @return the sip id
     */
    public String getSipId() {
        return sipId;
    }

    /**
     * Set the sip id
     */
    public void setSipId(String sipId) {
        this.sipId = sipId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public Geometry<?> getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry<?> geometry) {
        this.geometry = geometry;
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
        } else if (!ipId.equals(other.getIpId())) {
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
