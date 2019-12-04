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
package fr.cnes.reguards.modules.notifier.dto.in;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.JsonMessageConverter;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;

/**
 * An event contain a JSON element plus an action
 * @author Kevin Marchois
 *
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE, converter = JsonMessageConverter.GSON)
public class NotificationActionEvent implements ISubscribable {

    @NotNull(message = "JSON element is required")
    private Feature feature;

    @NotNull(message = "Notification action is required")
    private FeatureManagementAction action;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public FeatureManagementAction getAction() {
        return action;
    }

    public void setAction(FeatureManagementAction action) {
        this.action = action;
    }

    public static NotificationActionEvent build(Feature feature, FeatureManagementAction action) {
        NotificationActionEvent toCreate = new NotificationActionEvent();
        toCreate.setAction(action);
        toCreate.setFeature(feature);
        return toCreate;
    }
}
