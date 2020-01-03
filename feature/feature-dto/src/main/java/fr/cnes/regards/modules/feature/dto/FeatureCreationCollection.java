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
public class FeatureCreationCollection extends AbstractFeatureCollection<Feature> {

    @Valid
    private FeatureCreationSessionMetadata metadata;

    /**
     * Create a new {@link FeatureCreationCollection} <br/>
     * @param metadata {@link FeatureCreationSessionMetadata}
     * @param features collection of {@link Feature}
     * @return a {@link FeatureCreationCollection}
     */
    public static FeatureCreationCollection build(FeatureCreationSessionMetadata metadata,
            Collection<Feature> features) {
        FeatureCreationCollection collection = new FeatureCreationCollection();
        collection.setMetadata(metadata);
        collection.addAll(features);
        return collection;
    }

    public FeatureCreationSessionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FeatureCreationSessionMetadata metadata) {
        this.metadata = metadata;
    }
}
