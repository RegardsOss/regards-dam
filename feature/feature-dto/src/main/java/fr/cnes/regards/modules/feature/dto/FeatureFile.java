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
package fr.cnes.regards.modules.feature.dto;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Sets;

/**
 *
 * File description
 *
 * @author Marc SORDI
 *
 */
public class FeatureFile {

    /**
     * File locations (a file can be stored at several locations)
     */
    @Valid
    @NotEmpty(message = "At least one location is required")
    private Set<FeatureFileLocation> locations = new HashSet<>();

    /**
     * File attributes
     */
    @Valid
    @NotNull(message = "File attributes is requred")
    private FeatureFileAttributes attributes;

    public static FeatureFile build(FeatureFileAttributes attributes, FeatureFileLocation... locations) {
        FeatureFile file = new FeatureFile();
        file.setAttributes(attributes);
        file.setLocations(Sets.newHashSet(locations));
        return file;
    }

    public Set<FeatureFileLocation> getLocations() {
        return locations;
    }

    public void setLocations(Set<FeatureFileLocation> locations) {
        this.locations = locations;
    }

    public FeatureFileAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(FeatureFileAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (attributes == null ? 0 : attributes.hashCode());
        result = prime * result + (locations == null ? 0 : locations.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FeatureFile other = (FeatureFile) obj;
        if (attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (locations == null) {
            if (other.locations != null) {
                return false;
            }
        } else if (!locations.equals(other.locations)) {
            return false;
        }
        return true;
    }
}
