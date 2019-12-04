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
package fr.cnes.regards.modules.notifier.dao;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.notifier.domain.NotificationAction;
import fr.cnes.regards.modules.notifier.domain.state.NotificationState;

/**
 * Repository to manipulate {@link NotificationAction}
 * @author Kevin Marchois
 *
 */
@Repository
public interface INotificationActionRepository extends JpaRepository<NotificationAction, Long> {

    @Query("Select request.id From NotificationAction request  "
            + " where request.state = 'DELAYED' Order by request.requestDate")
    public Set<Long> findIdToSchedule(Pageable pageable);

    @Modifying
    @Query("Update NotificationAction request set request.state = :state Where request.id in :ids")
    public void updateState(@Param("state") NotificationState state, @Param("ids") Set<Long> ids);
}
