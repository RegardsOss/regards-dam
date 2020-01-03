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
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCreationSessionMetadata;
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
    @NotNull(message = "Feature metadata is required")
    private FeatureCreationSessionMetadata metadata;

    @Valid
    @NotNull(message = "Feature is required")
    private Feature feature;

    public static FeatureCreationRequestEvent build(FeatureCreationSessionMetadata metadata, Feature feature) {
        return build(metadata, feature, OffsetDateTime.now().minusSeconds(1));
    }

    public static FeatureCreationRequestEvent build(FeatureCreationSessionMetadata metadata, Feature feature,
            OffsetDateTime requestDate) {
        FeatureCreationRequestEvent event = new FeatureCreationRequestEvent();
        event.setFeature(feature);
        event.setRequestId(generateRequestId());
        event.setMetadata(metadata);
        event.setRequestDate(requestDate);
        return event;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public FeatureCreationSessionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FeatureCreationSessionMetadata metadata) {
        this.metadata = metadata;
    }
}
