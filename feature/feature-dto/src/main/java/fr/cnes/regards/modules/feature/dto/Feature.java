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

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.geojson.AbstractFeature;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.model.dto.properties.AbstractProperty;

/**
 * GeoJson feature with dynamic properties based on data model definition<br/>
 * Feature id corresponds to input provider identifier
 *
 * @author Marc SORDI
 *
 */
public class Feature extends AbstractFeature<Set<AbstractProperty<?>>, String> {

    /**
     * Unique feature identifer based on provider identifier with versionning
     */
    private UniformResourceName urn;

    @NotNull(message = "Feature type is required")
    private EntityType entityType;

    @NotBlank(message = "Model name is required")
    protected String model;

    @Valid
    protected List<FeatureFile> files;
}
