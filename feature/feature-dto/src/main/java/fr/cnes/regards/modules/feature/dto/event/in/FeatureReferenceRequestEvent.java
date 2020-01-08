/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.feature.dto.event.in;

import java.time.OffsetDateTime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.JsonMessageConverter;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;

/**
 * Request reference for new feature creation from a location
 *
 * @author Kevin Marchois
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE, converter = JsonMessageConverter.GSON)
public class FeatureReferenceRequestEvent extends AbstractRequestEvent implements ISubscribable {

    @NotNull(message = "Url is required")
    private String location;

    @NotNull(message = "Plugin id is required")
    private String pluginBusinessId;

    @Valid
    @NotNull(message = "Feature metadata is required")
    private FeatureSessionMetadata metadata;

    public static FeatureReferenceRequestEvent build(FeatureSessionMetadata metadata, String location,
            String pluginBusinessId) {
        return build(metadata, location, OffsetDateTime.now().minusSeconds(1), pluginBusinessId);
    }

    public static FeatureReferenceRequestEvent build(FeatureSessionMetadata metadata, String location,
            OffsetDateTime requestDate, String pluginBusinessId) {
        FeatureReferenceRequestEvent event = new FeatureReferenceRequestEvent();
        event.setLocation(location);
        event.setRequestId(generateRequestId());
        event.setMetadata(metadata);
        event.setRequestDate(requestDate);
        event.setPluginBusinessId(pluginBusinessId);
        return event;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPluginBusinessId() {
        return pluginBusinessId;
    }

    public void setPluginBusinessId(String pluginBusinessId) {
        this.pluginBusinessId = pluginBusinessId;
    }

    public FeatureSessionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FeatureSessionMetadata metadata) {
        this.metadata = metadata;
    }

}
