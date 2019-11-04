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

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;

/**
 * Base class for feature creation requests
 *
 * @author Marc SORDI
 *
 */
@MappedSuperclass
public abstract class AbstractFeatureCreationRequest extends AbstractRequest {

    @Id
    @SequenceGenerator(name = "featureCreationRequestSequence", initialValue = 1,
            sequenceName = "seq_feature_creation_request")
    @GeneratedValue(generator = "featureCreationRequestSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    @NotBlank(message = "Provider id is required")
    private String providerId;

    @Embedded
    private FeatureMetadataEntity metadata;

    public Long getId() {
        return this.id;
    }

    public FeatureMetadataEntity getMetadata() {
        return metadata;
    }

    public void setMetadata(FeatureMetadataEntity metadata) {
        this.metadata = metadata;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
