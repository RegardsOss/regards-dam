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
package fr.cnes.regards.modules.notifier.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonElement;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.amqp.event.ISubscribable;

/**
 * @author Marc SORDI
 *
 */
public abstract class AbstractRecipientSender<E extends ISubscribable> implements IRecipientNotifier, IHandler<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRecipientSender.class);

    private int count = 0;

    @Autowired
    IPublisher publisher;

    abstract E buildEvent(JsonElement element, String action);

    @Override
    public boolean send(JsonElement element, String action) {
        this.publisher.publish(buildEvent(element, action));
        return true;
    }

    @Override
    public void handle(TenantWrapper<E> wrapper) {
        count++;
        LOGGER.debug("{} message(s) received in class {}", count, this.getClass().getName());
    }
}
