/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.entities.dto;

import java.util.Set;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;

/**
 * DTO for POST request to get DataObject attributes associated to given Datasets.
 * @author sbinda
 */
public class DatasetDataAttributesRequestBody {

    /**
     * Dataset URNs
     */
    private final Set<UniformResourceName> datasetIds = Sets.newHashSet();

    /**
     * Dataset model Ids.
     */
    private Set<String> modelNames = Sets.newHashSet();

    public Set<String> getModelNames() {
        return modelNames;
    }

    /**
     * @return the datasetIds
     */
    public Set<UniformResourceName> getDatasetIds() {
        return datasetIds;
    }

    public void setModelNames(Set<String> modelNames) {
        this.modelNames = modelNames;
    }
}
