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
package fr.cnes.regards.modules.feature.domain;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.modules.feature.domain.request.FeatureRequestStep;
import fr.cnes.regards.modules.feature.dto.Feature;

/**
 * @author Marc SORDI
 *
 */
@Entity
@Table(name = "t_feature", indexes = { @Index(name = "idx_feature_last_update", columnList = "last_update") })
public class FeatureEntity {

	@Id
	@SequenceGenerator(name = "featureSequence", initialValue = 1, sequenceName = "seq_feature")
	@GeneratedValue(generator = "featureSequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	@Column(columnDefinition = "jsonb", name = "feature")
	@Type(type = "jsonb")
	@Valid
	private Feature feature;

	@NotNull(message = "Feature request state is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "state", length = 50, nullable = false)
	private FeatureRequestStep state;

	@Column(name = "last_update", nullable = false)
	@Convert(converter = OffsetDateTimeAttributeConverter.class)
	@NotNull
	private OffsetDateTime lastUpdate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public FeatureRequestStep getState() {
		return state;
	}

	public void setState(FeatureRequestStep state) {
		this.state = state;
	}

	public OffsetDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(OffsetDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public static FeatureEntity build(Feature feature, OffsetDateTime lastUpdate, FeatureRequestStep state) {
		FeatureEntity featureEntity = new FeatureEntity();
		featureEntity.setFeature(feature);
		featureEntity.setLastUpdate(lastUpdate);
		featureEntity.setState(state);
		return featureEntity;
	}
}
