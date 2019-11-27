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

import fr.cnes.regards.framework.geojson.AbstractFeatureCollection;

/**
 * Feature collection representation based on GeoJson standard structure.
 *
 * @author Kevin Marchois
 *
 */
public class FeatureUpdateCollection extends AbstractFeatureCollection<Feature> {

    @Valid
    private FeatureSessionMetadata metadata;

    /**
     * Create a new {@link FeatureUpdateCollection} <br/>
     * @param metadata {@link FeatureMetadata}
     * @param features collection of {@link Feature}
     * @return a {@link FeatureUpdateCollection}
     */
    public static FeatureUpdateCollection build(FeatureSessionMetadata metadata, Collection<Feature> features) {
        FeatureUpdateCollection collection = new FeatureUpdateCollection();
        collection.setMetadata(metadata);
        collection.addAll(features);
        return collection;
    }

    public FeatureSessionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FeatureSessionMetadata metadata) {
        this.metadata = metadata;
    }
}
