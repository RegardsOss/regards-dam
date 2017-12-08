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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import fr.cnes.regards.framework.jpa.json.JsonTypeDescriptor;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.indexer.domain.IDocFiles;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Abstraction for entities managing data files
 *
 * @author lmieulet
 * @author Marc Sordi
 * @author oroussel
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractDataEntity extends AbstractEntity implements IDocFiles {

    /**
     * Physical data file references
     */
    @Type(type = "jsonb", parameters = {
            @Parameter(name = JsonTypeDescriptor.ARG_TYPE, value = "fr.cnes.regards.modules.indexer.domain.DataFile"),
            @Parameter(name = JsonTypeDescriptor.KEY_ARG_TYPE, value = "fr.cnes.regards.framework.oais.urn.DataType") })
    @Column(columnDefinition = "jsonb", name = "files")
    private Multimap<DataType, DataFile> files = HashMultimap.create();

    protected AbstractDataEntity() {
        this(null, null, null);
    }

    protected AbstractDataEntity(Model model, UniformResourceName ipId, String label) {
        super(model, ipId, label);
    }

    public Multimap<DataType, DataFile> getFiles() {
        return files;
    }

    public void setFiles(Multimap<DataType, DataFile> files) {
        this.files = files;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
