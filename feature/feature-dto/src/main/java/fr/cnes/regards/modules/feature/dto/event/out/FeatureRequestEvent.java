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
package fr.cnes.regards.modules.feature.dto.event.out;

import java.util.Set;

import org.springframework.lang.Nullable;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.JsonMessageConverter;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;

/**
 * Ingest request event
 *
 * @author Marc SORDI
 *
 */
@Event(target = Target.ONE_PER_MICROSERVICE_TYPE, converter = JsonMessageConverter.GSON)
public class FeatureRequestEvent implements ISubscribable {

    /**
     * The request id
     */
    private String requestId;

    /**
     * The feature id
     */
    private String id;

    /**
     * The feature URN
     */
    private FeatureUniformResourceName urn;

    private RequestState state;

    private Set<String> errors;

    public static FeatureRequestEvent build(String requestId, String id, @Nullable FeatureUniformResourceName urn,
            RequestState state) {
        return build(requestId, id, urn, state, null);
    }

    public static FeatureRequestEvent build(String requestId, @Nullable String id,
            @Nullable FeatureUniformResourceName urn, RequestState state, Set<String> errors) {
        FeatureRequestEvent event = new FeatureRequestEvent();
        event.setRequestId(requestId);
        event.setId(id);
        event.setUrn(urn);
        event.setState(state);
        event.setErrors(errors);

        return event;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public FeatureUniformResourceName getUrn() {
        return urn;
    }

    public void setUrn(FeatureUniformResourceName urn) {
        this.urn = urn;
    }

}
