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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 * Contain a reference to to file plus a plugin buniness id
 * @author Kevin Marchois
 *
 */
@Entity
@Table(name = "t_feature_reference_request",
        indexes = { @Index(name = "idx_feature_reference_request_step", columnList = AbstractRequest.COLUMN_STEP) },
        uniqueConstraints = { @UniqueConstraint(name = "uk_feature_reference_request_id",
                columnNames = { AbstractRequest.COLUMN_REQUEST_ID }) })
public class FeatureReferenceRequest extends AbstractRequest {

    @Id
    @SequenceGenerator(name = "featureCreationRequestSequence", initialValue = 1,
            sequenceName = "seq_feature_creation_request")
    @GeneratedValue(generator = "featureCreationRequestSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Embedded
    private FeatureMetadataEntity metadata;

    @NotNull
    @Column(name = "location", nullable = false)
    private String location;

    @NotNull
    @Column(name = "plugin_business_id", nullable = false)
    private String pluginBusinessId;

    public static FeatureReferenceRequest build(String requestId, OffsetDateTime requestDate, RequestState state,
            FeatureMetadataEntity metadata, FeatureRequestStep step, PriorityLevel priority, String location,
            String pluginBusinessId) {
        FeatureReferenceRequest request = new FeatureReferenceRequest();
        request.setMetadata(metadata);
        request.setStep(step);
        request.setLocation(location);
        request.setPluginBusinessId(pluginBusinessId);
        request.setPriority(priority);
        request.setState(state);
        request.setRequestId(requestId);
        request.setRegistrationDate(OffsetDateTime.now());
        request.setRequestDate(requestDate);

        return request;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FeatureMetadataEntity getMetadata() {
        return metadata;
    }

    public void setMetadata(FeatureMetadataEntity metadata) {
        this.metadata = metadata;
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

}
