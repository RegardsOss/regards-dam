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
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.util.Assert;

import fr.cnes.regards.framework.jpa.json.JsonBinaryType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.oais.urn.converters.UrnConverter;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;

/**
 * @author Marc SORDI
 *
 */
@Entity
@Table(name = "t_feature_update_request",
        indexes = { @Index(name = "idx_feature_update_request_id", columnList = AbstractRequest.COLUMN_REQUEST_ID),
                @Index(name = "idx_feature_update_request_state", columnList = AbstractRequest.COLUMN_STATE) },
        uniqueConstraints = { @UniqueConstraint(name = "uk_feature_update_request_id",
                columnNames = { AbstractRequest.COLUMN_REQUEST_ID }) })
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
public class FeatureUpdateRequest extends AbstractRequest {

    @Id
    @SequenceGenerator(name = "featureUpdateRequestSequence", initialValue = 1,
            sequenceName = "seq_feature_update_request")
    @GeneratedValue(generator = "featureUpdateRequestSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * Information Package ID for REST request
     */
    @Column(nullable = false, length = UniformResourceName.MAX_SIZE)
    @Convert(converter = UrnConverter.class)
    private UniformResourceName ipId;

    @Column(columnDefinition = "jsonb", name = "feature", nullable = false)
    @Type(type = "jsonb")
    private Feature feature;

    @ManyToOne
    @JoinColumn(name = "feature_id")
    private FeatureEntity featureEntity;

    /**
     * All internal request steps including local and remote ones
     */
    @NotNull(message = "Feature request step is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "step", length = 50, nullable = false)
    private FeatureRequestStep step;

    @Column(name = "group_id")
    private String groupId;

    public Feature getFeature() {
        return this.feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Long getId() {
        return this.id;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public FeatureEntity getFeatureEntity() {
        return featureEntity;
    }

    public void setFeatureEntity(FeatureEntity featureEntity) {
        this.featureEntity = featureEntity;
    }

    public static FeatureUpdateRequest build(String requestId, OffsetDateTime requestDate, RequestState state,
            Set<String> errors, Feature feature) {
        Assert.notNull(feature, "Feature is required");
        FeatureUpdateRequest fcr = new FeatureUpdateRequest();
        fcr.with(requestId, requestDate, state, errors);
        fcr.setFeature(feature);
        return fcr;
    }
}
