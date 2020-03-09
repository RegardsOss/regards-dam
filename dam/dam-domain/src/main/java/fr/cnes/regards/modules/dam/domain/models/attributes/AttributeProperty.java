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
package fr.cnes.regards.modules.dam.domain.models.attributes;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.cnes.regards.framework.jpa.IIdentifiable;

/**
 *
 * Custom attribute property
 *
 * @author Marc Sordi
 *
 */
@Entity
@Table(name = "t_attribute_property")
@SequenceGenerator(name = "attPropertySequence", initialValue = 1, sequenceName = "seq_att_ppty")
public class AttributeProperty implements IIdentifiable<Long> {

    /**
     * Key max size
     */
    private static final int KEY_MAX_SIZE = 32;

    /**
     * Resource identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attPropertySequence")
    @Column(name = "id")
    private Long id;

    /**
     * Custom key
     */
    @NotNull
    @Size(max = KEY_MAX_SIZE)
    @Column(name = "ppty_key", length = KEY_MAX_SIZE, nullable = false)
    private String key;

    /**
     * Custom value
     */
    @NotNull
    @Column(name = "ppty_value", nullable = false)
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String pKey) {
        this.key = pKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String pValue) {
        this.value = pValue;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }
}
