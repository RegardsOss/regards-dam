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

import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.notification.domain.plugin.IRecipientSender;
import fr.cnes.regards.modules.notifier.dto.NotificationEvent8;

/**
 * @author kevin
 *
 */
@Plugin(author = "REGARDS Team", description = "Recipient sender 8 for feature", id = "RecipientSender8",
        version = "1.0.0", contact = "regards@c-s.fr", license = "GPLv3", owner = "CNES",
        url = "https://regardsoss.github.io/")
public class RecipientSender8 implements IRecipientSender, IHandler<NotificationEvent8> {

    @Autowired
    IPublisher publisher;

    @Override
    public boolean send(Feature feature, FeatureManagementAction action) {
        this.publisher.publish(NotificationEvent8.build(feature, action));
        return true;
    }

    @Override
    public void handle(TenantWrapper<NotificationEvent8> wrapper) {
        // TODO Auto-generated method stub

    }

}
