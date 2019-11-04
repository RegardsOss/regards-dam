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

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;

/**
 *
 * The sessionOwner and session allows to make groups of features.
 *
 * @author Marc Sordi
 *
 */
public class FeatureMetadata {

    protected static final String MISSING_STORAGE_METADATA = "Storage metadata is required";

    protected static final String MISSING_PRIORITY_LEVEL = "Priority level is required";

    @Valid
    @NotNull(message = MISSING_STORAGE_METADATA)
    private List<StorageMetadata> storages;

    @NotNull(message = MISSING_PRIORITY_LEVEL)
    private PriorityLevel priority;

    public List<StorageMetadata> getStorages() {
        return storages;
    }

    public void setStorages(List<StorageMetadata> storages) {
        this.storages = storages;
    }

    public boolean hasStorage() {
        return !storages.isEmpty();
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(PriorityLevel prioriity) {
        this.priority = prioriity;
    }

    /**

     * Build feature metadata
     * @param storages storage metadata
     */
    public static FeatureMetadata build(PriorityLevel priority, StorageMetadata... storages) {
        return FeatureMetadata.build(priority, Arrays.asList(storages));
    }

    /**
     * Build feature metadata
     * @param storages storage metadata
     */
    public static FeatureMetadata build(PriorityLevel priority, List<StorageMetadata> storages) {
        Assert.notNull(storages, MISSING_STORAGE_METADATA);
        Assert.notNull(priority, MISSING_PRIORITY_LEVEL);
        FeatureMetadata m = new FeatureMetadata();
        m.setStorages(storages);
        m.setPriority(priority);
        return m;
    }
}
