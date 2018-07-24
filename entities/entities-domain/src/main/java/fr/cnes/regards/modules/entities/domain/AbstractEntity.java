/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.gson.annotation.GsonIgnore;
import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.framework.jpa.json.JsonBinaryType;
import fr.cnes.regards.framework.jpa.validator.PastOrNow;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.oais.urn.converters.UrnConverter;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.feature.EntityFeature;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.indexer.domain.IDocFiles;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Base entity feature decorator
 * @param <F> represents the decorated entity feature
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 */
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
@Entity
@Table(name = "t_entity", indexes = { @Index(name = "idx_entity_ipId", columnList = "ipId") },
        uniqueConstraints = @UniqueConstraint(name = "uk_entity_ipId", columnNames = { "ipId" }))
@DiscriminatorColumn(name = "dtype", length = 10)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractEntity<F extends EntityFeature> implements IIndexable, IDocFiles {

    /**
     * Entity id for SGBD purpose mainly and REST request
     */
    @Id
    @SequenceGenerator(name = "EntitySequence", initialValue = 1, sequenceName = "seq_entity")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EntitySequence")
    private Long id;

    /**
     * Information Package ID for REST request
     */
    @Column(nullable = false, length = UniformResourceName.MAX_SIZE)
    @Convert(converter = UrnConverter.class)
    @Valid
    private UniformResourceName ipId;

    /**
     * time at which the entity was created
     */
    @PastOrNow(message = "The creationDate must be in the past or now")
    @Column(name = "creation_date", nullable = false)
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    protected OffsetDateTime creationDate;

    /**
     * last time the entity was updated
     */
    @PastOrNow(message = "The lastUpdate date must be in the past or now")
    @Column(name = "update_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    protected OffsetDateTime lastUpdate;

    /**
     * model that this entity is respecting
     */
    @NotNull(message = "The Model must not be null")
    @ManyToOne
    @JoinColumn(name = "model_id", foreignKey = @ForeignKey(name = "fk_entity_model_id"), nullable = false,
            updatable = false)
    protected Model model;

    /**
     * State determined through different storage steps for the AIP
     */
    @GsonIgnore
    @Column(name = "aip_state", length = 32)
    @Enumerated(EnumType.STRING)
    private EntityAipState stateAip;

    /**
     * Input tags: a tag is either an URN to a collection (ie a direct access collection) or a word without business
     * meaning<br/>
     */
    @ElementCollection
    @CollectionTable(name = "t_entity_tag", joinColumns = @JoinColumn(name = "entity_id"),
            foreignKey = @javax.persistence.ForeignKey(name = "fk_entity_tag_entity_id"))
    @Column(name = "value", length = 200)
    private Set<String> tags = new HashSet<>();

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
     * feature.geometry projection on WGS84 crs
     */
    @Valid
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    protected IGeometry wgs84 = IGeometry.unlocated();

    /**
     * Raw entity feature with minimum fuss
     */
    @Valid
    @NotNull(message = "Feature is required")
    @Column(columnDefinition = "jsonb")
    @Type(type = "jsonb")
    protected F feature;

    protected AbstractEntity(Model model, F feature) {
        this.model = model;
        this.feature = feature;
        if (this.feature != null) {
            this.ipId = feature.getId();
            this.feature.setModel(model.getName());
        }
    }

    protected AbstractEntity() {
        this(null, null);
    }

    @Override
    public String getDocId() {
        return feature.getId().toString();
    }

    @Override
    public String getType() {
        return feature.getEntityType().toString();
    }

    @Override
    public Multimap<DataType, DataFile> getFiles() {
        return feature.getFiles();
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return feature id
     */
    public UniformResourceName getIpId() {
        return feature.getId();
    }

    /**
     * Set the feature id
     */
    public void setIpId(UniformResourceName ipId) {
        this.ipId = ipId;
        // Propagate to feature
        feature.setId(ipId);
    }

    /**
     * Get an immutable copy of tags. To modify tag, use {@link #setTags(Set)} or {@link #addTags(String...)} or
     * {@link #removeTags(Collection)}
     */
    public ImmutableSet<String> getTags() {
        return ImmutableSet.copyOf(tags);
    }

    public EntityAipState getStateAip() {
        return stateAip;
    }

    public void setStateAip(EntityAipState stateAip) {
        this.stateAip = stateAip;
    }

    public void setTags(Set<String> tags) {
        Assert.notEmpty(tags, "Tags must not be null or empty");
        this.tags = tags;
        // Propagate to feature
        feature.setTags(tags);
    }

    public void addTags(String... tags) {
        Assert.notEmpty(tags, "Tags must not be null or empty");
        this.tags.addAll(Arrays.asList(tags));
        // Propagate to feature
        feature.addTags(tags);
    }

    public void removeTags(java.util.Collection<String> tags) {
        Assert.notEmpty(tags, "Tags must not be null or empty");
        this.tags.removeAll(tags);
        // Propagate to feature
        feature.removeTags(tags);
    }

    public void clearTags() {
        this.tags.clear();
        // Propagate to feature
        feature.getTags().clear();
    }

    /**
     * Get an immutable copy of feature properties.
     * If this set should be modified, please use addProperty or removeProperty
     */
    public ImmutableSet<AbstractAttribute<?>> getProperties() {
        return ImmutableSet.copyOf(feature.getProperties());
    }

    /**
     * Get a mutable copy of property paths.
     */
    public Set<String> getMutableCopyOfPropertiesPaths() {
        Set<String> propertiesPaths = new HashSet<>();
        for (AbstractAttribute<?> prop : feature.getProperties()) {
            // Fragment
            if (prop instanceof ObjectAttribute) {
                String fragmentName = prop.getName();
                ((ObjectAttribute) prop).getValue()
                        .forEach(fProp -> propertiesPaths.add(fragmentName + "." + fProp.getName()));
            } else {
                propertiesPaths.add(prop.getName());

            }
        }
        return propertiesPaths;
    }

    /**
     * Add feature property
     */
    public void addProperty(AbstractAttribute<?> property) {
        feature.addProperty(property);
    }

    public void removeProperty(AbstractAttribute<?> property) {
        feature.removeProperty(property);
    }

    public AbstractAttribute<?> getProperty(String name) {
        return feature.getProperty(name);
    }

    /**
     * Set the properties
     */
    public void setProperties(Set<AbstractAttribute<?>> attributes) {
        feature.setProperties(attributes);
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getSipId() {
        return feature.getSipId();
    }

    public void setSipId(String sipId) {
        feature.setSipId(sipId);
    }

    public String getLabel() {
        return feature.getLabel();
    }

    public void setLabel(String label) {
        feature.setLabel(label);
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    @SuppressWarnings("unchecked")
    public <T extends IGeometry> T getGeometry() {
        return (T) feature.getGeometry();
    }

    public void setGeometry(IGeometry geometry) {
        feature.setGeometry(geometry);
    }

    @SuppressWarnings("unchecked")
    public <T extends IGeometry> T getWgs84() {
        return (T) wgs84;
    }

    public void setWgs84(IGeometry wgs84) {
        this.wgs84 = wgs84;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // CHECKSTYLE:OFF
        result = (prime * result) + ((getIpId() == null) ? 0 : getIpId().hashCode());
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
        AbstractEntity<?> other = (AbstractEntity<?>) pObj;
        if (getIpId() == null) {
            if (other.getIpId() != null) {
                return false;
            }
        } else if (!getIpId().equals(other.getIpId())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AbstractEntity [lastUpdate=" + lastUpdate + ", creationDate=" + creationDate + ", id=" + id + ", ipId="
                + getIpId() + ", sipId=" + getSipId() + ", label=" + getLabel() + ", attributes=" + getProperties()
                + ", model=" + model + "]";
    }

    public F getFeature() {
        return feature;
    }
}
