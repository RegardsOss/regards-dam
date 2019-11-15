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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.out.RequestState;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.dto.urn.converter.FeatureUrnConverter;

/**
 * @author Kevin Marchois
 *
 */
@Entity
@Table(name = "t_feature_deletion_request",
        indexes = { @Index(name = "idx_feature_creation_request_id", columnList = AbstractRequest.COLUMN_REQUEST_ID),
                @Index(name = "idx_feature_creation_request_state", columnList = AbstractRequest.COLUMN_STATE),
                @Index(name = "idx_feature_step_registration_priority",
                        columnList = AbstractRequest.COLUMN_STEP + "," + AbstractRequest.COLUMN_REGISTRATION_DATE + ","
                                + AbstractRequest.COLUMN_PRIORITY),
                @Index(name = "idx_feature_creation_group_id", columnList = AbstractRequest.GROUP_ID) },
        uniqueConstraints = { @UniqueConstraint(name = "uk_feature_creation_request_id",
                columnNames = { AbstractRequest.COLUMN_REQUEST_ID }) })
public class FeatureDeletionRequest extends AbstractRequest {

    @Id
    @SequenceGenerator(name = "featureUpdateRequestSequence", initialValue = 1,
            sequenceName = "seq_feature_update_request")
    @GeneratedValue(generator = "featureUpdateRequestSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = FeatureUniformResourceName.MAX_SIZE)
    @Convert(converter = FeatureUrnConverter.class)
    private FeatureUniformResourceName urn;

    public static FeatureDeletionRequest build(String requestId, OffsetDateTime requestDate, RequestState state,
            Set<String> errors, FeatureRequestStep step, PriorityLevel priority, FeatureUniformResourceName urn,
            PriorityLevel priorityLevel) {
        FeatureDeletionRequest request = new FeatureDeletionRequest();
        request.with(requestId, requestDate, state, priority, errors);
        request.setStep(step);
        request.setUrn(urn);
        request.setPriority(priorityLevel);

        return request;
    }

    public FeatureUniformResourceName getUrn() {
        return urn;
    }

    public void setUrn(FeatureUniformResourceName urn) {
        this.urn = urn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
