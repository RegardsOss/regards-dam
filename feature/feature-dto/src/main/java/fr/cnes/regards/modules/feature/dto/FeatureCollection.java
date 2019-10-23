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
package fr.cnes.regards.modules.feature.dto;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.geojson.AbstractFeatureCollection;

/**
 * Feature collection representation based on GeoJson standard structure.
 *
 * @author Kevin Marchois
 *
 */
public class FeatureCollection extends AbstractFeatureCollection<Feature> {

    @Valid
    @NotNull(message = "Feature metadata is required")
    private FeatureMetadata metadata;

    /**
     * Create a new {@link FeatureCollection} <br/>
     * @param metadata {@link FeatureMetadata}
     * @param features collection of {@link Feature}
     * @return a {@link FeatureCollection}
     */
    public static FeatureCollection build(FeatureMetadata metadata, Collection<Feature> features) {
        FeatureCollection collection = new FeatureCollection();
        collection.setMetadata(metadata);
        collection.addAll(features);
        return collection;
    }

    public FeatureMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FeatureMetadata metadata) {
        this.metadata = metadata;
    }
}
