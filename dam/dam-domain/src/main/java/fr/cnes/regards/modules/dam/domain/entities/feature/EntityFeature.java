/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.entities.feature;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import fr.cnes.regards.framework.geojson.AbstractFeature;
import fr.cnes.regards.framework.gson.annotation.GsonIgnore;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.ObjectProperty;

/**
 * Public and common entity feature properties
 * @author Marc Sordi
 */
public abstract class EntityFeature extends AbstractFeature<Set<IProperty<?>>, UniformResourceName> {

    /**
     * Submission information package provider identifier
     */
    @NotBlank(message = "Feature provider id is required")
    protected String providerId;

    @NotNull(message = "Feature type is required")
    protected EntityType entityType;

    @NotBlank(message = "Label is required")
    protected String label;

    @NotBlank(message = "Model name is required")
    protected String model;

    /**
     * Related entity files
     */
    @Valid
    protected Multimap<DataType, DataFile> files = HashMultimap.create();

    /**
     * Related entity tags
     */
    protected Set<String> tags = new HashSet<>();

    // To perform quick access to attribute from its name
    @Transient
    @GsonIgnore
    private Map<String, IProperty<?>> propertyMap = null;

    public EntityFeature(UniformResourceName id, String providerId, EntityType entityType, String label) {
        Assert.notNull(entityType, "Entity type is required");
        setId(id);
        this.providerId = providerId;
        this.entityType = entityType;
        this.label = label;
        this.properties = new HashSet<>();
    }

    public void addProperty(IProperty<?> property) {
        this.properties.add(property);
        // If property key is null, it is not a valid property and so it may not pass validation process
        if (property.getName() != null) {
            propertyMap = Maps.uniqueIndex(this.properties, IProperty::getName);
        }
    }

    public IProperty<?> getProperty(String name) {
        if (propertyMap == null) {
            propertyMap = Maps.uniqueIndex(this.properties, IProperty::getName);
        }
        if (!name.contains(".")) {
            return this.propertyMap.get(name);
        } else {
            ObjectProperty fragment = (ObjectProperty) this.propertyMap.get(name.substring(0, name.indexOf('.')));
            String propName = name.substring(name.indexOf('.') + 1);
            if (fragment != null) {
                Optional<IProperty<?>> attOpt = fragment.getValue().stream().filter(p -> p.getName().equals(propName))
                        .findFirst();
                return attOpt.isPresent() ? attOpt.get() : null;
            }
            return null;
        }
    }

    public void removeProperty(IProperty<?> property) {
        this.properties.remove(property);
        propertyMap = Maps.uniqueIndex(this.properties, IProperty::getName);
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Multimap<DataType, DataFile> getFiles() {
        return files;
    }

    public void setFiles(Multimap<DataType, DataFile> files) {
        this.files = files;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void addTags(String... tags) {
        this.tags.addAll(Arrays.asList(tags));
    }

    public void addTags(Collection<String> tags) {
        this.tags.addAll(tags);
    }

    public void removeTags(Collection<String> tags) {
        this.tags.removeAll(tags);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public void setProperties(Set<IProperty<?>> properties) {
        super.setProperties(properties);
        propertyMap = Maps.uniqueIndex(this.properties, IProperty::getName);
    }

}
