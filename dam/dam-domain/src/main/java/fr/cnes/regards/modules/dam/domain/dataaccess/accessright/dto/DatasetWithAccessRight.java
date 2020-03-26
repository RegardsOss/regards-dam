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
package fr.cnes.regards.modules.dam.domain.dataaccess.accessright.dto;

import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;

/**
 * DTO To handle association between a {@link Dataset} and a {@link AccessRight}
 * @author Sébastien Binda
 *
 */
public class DatasetWithAccessRight {

    /**
     * Internal identifier of the DataSet
     */
    @SuppressWarnings("unused")
    private final UniformResourceName datasetIpId;

    private Dataset dataset;

    /**
     * AccessRight associated to the dataset.
     */
    private AccessRight accessRight;

    public DatasetWithAccessRight(Dataset dataset, AccessRight accessRight) {
        super();
        this.dataset = dataset;
        this.datasetIpId = dataset.getIpId();
        this.accessRight = accessRight;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public AccessRight getAccessRight() {
        return accessRight;
    }

    public void setAccessRight(AccessRight accessRight) {
        this.accessRight = accessRight;
    }

}
