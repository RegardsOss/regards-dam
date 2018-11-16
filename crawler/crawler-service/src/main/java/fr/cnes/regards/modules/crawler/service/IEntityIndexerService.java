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
package fr.cnes.regards.modules.crawler.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.indexer.dao.BulkSaveResult;

/**
 * Entity domain indexer service interface. This is on top of indexerService to manage domain specific objects.
 * @author oroussel
 */
public interface IEntityIndexerService {

    void handleApplicationReady(ApplicationReadyEvent event);

    /**
     * Update entity into Elasticsearch
     * @param tenant concerned tenant
     * @param ipId concerned entity id
     * @param updateDate current update date (usually now)
     * @param forceAssociatedEntitiesUpdate if true, force associated entities update (usually data objects for dataset)
     */
    default void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime updateDate,
            boolean forceAssociatedEntitiesUpdate) throws ModuleException {
        this.updateEntityIntoEs(tenant, ipId, null, updateDate, forceAssociatedEntitiesUpdate, null);
    }

    /**
     * Manage computed attributes computation
     * @param dataset concerned dataset
     * @param dsiId can be null (in this case, no notification is sent)
     */
    void computeComputedAttributes(Dataset dataset, Long dsiId, String tenant);

    /**
     * Update entity into Elasticsearch
     * @param tenant concerned tenant
     * @param ipId concerned entity id
     * @param lastUpdateDate last ingestion update date
     * @param updateDate current update date (usually now)
     * @param forceAssociatedEntitiesUpdate if true, force associated entities update (usually data objects for dataset)
     */
    void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate, boolean forceAssociatedEntitiesUpdate, Long dsiId) throws ModuleException;

    /**
     * Create index it doesn't exist
     * @param tenant concerned tenant
     * @return true if a creation has been done
     */
    boolean createIndexIfNeeded(String tenant);

    /**
     * Transactional method updating a set of datasets
     * @param lastUpdateDate Take into account only more recent lastUpdateDate than provided
     * @param forceDataObjectsUpdate true to force all associated data objects update
     * @param dsiId datasetIngestion id
     */
    void updateDatasets(String tenant, Set<Dataset> datasets, OffsetDateTime lastUpdateDate,
            boolean forceDataObjectsUpdate, Long dsiId) throws ModuleException;

    /**
     * Create given data objects into Elasticsearch
     * @param tenant concerned tenant
     * @param datasourceId id of data source from where data objects come
     * @param now update date (usually now)
     * @param objects objects to save
     * @return bulk save result
     */
    BulkSaveResult createDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects);

    /**
     * Merge given data objects into Elasticsearch
     * @param tenant concerned tenant
     * @param datasourceId id of data source from where data objects come
     * @param now update date (usually now)
     * @param objects objects to save
     * @return bulk save result
     */
    BulkSaveResult mergeDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects);

    /**
     * Delete given data object from Elasticsearch
     * @param tenant concerned tenant
     * @param ipId id of Data object
     * @return wether or not the data object has been deleted
     */
    boolean deleteDataObject(String tenant, String ipId);

    /**
     * Create a notification for admin
     * @param tenant concerned tenant
     * @param title notification title
     * @param buf Buffer containing message
     */
    void createNotificationForAdmin(String tenant, String title, CharSequence buf);
}
