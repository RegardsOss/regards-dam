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
package fr.cnes.regards.modules.feature.dto.event.out;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.JsonMessageConverter;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.feature.dto.Feature;

/**
 * @author Kevin Marchois
 *
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE, converter = JsonMessageConverter.GSON)
public class FeatureEvent implements ISubscribable {

    private Feature feature;

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

    public static FeatureEvent build(Feature feature, FeatureManagementAction action) {
        FeatureEvent toCreate = new FeatureEvent();
        toCreate.setAction(action);
        toCreate.setFeature(feature);
        return toCreate;
    }
}
