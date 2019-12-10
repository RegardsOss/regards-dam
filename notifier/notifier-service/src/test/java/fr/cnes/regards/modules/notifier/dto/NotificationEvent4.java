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
package fr.cnes.regards.modules.notifier.dto;

import com.google.gson.JsonElement;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.JsonMessageConverter;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.reguards.modules.notifier.dto.NotificationManagementAction;

/**
 * @author kevin
 *
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE, converter = JsonMessageConverter.GSON)
public class NotificationEvent4 implements ISubscribable {

    private JsonElement element;

    private NotificationManagementAction action;

    public JsonElement getElement() {
        return element;
    }

    public void setElement(JsonElement element) {
        this.element = element;
    }

    public NotificationManagementAction getAction() {
        return action;
    }

    public void setAction(NotificationManagementAction action) {
        this.action = action;
    }

    public static NotificationEvent4 build(JsonElement element, NotificationManagementAction action) {
        NotificationEvent4 toCreate = new NotificationEvent4();
        toCreate.setAction(action);
        toCreate.setElement(element);
        return toCreate;
    }
}
