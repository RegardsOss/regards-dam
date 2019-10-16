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

import java.util.Set;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.oais.urn.DataType;

/**
 * Extra information useful for Storage submission.<br/>
 *
 * @author Kevin Marchois
 *
 */
public class FeatureMetadataDto {

    /**
     * Storage identifier. To use a plugin from storage, this identifier must match
     * a plugin configuration business identifier.
     */
    @NotNull
    private String storageIdentifier;

    /**
     * Optional path identifying the base directory in which to store related files
     */
    private String subDir;

    /**
     * List of data object types accepted by this storage location (when storing
     * AIPs)
     */
    private Set<DataType> targetTypes;

    public String getStorageIdentifier() {
        return storageIdentifier;
    }

    public void setStorageIdentifier(String storageIdentifier) {
        this.storageIdentifier = storageIdentifier;
    }

    public String getSubDir() {
        return subDir;
    }

    public void setSubDir(String subDir) {
        this.subDir = subDir;
    }

    public Set<DataType> getTargetTypes() {
        return targetTypes;
    }

    public void setTargetTypes(Set<DataType> targetTypes) {
        this.targetTypes = targetTypes;
    }

    public static FeatureMetadataDto builder(String storageIdentifier, String subDir, Set<DataType> targetTypes) {
        FeatureMetadataDto toCreate = new FeatureMetadataDto();
        toCreate.setSubDir(subDir);
        toCreate.setStorageIdentifier(storageIdentifier);
        toCreate.setTargetTypes(targetTypes);

        return toCreate;
    }
}
