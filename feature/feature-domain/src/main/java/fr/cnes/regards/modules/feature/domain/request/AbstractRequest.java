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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.framework.jpa.json.JsonTypeDescriptor;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

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

    protected static final String COLUMN_STATE = "state";

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

    @NotNull(message = "Feature request state is required")
    @Enumerated(EnumType.STRING)
    @Column(name = COLUMN_STATE, length = 50, nullable = false)
    private RequestState state;

    @Column(columnDefinition = "jsonb", name = "errors")
    @Type(type = "jsonb", parameters = { @Parameter(name = JsonTypeDescriptor.ARG_TYPE, value = "java.lang.String") })
    private Set<String> errors;

    // FIXME enlever de la super classe si l'update ne concerna pas les ficheirs
    @ManyToOne
    @JoinColumn(name = "feature_id", foreignKey = @ForeignKey(name = "fk_feature_id"))
    private FeatureEntity featureEntity;

    @Column(name = "group_id")
    private String groupId;

    /**
     * All internal request steps including local and remote ones
     */
    @NotNull(message = "Feature request step is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "step", length = 50, nullable = false)
    private FeatureRequestStep step;

    @Embedded
    private FeatureSession session;

    @SuppressWarnings("unchecked")
    protected <T extends AbstractRequest> T with(String requestId, OffsetDateTime requestDate, RequestState state,
            Set<String> errors) {
        Assert.notNull(requestId, "Request id is required");
        Assert.notNull(requestDate, "Request date is required");
        Assert.notNull(state, "Request state is required");
        this.requestId = requestId;
        this.requestDate = requestDate;
        this.registrationDate = OffsetDateTime.now();
        this.state = state;
        this.errors = errors;
        return (T) this;
    }

    public String getRequestId() {
        return requestId;
    }

    public RequestState getState() {
        return state;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        if (errors == null) {
            errors = new HashSet<>();
        }
        errors.add(error);
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

    public void setState(RequestState state) {
        this.state = state;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    public FeatureEntity getFeatureEntity() {
        return featureEntity;
    }

    public void setFeatureEntity(FeatureEntity featureEntity) {
        this.featureEntity = featureEntity;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public FeatureRequestStep getStep() {
        return step;
    }

    public void setStep(FeatureRequestStep step) {
        this.step = step;
    }

    public FeatureSession getSession() {
        return session;
    }

    public void setSession(FeatureSession session) {
        this.session = session;
    }

}
