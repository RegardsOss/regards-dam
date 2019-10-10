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
package fr.cnes.regards.modules.feature.service;

import java.util.List;

import fr.cnes.regards.modules.feature.dto.event.in.FeatureUpdateRequestEvent;

/**
 * This service handles feature update workflow.
 * @author Marc SORDI
 */
public interface IFeatureUpdateService {

    /**
     * Register update requests in database for further processing
     */
    void registerUpdateRequests(List<FeatureUpdateRequestEvent> items);

    /**
     * Schedule update request processing.<br/>
     * A delta of time is kept between request registration and processing to manage concurrent updates.
     */
    void scheduleUpdateRequestProcessing();
}
