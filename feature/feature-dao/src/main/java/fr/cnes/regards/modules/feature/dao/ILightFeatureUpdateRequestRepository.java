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
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.LightFeatureUpdateRequest;
import fr.cnes.regards.modules.feature.dto.Feature;

/**
 *
 * @author Marc SORDI
 *
 */
@Repository
public interface ILightFeatureUpdateRequestRepository extends JpaRepository<LightFeatureUpdateRequest, Long> {

    /**
     * Get {@link LightFeatureUpdateRequest} with a {@link Feature} urn not assigned to an other {@link LightFeatureUpdateRequest}
     * with it step set to LOCAL_SCHEDULED an ordered by registration date and before a delay
     * @param page contain the number of {@link LightFeatureUpdateRequest} to return
     * @param delay we want {@link LightFeatureUpdateRequest} with registration date before this delay
     * @return list of {@link LightFeatureUpdateRequest}
     */
    @Query("select request from LightFeatureUpdateRequest request where request.urn not in ("
            + " select scheduledRequest.urn from LightFeatureUpdateRequest scheduledRequest"
            + " where scheduledRequest.step = 'LOCAL_SCHEDULED') and request.registrationDate <= :delay order by request.priority, request.requestDate ")
    public List<LightFeatureUpdateRequest> findRequestsToSchedule(Pageable page, @Param("delay") OffsetDateTime delay);

    /**
     * Update {@link LightFeatureUpdateRequest} step
     * @param step new {@link FeatureRequestStep}
     * @param ids id of {@link LightFeatureUpdateRequest} to update
     */
    @Modifying
    @Query("update LightFeatureUpdateRequest fcr set fcr.step = :newStep where fcr.id in :ids ")
    public void updateStep(@Param("newStep") FeatureRequestStep step, @Param("ids") Set<Long> ids);

}
