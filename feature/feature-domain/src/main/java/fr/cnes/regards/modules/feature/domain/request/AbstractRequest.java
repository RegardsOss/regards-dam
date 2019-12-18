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
package fr.cnes.regards.modules.feature.domain.request;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.springframework.util.Assert;

import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;

/**
 * Common request properties
 *
 * @author Marc SORDI
 *
 */
@MappedSuperclass
public abstract class AbstractRequest {

    protected static final String COLUMN_REQUEST_ID = "request_id";

    protected static final String COLUMN_REQUEST_TIME = "request_date";

    protected static final String COLUMN_REGISTRATION_DATE = "registration_date";

    protected static final String COLUMN_STEP = "step";

    protected static final String COLUMN_PRIORITY = "priority";

    protected static final String URN = "urn";

    @Column(name = COLUMN_REQUEST_ID, length = 36, nullable = false, updatable = false)
    private String requestId;

    /**
     * External request registration date
     */
    @Column(name = COLUMN_REQUEST_TIME, nullable = false, updatable = false)
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime requestDate;

    /**
     * Internal request registration date
     */
    @Column(name = COLUMN_REGISTRATION_DATE, nullable = false, updatable = false)
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime registrationDate;

    /**
     * All internal request steps including local and remote ones
     */
    @NotNull(message = "Feature request step is required")
    @Enumerated(EnumType.STRING)
    @Column(name = COLUMN_STEP, length = 50, nullable = false)
    private FeatureRequestStep step;

    @NotNull(message = "Priority of the request")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = COLUMN_PRIORITY, length = 50, nullable = false)
    private PriorityLevel priority;

    @SuppressWarnings("unchecked")
    protected <T extends AbstractRequest> T with(String requestId, OffsetDateTime requestDate, PriorityLevel priority) {
        Assert.notNull(requestId, "Request id is required");
        Assert.notNull(requestDate, "Request date is required");
        Assert.notNull(priority, "Request priority is required");
        this.requestId = requestId;
        this.requestDate = requestDate;
        this.registrationDate = OffsetDateTime.now();
        this.priority = priority;
        return (T) this;
    }

    public String getRequestId() {
        return requestId;
    }

    public OffsetDateTime getRequestDate() {
        return requestDate;
    }

    public OffsetDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setRequestDate(OffsetDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public void setRegistrationDate(OffsetDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public FeatureRequestStep getStep() {
        return step;
    }

    public void setStep(FeatureRequestStep step) {
        this.step = step;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

}
