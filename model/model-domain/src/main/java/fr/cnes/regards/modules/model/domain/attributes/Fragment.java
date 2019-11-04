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
package fr.cnes.regards.modules.model.domain.attributes;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import fr.cnes.regards.framework.module.manager.ConfigIgnore;
import fr.cnes.regards.modules.model.domain.IXmlisable;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * Fragment : gathers a set of attributes and acts as a name space.
 *
 * @author msordi
 *
 */
@Entity
@Table(name = "t_fragment", indexes = { @Index(name = "idx_name", columnList = "name") },
        uniqueConstraints = @UniqueConstraint(name = "uk_fragment_name", columnNames = { "name" }))
@SequenceGenerator(name = "fragmentSequence", initialValue = 1, sequenceName = "seq_fragment")
public class Fragment implements IIdentifiable<Long>, IXmlisable<fr.cnes.regards.modules.model.domain.schema.Fragment> {

    /**
     * Default fragment name
     */
    private static final String DEFAULT_FRAGMENT_NAME = "default";

    /**
     * Default fragment description
     */
    private static final String DEFAULT_FRAGMENT_DESCRIPTION = "Default fragment";

    /**
     * Internal identifier
     */
    @Id
    @ConfigIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fragmentSequence")
    private Long id;

    /**
     * Fragment name
     */
    @NotNull
    @Pattern(regexp = Model.NAME_REGEXP,
            message = "Fragment name must conform to regular expression \"" + Model.NAME_REGEXP + "\".")
    @Size(min = Model.NAME_MIN_SIZE, max = Model.NAME_MAX_SIZE, message = "Fragment name must be between "
            + Model.NAME_MIN_SIZE + " and " + Model.NAME_MAX_SIZE + " length.")
    @Column(nullable = false, updatable = false, length = Model.NAME_MAX_SIZE)
    private String name;

    /**
     * Optional fragment description
     */
    @Column
    @Type(type = "text")
    private String description;

    /**
     * Optional fragment version
     */
    @Size(max = 16, message = "Fragment version must have a maximal size of 16 characters")
    @Column(length = 16)
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public boolean isDefaultFragment() {
        return DEFAULT_FRAGMENT_NAME.equals(name);
    }

    public static Fragment buildDefault() {
        return Fragment.buildFragment(getDefaultName(), DEFAULT_FRAGMENT_DESCRIPTION);
    }

    public static Fragment buildFragment(String pName, String pDescription) {
        final Fragment fragment = new Fragment();
        fragment.setName(pName);
        fragment.setDescription(pDescription);
        return fragment;
    }

    public static String getDefaultName() {
        return DEFAULT_FRAGMENT_NAME;
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
        Fragment other = (Fragment) obj;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public fr.cnes.regards.modules.model.domain.schema.Fragment toXml() {

        // CHECKSTYLE:OFF
        final fr.cnes.regards.modules.model.domain.schema.Fragment xmlFragment = new fr.cnes.regards.modules.model.domain.schema.Fragment();
        // CHECKSTYLE:ON
        xmlFragment.setName(name);
        xmlFragment.setDescription(description);
        xmlFragment.setVersion(version);

        return xmlFragment;
    }

    @Override
    public void fromXml(fr.cnes.regards.modules.model.domain.schema.Fragment pXmlElement) {
        setName(pXmlElement.getName());
        setDescription(pXmlElement.getDescription());
        setVersion(pXmlElement.getVersion());
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String pVersion) {
        version = pVersion;
    }

    @Override
    public String toString() {
        return "Fragment{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
