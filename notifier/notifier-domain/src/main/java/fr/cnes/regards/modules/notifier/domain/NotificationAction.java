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

import com.google.gson.JsonElement;

import fr.cnes.regards.modules.notifier.domain.state.NotificationState;

/**
 * Entity to store notification action
 * @author Kevin Marchois
 *
 */
@Entity
@Table(name = "t_notification_action")
public class NotificationAction {

    @Id
    @SequenceGenerator(name = "notificationSequence", initialValue = 1, sequenceName = "seq_notification_action")
    @GeneratedValue(generator = "notificationSequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(columnDefinition = "jsonb", name = "feature", nullable = false)
    @Type(type = "jsonb")
    private JsonElement element;

    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private String action;

    /** creation date of the instance */
    @Column(name = "action_date", nullable = false)
    private OffsetDateTime actionDate;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationState state;

    public JsonElement getElement() {
        return element;
    }

    public void setElement(JsonElement feature) {
        this.element = feature;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public OffsetDateTime getActionDate() {
        return actionDate;
    }

    public void setActionDate(OffsetDateTime actionDate) {
        this.actionDate = actionDate;
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

    public static NotificationAction build(JsonElement element, String action, NotificationState state) {
        NotificationAction toCreate = new NotificationAction();
        toCreate.setAction(action);
        toCreate.setElement(element);
        toCreate.setActionDate(OffsetDateTime.now());
        toCreate.setState(state);

        return toCreate;
    }
}
