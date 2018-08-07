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
package fr.cnes.regards.modules.dam.domain.entities;

import java.util.HashSet;
import java.util.Set;

import fr.cnes.regards.modules.dam.domain.entities.feature.DataObjectFeature;
import fr.cnes.regards.modules.dam.domain.entities.metadata.DataObjectMetadata;
import fr.cnes.regards.modules.dam.domain.models.Model;

/**
 *
 * Data object feature decorator<br/>
 * A DataObject is created by a DataSource when a data source (external database or AIPs by example) is ingested.
 *
 * @author lmieulet
 * @author Marc Sordi
 * @author oroussel
 * @author Marc Sordi
 */
public class DataObject extends AbstractEntity<DataObjectFeature> {

    /**
     * This field permits to identify which datasource provides it
     */
    private String dataSourceId;

    /**
     * Denormalization : allows to retrieve dataobjects related to models (i.e. types) of dataset
     */
    private Set<Long> datasetModelIds = new HashSet<>();

    /**
     * These metadata are used only by elasticsearch to add useful informations needed by catalog
     */
    private DataObjectMetadata metadata = new DataObjectMetadata();

    /**
     * A data object can be internal (created from AIP) or external (created from external Database).
     * If internal, this means it is managed by storage (in this case, all files are ONLINE or NEARLINE).
     * By default, to provide ascendant compatibility, a DataObject is internal (event if it is not the case on already
     * created dataObjects)
     * @see fr.cnes.regards.modules.indexer.domain.DataFile#online
     */
    private boolean internal = true;

    public DataObject() {
        super(null, null);
    }

    public DataObject(Model model, String tenant, String providerId, String label) {
        super(model, new DataObjectFeature(tenant, providerId, label));
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String pDataSourceId) {
        this.dataSourceId = pDataSourceId;
    }

    public Set<Long> getDatasetModelIds() {
        return datasetModelIds;
    }

    public void setDatasetModelIds(Set<Long> datasetModelIds) {
        this.datasetModelIds = datasetModelIds;
    }

    public DataObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DataObjectMetadata metadata) {
        this.metadata = metadata;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
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
