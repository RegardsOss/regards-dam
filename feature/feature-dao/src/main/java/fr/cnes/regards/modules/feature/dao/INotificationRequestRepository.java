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
package fr.cnes.regards.modules.feature.dao;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.domain.request.NotificationRequest;

/**
 * Repository to manipulate {@link NotificationRequest in database}
 * @author Kevin Marchois
 *
 */
@Repository
public interface INotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {

    public Page<NotificationRequest> findByStep(FeatureRequestStep step, Pageable page);

    public void deleteByIdIn(Set<Long> ids);

    /**
     * Update {@link NotificationRequest} step
     * @param step new {@link FeatureRequestStep}
     * @param ids id of {@link NotificationRequest} to update
     */
    @Modifying
    @Query("update NotificationRequest nr set nr.step = :newStep where nr.id in :ids ")
    public void updateStep(@Param("newStep") FeatureRequestStep step, @Param("ids") Set<Long> ids);
}
