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
import java.util.concurrent.ExecutionException;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.notifier.domain.NotificationRequest;
import fr.cnes.regards.modules.notifier.domain.Recipient;
import fr.cnes.regards.modules.notifier.domain.Rule;
import fr.cnes.reguards.modules.notifier.dto.in.NotificationRequestEvent;

/**
 * Feature notification service interface
 * @author Kevin Marchois
 *
 */
public interface INotificationRuleService {

    /**
     * Handle a {@link Feature} and check if it matches with enabled {@link Rule} in that case
     * send a notification to a {@link Recipient}
     * @param toHandle {@link Feature} to handle
     * @param action action did of the {@link Feature} to handle
     * @return number of notification sended
     * @throws NotAvailablePluginConfigurationException
     * @throws ModuleException
     * @throws ExecutionException
     */
    public int handleFeature(Feature toHandle, FeatureManagementAction action)
            throws NotAvailablePluginConfigurationException, ModuleException, ExecutionException;

    /**
     * Handle a list of {@link NotificationRequest} it can be CREATE/UPDATE/DELETE event on a {@link Feature}
     * Check if this event is compliant with a {@link Rule} and in that case notify all {@link Recipient} associated
     * with this {@link Rule}
     * @param toHandles event to handle
     * @return number of notification sended
     */
    public int processRequest(List<NotificationRequest> toHandles);

    /**
     * Register {@link NotificationRequestEvent} to schedule notifications
     */
    public void registerNotifications(List<NotificationRequestEvent> events);

    /**
     * Schedule a job to process a batch of {@link NotificationRequest}<br/>
     * @return number of scheduled notification (0 if no request was scheduled)
     */
    int scheduleRequests();
}
