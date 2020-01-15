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
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 * @author Marc SORDI
 *
 */
@Entity
@Table(name = "t_feature_creation_request",
        indexes = { @Index(name = "idx_feature_creation_request_id", columnList = AbstractRequest.COLUMN_REQUEST_ID),
                @Index(name = "idx_feature_creation_request_state", columnList = AbstractFeatureRequest.COLUMN_STATE),
                @Index(name = "idx_feature_step_registration_priority",
                        columnList = AbstractRequest.COLUMN_STEP + "," + AbstractRequest.COLUMN_REGISTRATION_DATE + ","
                                + AbstractRequest.COLUMN_PRIORITY),
                @Index(name = "idx_feature_creation_group_id", columnList = AbstractFeatureRequest.GROUP_ID) },
        uniqueConstraints = { @UniqueConstraint(name = "uk_feature_creation_request_id",
                columnNames = { AbstractRequest.COLUMN_REQUEST_ID }) })
public class FeatureCreationRequest extends AbstractFeatureCreationRequest {

    @Column(columnDefinition = "jsonb", name = "feature", nullable = false)
    @Type(type = "jsonb")
    private Feature feature;

    @ManyToOne
    @JoinColumn(name = "feature_id", foreignKey = @ForeignKey(name = "fk_feature_id"))
    private FeatureEntity featureEntity;

    public static FeatureCreationRequest build(String requestId, OffsetDateTime requestDate, RequestState state,
            Set<String> errors, Feature feature, FeatureCreationMetadataEntity metadata, FeatureRequestStep step,
            PriorityLevel priority) {
        Assert.notNull(feature, "Feature is required");
        FeatureCreationRequest request = new FeatureCreationRequest();
        request.with(requestId, requestDate, state, step, priority, errors);
        request.setProviderId(feature.getId());
        request.setFeature(feature);
        request.setMetadata(metadata);
        return request;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public FeatureEntity getFeatureEntity() {
        return featureEntity;
    }

    public void setFeatureEntity(FeatureEntity featureEntity) {
        this.featureEntity = featureEntity;
    }

}
