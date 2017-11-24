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
package fr.cnes.regards.modules.crawler.domain.dto;

import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;

/**
 * DTO To add Datasource PluginConfiguration label to {@link DatasourceIngestion} entities
 * @author Sébastien Binda
 */
public final class DatasourceIngestionDTOBuilder {

    private DatasourceIngestionDTOBuilder() {
    }

    public static DatasourceIngestionDTO build(DatasourceIngestion datasourceIngestion,
            String datasourcePluginConfLabel) {
        DatasourceIngestionDTO result = new DatasourceIngestionDTO();
        result.setId(datasourceIngestion.getId());
        result.setLastIngestDate(datasourceIngestion.getLastIngestDate());
        result.setNextPlannedIngestDate(datasourceIngestion.getNextPlannedIngestDate());
        result.setSavedObjectsCount(datasourceIngestion.getSavedObjectsCount());
        result.setStackTrace(datasourceIngestion.getStackTrace());
        result.setStatus(datasourceIngestion.getStatus());
        result.setStatusDate(datasourceIngestion.getStatusDate());
        result.setDatasourceConfigurationLabel(datasourcePluginConfLabel);
        return result;
    }

}
