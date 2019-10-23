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

import fr.cnes.regards.modules.feature.domain.request.FeatureUpdateRequest;
import fr.cnes.regards.modules.feature.dto.FeatureCollection;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureUpdateRequestEvent;

/**
 * This service handles feature update workflow.
 * @author Marc SORDI
 */
public interface IFeatureUpdateService {

    /**
     * Register update requests in database for further processing from incoming request events
     */
    void registerRequests(List<FeatureUpdateRequestEvent> events);

    /**
     * Register update requests in database for further processing from feature collection
     */
    List<FeatureUpdateRequest> registerRequests(FeatureCollection collection);

    /**
     * Schedule a job to process a batch of requests<br/>
     * A delta of time is kept between request registration and processing to manage concurrent updates.
     */
    void scheduleRequests();

    /**
     * Process batch of requests during job
     */
    void processRequests(List<FeatureUpdateRequest> requests);
}
