/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.*;

import com.google.gson.annotations.JsonAdapter;
import fr.cnes.regards.modules.entities.domain.converter.DescriptionFileAdapter;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class identifying a descriptable entity
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractDescEntity extends AbstractEntity {

    /**
     * Description file
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "description_file_id", foreignKey = @ForeignKey(name = "fk_entity_description_file"))
    @JsonAdapter(value = DescriptionFileAdapter.class)
    private DescriptionFile descriptionFile;

    protected AbstractDescEntity() {
        this(null, null, null);
    }

    protected AbstractDescEntity(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public DescriptionFile getDescriptionFile() {
        return descriptionFile;
    }

    public void setDescriptionFile(DescriptionFile pDescriptionFile) {
        descriptionFile = pDescriptionFile;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
