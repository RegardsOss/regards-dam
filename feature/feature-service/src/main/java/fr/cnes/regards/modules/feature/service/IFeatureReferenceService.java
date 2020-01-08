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

import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.feature.domain.request.FeatureReferenceRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureReferenceRequestEvent;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 * Service for deleting Features
 * @author Kevin Marchois
 *
 */
public interface IFeatureReferenceService {

    /**
     * Register {@link FeatureReferenceRequest}in database for further processing from incoming request events
     */
    RequestInfo<String> registerRequests(List<FeatureReferenceRequestEvent> events);

    /**
     * Schedule a job to process a batch of requests<br/>
     * @return number of scheduled {@link FeatureReferenceRequest}
     */
    int scheduleRequests();

    /**
     * Process batch of {@link FeatureReferenceRequest} during job
     * We will call referenced plugin to obtain a {@link Feature} and publish it to create it
     * @throws ModuleException
     */
    void processRequests(List<FeatureReferenceRequest> requests) throws ModuleException;

    /**
     * Set {@link RequestState} ERROR to {@link FeatureReferenceRequest}
     * @param featureReferenceRequests list to set in error
     */
    void setErrorStatus(List<FeatureReferenceRequest> featureReferenceRequests);
}
