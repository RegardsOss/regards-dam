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
import javax.persistence.Convert;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;

import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.dto.urn.converter.FeatureUrnConverter;

/**
 * Base class for feature update requests
 *
 * @author Marc SORDI
 *
 */
@MappedSuperclass
public abstract class AbstractFeatureUpdateRequest extends AbstractRequest {

    @Id
    @SequenceGenerator(name = "featureUpdateRequestSequence", initialValue = 1,
            sequenceName = "seq_feature_update_request")
    @GeneratedValue(generator = "featureUpdateRequestSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    @NotBlank(message = "Provider id is required")
    private String providerId;

    /**
     * Information Package ID for REST request
     */
    @Column(nullable = false, length = FeatureUniformResourceName.MAX_SIZE)
    @Convert(converter = FeatureUrnConverter.class)
    private FeatureUniformResourceName urn;

    @ManyToOne
    @JoinColumn(name = "feature_id", foreignKey = @ForeignKey(name = "fk_feature_id"))
    private FeatureEntity featureEntity;

    public Long getId() {
        return this.id;
    }

    public FeatureUniformResourceName getUrn() {
        return urn;
    }

    public void setUrn(FeatureUniformResourceName urn) {
        this.urn = urn;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public FeatureEntity getFeatureEntity() {
        return featureEntity;
    }

    public void setFeatureEntity(FeatureEntity featureEntity) {
        this.featureEntity = featureEntity;
    }

}
