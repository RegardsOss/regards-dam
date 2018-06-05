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
package fr.cnes.regards.modules.models.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.models.domain.xml.IXmlisable;

/**
 *
 * Define a model
 *
 * @author msordi
 *
 */
@Entity
@Table(name = "t_model", indexes = { @Index(name = "idx_model_name", columnList = "name") },
        uniqueConstraints = @UniqueConstraint(name = "uk_model_name", columnNames = { "name" }))
@SequenceGenerator(name = "modelSequence", initialValue = 1, sequenceName = "seq_model")
public class Model implements IIdentifiable<Long>, IXmlisable<fr.cnes.regards.modules.models.schema.Model> {

    /**
     * Name regular expression
     */
    public static final String NAME_REGEXP = "[a-zA-Z_][0-9a-zA-Z_]*";

    /**
     * Name min size
     */
    public static final int NAME_MIN_SIZE = 3;

    /**
     * Name max size
     */
    public static final int NAME_MAX_SIZE = 32;

    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modelSequence")
    private Long id;

    /**
     * Model name
     */
    @NotNull
    @Pattern(regexp = NAME_REGEXP, message = "Model name must conform to regular expression \"" + NAME_REGEXP + "\".")
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE,
            message = "Attribute name must be between " + NAME_MIN_SIZE + " and " + NAME_MAX_SIZE + " length.")
    @Column(nullable = false, updatable = false, length = NAME_MAX_SIZE)
    private String name;

    /**
     * Optional model description
     */
    @Column
    @Type(type = "text")
    private String description;

    /**
     * Optional model version
     */
    @Column(length = 16)
    private String version;

    /**
     * Model type
     */
    @NotNull(message="The EntityType must not be null")
    @Column(length = 10, nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private EntityType type;

    public static Model build(String pName, String pDescription, EntityType pModelType) {
        final Model model = new Model();
        model.setName(pName);
        model.setDescription(pDescription);
        model.setType(pModelType);
        return model;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType pType) {
        type = pType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    @Override
    public fr.cnes.regards.modules.models.schema.Model toXml() {
        final fr.cnes.regards.modules.models.schema.Model xmlModel = new fr.cnes.regards.modules.models.schema.Model();
        xmlModel.setName(name);
        xmlModel.setDescription(description);
        xmlModel.setVersion(version);
        xmlModel.setType(type.toString());
        return xmlModel;
    }

    @Override
    public void fromXml(fr.cnes.regards.modules.models.schema.Model pXmlElement) {
        setName(pXmlElement.getName());
        setDescription(pXmlElement.getDescription());
        setVersion(pXmlElement.getVersion());
        setType(EntityType.valueOf(pXmlElement.getType()));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String pVersion) {
        version = pVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Model other = (Model) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Model [id=" + id + ", name=" + name + ", type=" + type + "]";
    }
}
