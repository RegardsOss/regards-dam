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

import java.util.ArrayList;
import java.util.List;

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
    private List<FeatureMetadataDto> metadata = new ArrayList<FeatureMetadataDto>();

    @NotNull
    @Valid
    private FeatureSessionDto session;

    public List<FeatureMetadataDto> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<FeatureMetadataDto> metadata) {
        this.metadata = metadata;
    }

    public FeatureSessionDto getSession() {
        return session;
    }

    public void setSession(FeatureSessionDto session) {
        this.session = session;
    }

    /**
     * Create a new {@link FeatureCollection} <br/>
     * @param metadata a list of {@link FeatureMetadataDto} not mandatory
     * @param featureSession a {@link FeatureSessionDto} mandatory
     * @return a {@link FeatureCollection}
     */
    public static FeatureCollection build(List<FeatureMetadataDto> metadata, FeatureSessionDto featureSession) {
        FeatureCollection collection = new FeatureCollection();
        collection.setMetadata(metadata);
        collection.setSession(featureSession);
        return collection;
    }
}
