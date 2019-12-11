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

import com.google.gson.JsonElement;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.JsonMessageConverter;
import fr.cnes.regards.framework.amqp.event.Target;

/**
 * An event contain a JSON element plus an action
 * @author Kevin Marchois
 *
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE, converter = JsonMessageConverter.GSON)
public class NotificationActionEvent implements ISubscribable {

    @NotNull(message = "JSON element is required")
    private JsonElement element;

    @NotNull(message = "Notification action is required")
    private String action;

    public JsonElement getElement() {
        return element;
    }

    public void setElement(JsonElement element) {
        this.element = element;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public static NotificationActionEvent build(JsonElement element, String action) {
        NotificationActionEvent toCreate = new NotificationActionEvent();
        toCreate.setAction(action);
        toCreate.setElement(element);
        return toCreate;
    }
}
