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
import fr.cnes.regards.modules.feature.dto.FeatureCreationSessionMetadata;

/**
 * Abstract class for creation events
 *
 * @author Kevin Marchois
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE, converter = JsonMessageConverter.GSON)
public class AbstractCreationRequestEvent extends AbstractRequestEvent implements ISubscribable {

    @Valid
    @NotNull(message = "Feature metadata is required")
    private FeatureCreationSessionMetadata metadata;

    public static AbstractCreationRequestEvent build(FeatureCreationSessionMetadata metadata) {
        return build(metadata, OffsetDateTime.now().minusSeconds(1));
    }

    public static AbstractCreationRequestEvent build(FeatureCreationSessionMetadata metadata,
            OffsetDateTime requestDate) {
        AbstractCreationRequestEvent event = new AbstractCreationRequestEvent();
        event.setRequestId(generateRequestId());
        event.setMetadata(metadata);
        event.setRequestDate(requestDate);
        return event;
    }

    public FeatureCreationSessionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FeatureCreationSessionMetadata metadata) {
        this.metadata = metadata;
    }
}
