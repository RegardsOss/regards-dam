/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.entities;

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

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.oais.urn.converters.UrnConverter;

@Entity
@Table(name = "t_entity_request", indexes = { @Index(name = "idx_group_id", columnList = "group_id") },
        uniqueConstraints = { @UniqueConstraint(name = "uk_group_id", columnNames = { "group_id" }) })
public class AbstractEntityRequest {

    /**
     * Entity id for SGBD purpose mainly and REST request
     */
    @Id
    @SequenceGenerator(name = "EntityRequestSequence", initialValue = 1, sequenceName = "seq_entity_request")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EntityRequestSequence")
    private Long id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(nullable = false, length = UniformResourceName.MAX_SIZE)
    @Convert(converter = UrnConverter.class)
    private UniformResourceName urn;

    public AbstractEntityRequest(String groupId, UniformResourceName urn) {
        this.groupId = groupId;
        this.urn = urn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public UniformResourceName getUrn() {
        return urn;
    }

    public void setUrn(UniformResourceName urn) {
        this.urn = urn;
    }

}
