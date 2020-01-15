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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.util.Assert;

import fr.cnes.regards.framework.jpa.json.JsonBinaryType;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 * @author Marc SORDI
 *
 */
@Entity
@Table(name = "t_feature_update_request",
        indexes = { @Index(name = "idx_feature_update_request_id", columnList = AbstractRequest.COLUMN_REQUEST_ID),
                @Index(name = "idx_feature_update_request_state", columnList = AbstractFeatureRequest.COLUMN_STATE),
                @Index(name = "idx_feature_update_request_urn", columnList = "urn"),
                @Index(name = "idx_feature_update_step_registration_priority",
                        columnList = AbstractRequest.COLUMN_STEP + "," + AbstractRequest.COLUMN_REGISTRATION_DATE + ","
                                + AbstractRequest.COLUMN_PRIORITY) },
        uniqueConstraints = { @UniqueConstraint(name = "uk_feature_update_request_id",
                columnNames = { AbstractRequest.COLUMN_REQUEST_ID }) })
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
public class FeatureUpdateRequest extends AbstractFeatureUpdateRequest {

    @Column(columnDefinition = "jsonb", name = "feature", nullable = false)
    @Type(type = "jsonb")
    private Feature feature;

    public static FeatureUpdateRequest build(String requestId, OffsetDateTime requestDate, RequestState state,
            Set<String> errors, Feature feature, PriorityLevel priority, FeatureRequestStep step) {
        Assert.notNull(feature, "Feature is required");
        FeatureUpdateRequest request = new FeatureUpdateRequest();
        request.with(requestId, requestDate, state, step, priority, errors);
        request.setProviderId(feature.getId());
        request.setUrn(feature.getUrn());
        request.setFeature(feature);
        return request;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

}
