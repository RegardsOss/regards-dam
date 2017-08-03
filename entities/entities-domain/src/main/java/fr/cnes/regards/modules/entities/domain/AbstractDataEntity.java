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

import java.util.Map;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Abstraction for entities managing data files
 *
 * @author lmieulet
 * @author Marc Sordi
 * @author oroussel
 */
public abstract class AbstractDataEntity extends AbstractEntity {

    /**
     * Physical data file references
     */
    private Map<DataType, DataFile> files;

    protected AbstractDataEntity() {
        this(null, null, null);
    }

    protected AbstractDataEntity(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public Map<DataType, DataFile> getFiles() {
        return files;
    }

    public void setFiles(Map<DataType, DataFile> pFiles) {
        files = pFiles;
    }

    @Override
    public boolean equals(Object pObj) {
        return super.equals(pObj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
