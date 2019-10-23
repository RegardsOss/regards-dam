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

import com.google.common.collect.Sets;
import fr.cnes.regards.framework.oais.urn.DataType;
import java.util.Set;
import javax.validation.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Metadata for storage
 *
 * @author Marc SORDI
 *
 */
public class StorageMetadata {

    private static final String MISSING_STORAGE_ERROR = "Storage identifier is required";

    private static final String MISSING_TARGET_TYPES = "Data type list should be provided";

    /**
     * Storage identifier.
     * To use a plugin from storage, this identifier must match a plugin configuration business identifier.
     */
    @NotBlank(message = MISSING_STORAGE_ERROR)
    private String pluginBusinessId;

    /**
     * Optional path identifying the base directory in which to store related files
     */
    private String storePath;

    /**
     * List of data object types accepted by this storage location (when storing AIPs)
     */
    @NotNull(message = MISSING_TARGET_TYPES)
    private Set<DataType> targetTypes;

    public String getPluginBusinessId() {
        return pluginBusinessId;
    }

    public void setPluginBusinessId(String pluginBusinessId) {
        this.pluginBusinessId = pluginBusinessId;
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public Set<DataType> getTargetTypes() {
        return targetTypes;
    }

    public void setTargetTypes(Set<DataType> targetTypes) {
        this.targetTypes = targetTypes;
    }

    /**
     * Build storage metadata
     * @param pluginBusinessId storage identifier
     * @param storePath path to the directory in which files have to be stored
     * @param targetTypes list of data type this storage will handle
     */
    public static StorageMetadata build(String pluginBusinessId, @Nullable String storePath, Set<DataType> targetTypes) {
        Assert.hasLength(pluginBusinessId, MISSING_STORAGE_ERROR);
        StorageMetadata m = new StorageMetadata();
        m.setPluginBusinessId(pluginBusinessId);
        m.setStorePath(storePath);
        m.setTargetTypes(targetTypes);
        return m;
    }


    /**
     * Build storage metadata with empty target types and storage path
     * @param pluginBusinessId storage identifier
     */
    public static StorageMetadata build(String pluginBusinessId) {
        Assert.hasLength(pluginBusinessId, MISSING_STORAGE_ERROR);
        StorageMetadata m = new StorageMetadata();
        m.setPluginBusinessId(pluginBusinessId);
        m.setStorePath(null);
        m.setTargetTypes(Sets.newHashSet());
        return m;
    }

}
