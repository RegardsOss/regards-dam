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

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.LightFeatureCreationRequest;

/**
 * @author Marc SORDI
 *
 */
@Repository
public interface ILightFeatureCreationRequestRepository extends JpaRepository<LightFeatureCreationRequest, Long> {

    public Page<LightFeatureCreationRequest> findByStep(FeatureRequestStep step, Pageable page);

    /**
     * Get a page of {@link LightFeatureCreationRequest} with specified step.
     * A creation request cannot be scheduled if one is already scheduled with same provider id.
     * @return a list of {@link FeatureCreationRequest}
     */
    @Query("select request from LightFeatureCreationRequest request where request.providerId not in ("
            + " select scheduledRequest.providerId from LightFeatureCreationRequest scheduledRequest"
            + " where scheduledRequest.step = 'LOCAL_SCHEDULED') and request.step = :step")
    public List<LightFeatureCreationRequest> findRequestsToSchedule(@Param("step") FeatureRequestStep step,
            Pageable page);

    /**
     * Update {@link FeatureRequestStep} step
     * @param step new {@link FeatureRequestStep}
     * @param ids id of {@link FeatureCreationRequest} to update
     */
    @Modifying
    @Query("update LightFeatureCreationRequest fcr set fcr.step = :newStep where fcr.id in :ids ")
    public void updateStep(@Param("newStep") FeatureRequestStep step, @Param("ids") Set<Long> ids);

}
