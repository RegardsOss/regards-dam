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
package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.dam.domain.entities.feature.DataObjectFeature;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;

/**
 * Factory for {@link DataObjectFeature} to init according {@link FeatureEntity}
 * @author Kevin Marchois
 *
 */
public interface IDataObjectFeatureFactory {

    /**
     * Get a {@link Page} of {@link DataObjectFeature}
     * The {@link Page} will be initialized from a list of {@link FeatureEntity} match with a model and
     * @param model of {@link FeatureEntity} to extract
     * @param pageable
     * @param date get {@link FeatureEntity} with last update date after this param
     * @return {@link Page} of {@link DataObjectFeature}
     */
    Page<DataObjectFeature> findAll(String model, Pageable pageable, OffsetDateTime date);
}
