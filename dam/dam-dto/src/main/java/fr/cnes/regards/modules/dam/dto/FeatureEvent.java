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
package fr.cnes.regards.modules.dam.dto;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;

/**
 * Event to notify data management service about a feature event like creation, update or deletion.
 *
 * @author Sébastien Binda
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE)
public class FeatureEvent implements ISubscribable {

    /**
     * Event type to apply on the associated feature
     */
    private FeatureEventType type;

    /**
     * Identifier of the feature in the current index.
     */
    private String featureId;

    public static FeatureEvent buildFeatureDeleted(String featureId) {
        FeatureEvent event = new FeatureEvent();
        event.type = FeatureEventType.DELETE;
        event.featureId = featureId;
        return event;
    }

    public FeatureEventType getType() {
        return type;
    }

    public String getFeatureId() {
        return featureId;
    }

}
