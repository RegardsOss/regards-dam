/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.notifier.domain;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureManagementAction;
import fr.cnes.regards.modules.notifier.domain.state.NotificationState;

/**
 * Entity to store notification request
 * @author Kevin Marchois
 *
 */
@Entity
@Table(name = "t_notification_request")
public class NotificationAction {

    @Id
    @SequenceGenerator(name = "notificationSequence", initialValue = 1, sequenceName = "seq_notification_request")
    @GeneratedValue(generator = "notificationSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(columnDefinition = "jsonb", name = "feature", nullable = false)
    @Type(type = "jsonb")
    private Feature feature;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeatureManagementAction action;

    @Column(name = "request", nullable = false)
    private OffsetDateTime requestDate;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationState state;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public FeatureManagementAction getAction() {
        return action;
    }

    public void setAction(FeatureManagementAction action) {
        this.action = action;
    }

    public OffsetDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(OffsetDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public Long getId() {
        return id;
    }

    public NotificationState getState() {
        return state;
    }

    public void setState(NotificationState state) {
        this.state = state;
    }

    public static NotificationAction build(Feature feature, FeatureManagementAction action, NotificationState state) {
        NotificationAction toCreate = new NotificationAction();
        toCreate.setAction(action);
        toCreate.setFeature(feature);
        toCreate.setRequestDate(OffsetDateTime.now());
        toCreate.setState(state);

        return toCreate;
    }
}
