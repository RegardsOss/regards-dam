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
package fr.cnes.regards.modules.notifier.service;

import java.util.List;

import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.notifier.domain.NotificationAction;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationActionEvent;

/**
 * Feature notification service interface
 * @author Kevin Marchois
 *
 */
public interface INotificationRuleService {

    /**
     * Handle a list of {@link NotificationAction} it can be CREATE/UPDATE/DELETE event on a {@link Feature}
     * Check if this event is compliant with a {@link Rule} and in that case notify all {@link Recipient} associated
     * with this {@link Rule}
     * @param toHandles event to handle
     * @return number of notification sended
     */
    public int processRequest(List<NotificationAction> toHandles);

    /**
     * Register {@link NotificationActionEvent} to schedule notifications
     */
    public void registerNotifications(List<NotificationActionEvent> events);

    /**
     * Schedule a job to process a batch of {@link NotificationAction}<br/>
     * @return number of scheduled notification (0 if no request was scheduled)
     */
    int scheduleRequests();
}
