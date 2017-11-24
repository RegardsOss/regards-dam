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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionStatus;

/**
 * DTO To add Datasource PluginConfiguration label to {@link DatasourceIngestion} entities
 * @author Sébastien Binda
 */
public class DatasourceIngestionDTO {

    private Long id;

    private String datasourceConfigurationLabel;

    private OffsetDateTime lastIngestDate;

    private OffsetDateTime nextPlannedIngestDate;

    private String duration = null;

    private IngestionStatus status = IngestionStatus.NEW;

    private OffsetDateTime statusDate = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);

    private Integer savedObjectsCount;

    private String stackTrace;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDatasourceConfigurationLabel() {
        return datasourceConfigurationLabel;
    }

    public void setDatasourceConfigurationLabel(String datasourceConfigurationLabel) {
        this.datasourceConfigurationLabel = datasourceConfigurationLabel;
    }

    public OffsetDateTime getLastIngestDate() {
        return lastIngestDate;
    }

    public void setLastIngestDate(OffsetDateTime lastIngestDate) {
        this.lastIngestDate = lastIngestDate;
    }

    public OffsetDateTime getNextPlannedIngestDate() {
        return nextPlannedIngestDate;
    }

    public void setNextPlannedIngestDate(OffsetDateTime nextPlannedIngestDate) {
        this.nextPlannedIngestDate = nextPlannedIngestDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public IngestionStatus getStatus() {
        return status;
    }

    public void setStatus(IngestionStatus status) {
        this.status = status;
    }

    public OffsetDateTime getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(OffsetDateTime statusDate) {
        this.statusDate = statusDate;
    }

    public Integer getSavedObjectsCount() {
        return savedObjectsCount;
    }

    public void setSavedObjectsCount(Integer savedObjectsCount) {
        this.savedObjectsCount = savedObjectsCount;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

}
