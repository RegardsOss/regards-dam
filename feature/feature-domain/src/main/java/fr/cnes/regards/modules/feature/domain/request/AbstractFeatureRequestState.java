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
package fr.cnes.regards.modules.feature.domain.request;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 * Super class containing step for feature request class
 * @author kevin
 *
 */
@MappedSuperclass
public class AbstractFeatureRequestState extends AbstractRequest {

    protected static final String COLUMN_STATE = "state";

    @NotNull(message = "Feature request state is required")
    @Enumerated(EnumType.STRING)
    @Column(name = COLUMN_STATE, length = 50, nullable = false)
    private RequestState state;

    /**
     * @param requestId
     * @param requestDate
     * @param priority
     * @param state
     */
    public void with(String requestId, OffsetDateTime requestDate, PriorityLevel priority, RequestState state) {
        super.with(requestId, requestDate, priority);
        this.state = state;
    }

    public RequestState getState() {
        return state;
    }

    public void setState(RequestState state) {
        this.state = state;
    }

}
