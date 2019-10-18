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
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.JsonMessageConverter;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureMetadataDto;
import fr.cnes.regards.modules.feature.dto.FeatureSessionDto;
import fr.cnes.regards.modules.feature.dto.validation.ValidFeatureEvent;

/**
 * Request for new feature creation using event driven mechanism
 *
 * @author Marc SORDI
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE, converter = JsonMessageConverter.GSON)
@ValidFeatureEvent
public class FeatureCreationRequestEvent extends AbstractRequestEvent implements ISubscribable {

    @Valid
    @NotNull(message = "Feature is required")
    private Feature feature;

    @Valid
    private List<FeatureMetadataDto> metadata = new ArrayList<FeatureMetadataDto>();

    public List<FeatureMetadataDto> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<FeatureMetadataDto> metadata) {
        this.metadata = metadata;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public static FeatureCreationRequestEvent builder(Feature feature, List<FeatureMetadataDto> metadata,
            OffsetDateTime date, FeatureSessionDto session) {
        FeatureCreationRequestEvent event = new FeatureCreationRequestEvent();
        event.setFeature(feature);
        event.setRequestId(generateRequestId());
        event.setMetadata(metadata);
        event.setRequestDate(date);
        event.setSession(session);
        return event;
    }
}
