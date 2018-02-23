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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.domain.metadata.DataObjectMetadata;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * A DataObject is created by a DataSource when a data source (external database or AIPs by example) is ingested.
 *
 * @author lmieulet
 * @author Marc Sordi
 * @author oroussel
 */
public class DataObject extends AbstractDataEntity {

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
     * This field only exists for Gson serialization (used by frontent)
     */
    private Boolean containsPhysicalData = null;

    public DataObject(Model model, String tenant, String label) {
        super(model, new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, tenant,
                                             UUID.fromString("0-0-0-0-" + (int)(Math.random() * Integer.MAX_VALUE)),
                                                             1),
              label);
    }

    public DataObject() {
        this(null, null, null);
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

    @Override
    public boolean containsPhysicalData() {
        containsPhysicalData = super.containsPhysicalData();
        return containsPhysicalData;
    }

    @Override
    public String getType() {
        return EntityType.DATA.toString();
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
