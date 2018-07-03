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
package fr.cnes.regards.modules.entities.domain;

import java.util.HashSet;
import java.util.Set;

import fr.cnes.regards.modules.entities.domain.feature.DataObjectFeature;
import fr.cnes.regards.modules.entities.domain.metadata.DataObjectMetadata;
import fr.cnes.regards.modules.models.domain.Model;

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

    public DataObject(Model model, String tenant, String label) {
        super(model, new DataObjectFeature(tenant, label));
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

    // FIXME remove
    // /**
    // * Update both containsPhysicalData and canBeExternallyDownloaded properties on DataObject AND downloadable
    // property
    // * on all associated files.
    // * Theses properties are needed by frontend
    // */
    // public void updateJsonSpecificProperties() {
    // containsPhysicalData = containsPhysicalData();
    // canBeExternallyDownloaded = canBeExternallyDownloaded();
    // updateDownloadable();
    // }
    //
    // /**
    // * @return true if at least one associated file (through "files" property) is physically available (cf. Storage).
    // * This concerns only RAW_DATA and all QUICKLOOKS
    // */
    // protected boolean containsPhysicalData() {
    // return Multimaps.filterKeys(getFiles(), k -> {
    // switch (k) {
    // case RAWDATA:
    // case QUICKLOOK_SD:
    // case QUICKLOOK_MD:
    // case QUICKLOOK_HD:
    // return true;
    // default:
    // return false;
    // }
    // }).values().stream().filter(DataFile::isPhysicallyAvailable).findAny().isPresent();
    // }
    //
    // /**
    // * @return true if at least one associated file (through "files" property) can be externally downloaded
    // * This concerns only RAW_DATA and all QUICKLOOKS
    // */
    // protected boolean canBeExternallyDownloaded() {
    // return Multimaps.filterKeys(getFiles(), k -> {
    // switch (k) {
    // case RAWDATA:
    // case QUICKLOOK_SD:
    // case QUICKLOOK_MD:
    // case QUICKLOOK_HD:
    // return true;
    // default:
    // return false;
    // }
    // }).values().stream().filter(DataFile::canBeExternallyDownloaded).findAny().isPresent();
    // }
    //
    // /**
    // * Update downloadable property on all files
    // */
    // public void updateDownloadable() {
    // Multimaps.filterKeys(getFiles(), k -> k == DataType.RAWDATA).values().forEach(DataFile::isDownloadable);
    // }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
