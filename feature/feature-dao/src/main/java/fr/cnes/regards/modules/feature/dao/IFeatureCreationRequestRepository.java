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

import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

public interface IFeatureCreationRequestRepository extends JpaRepository<FeatureCreationRequest, Long> {

    public void deleteByIdIn(List<Long> ids);

    public List<FeatureCreationRequest> findByGroupId(String groupId);

    /**
     * Update {@link FeatureCreationRequest} state
     * @param state new {@link RequestState}
     * @param ids id of {@link FeatureCreationRequest} to update
     */
    @Modifying
    @Query("update FeatureCreationRequest fcr set fcr.state = :newState where fcr.id in :ids ")
    public void updateState(@Param("newState") RequestState state, @Param("ids") Set<Long> ids);

    /**
     * Get a page {@link FeatureRequestStep}  at the {@link FeatureRequestStep} in parameter
     * @param step
     * @param page
     * @return a {@link Page} of {@link FeatureCreationRequest}
     */
    public Page<FeatureCreationRequest> findByStep(FeatureRequestStep step, Pageable page);

}
