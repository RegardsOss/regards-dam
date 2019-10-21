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
package fr.cnes.regards.modules.feature.dao;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.cnes.regards.modules.feature.domain.request.FeatureUpdateRequest;
import fr.cnes.regards.modules.feature.dto.Feature;

/**
 *
 * @author Marc SORDI
 *
 */
public interface IFeatureUpdateRequestRepository extends JpaRepository<FeatureUpdateRequest, Long> {

    /**
     * Get {@link FeatureUpdateRequest} with a {@link Feature} urn not assigned to an other {@link FeatureUpdateRequest}
     * with it step set to LOCAL_SCHEDULED an ordered by registration date and before a delay
     * @param page contain the number of {@link FeatureUpdateRequest} to return
     * @param delay we want {@link FeatureUpdateRequest} with registration date before this delay
     * @return list of  {@link FeatureUpdateRequest}
     */
    @Query("select request from FeatureUpdateRequest request where request.urn not in ("
            + " select scheduledRequest.urn from FeatureUpdateRequest scheduledRequest"
            + " where scheduledRequest.step = 'LOCAL_SCHEDULED') and request.registrationDate <= :delay order by request.registrationDate ")
    public List<FeatureUpdateRequest> findRequestToSchedule(Pageable page, @Param("delay") OffsetDateTime delay);

}
