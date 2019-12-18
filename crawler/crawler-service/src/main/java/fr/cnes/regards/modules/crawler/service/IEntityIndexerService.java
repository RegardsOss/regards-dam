/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import java.util.Collection;
import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.notification.NotificationLevel;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.indexer.dao.BulkSaveResult;

/**
 * Entity domain indexer service interface. This is on top of indexerService to manage domain specific objects.
 *
 * @author oroussel
 */
public interface IEntityIndexerService {

    /**
     * Update entity into Elasticsearch
     *
     * @param tenant                        concerned tenant
     * @param ipId                          concerned entity id
     * @param updateDate                    current update date (usually now)
     * @param forceAssociatedEntitiesUpdate if true, force associated entities update (usually data objects for dataset)
     * @throws ModuleException
     */
    default void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime updateDate,
            boolean forceAssociatedEntitiesUpdate) throws ModuleException {
        this.updateEntityIntoEs(tenant, ipId, null, updateDate, forceAssociatedEntitiesUpdate, null);
    }

    /**
     * Manage computed attributes computation
     *
     * @param dataset concerned dataset
     * @param dsiId   {@link DatasourceIngestion} id. can be null (in this case, no notification is sent)
     * @param tenant
     */
    void computeComputedAttributes(Dataset dataset, String dsiId, String tenant);

    /**
     * Update entity into Elasticsearch
     *
     * @param tenant                        concerned tenant
     * @param ipId                          concerned entity id
     * @param lastUpdateDate                last ingestion update date
     * @param updateDate                    current update date (usually now)
     * @param forceAssociatedEntitiesUpdate if true, force associated entities update (usually data objects for dataset)
     * @param dsiId                         {@link DatasourceIngestion} id
     * @throws ModuleException
     */
    void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate, boolean forceAssociatedEntitiesUpdate, String dsiId) throws ModuleException;

    /**
     * Create index it doesn't exist
     *
     * @param tenant concerned tenant
     * @return true if a creation has been done
     */
    boolean createIndexIfNeeded(String tenant);

    /**
     * CreDeleteate index if exist
     *
     * @param tenant concerned tenant
     * @return true if a deletion has been done
     */
    boolean deleteIndex(String tenant);

    /**
     * Transactional method updating a set of datasets
     *
     * @param tenant
     * @param datasets
     * @param lastUpdateDate         Take into account only more recent lastUpdateDate than provided
     * @param updateDate
     *@param forceDataObjectsUpdate true to force all associated data objects update
     * @param dsiId                  datasetIngestion id   @throws ModuleException
     */
    void updateDatasets(String tenant, Collection<Dataset> datasets, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate, boolean forceDataObjectsUpdate, String dsiId) throws ModuleException;

    /**
     * Force update of all {@link Dataset}s
     *
     * @param tenant
     * @param updateDate
     * @throws ModuleException
     */
    void updateAllDatasets(String tenant, OffsetDateTime updateDate) throws ModuleException;

    /**
     * Force update of all {@link fr.cnes.regards.modules.dam.domain.entities.Collection}s
     *
     * @param tenant
     * @param updateDate
     * @throws ModuleException
     */
    void updateAllCollections(String tenant, OffsetDateTime updateDate) throws ModuleException;

    /**
     * Create given data objects into Elasticsearch
     *
     * @param tenant       concerned tenant
     * @param datasourceId id of data source from where data objects come
     * @param now          update date (usually now)
     * @param objects      objects to save
     * @param datasourceIngestionId
     * @return bulk save result
     * @throws ModuleException
     */
    BulkSaveResult createDataObjects(String tenant, Long datasourceId, OffsetDateTime now, List<DataObject> objects,
            String datasourceIngestionId)
            throws ModuleException;

    /**
     * Merge given data objects into Elasticsearch
     *
     * @param tenant       concerned tenant
     * @param datasourceId id of data source from where data objects come
     * @param now          update date (usually now)
     * @param objects      objects to save
     * @param datasourceIngestionId
     * @return bulk save result
     * @throws ModuleException
     */
    BulkSaveResult mergeDataObjects(String tenant, Long datasourceId, OffsetDateTime now, List<DataObject> objects,
            String datasourceIngestionId)
            throws ModuleException;

    /**
     * Delete given data object from Elasticsearch
     *
     * @param tenant concerned tenant
     * @param ipId   id of Data object
     * @return wether or not the data object has been deleted
     */
    boolean deleteDataObject(String tenant, String ipId);

    /**
     * Create a notification for admin
     *
     * @param tenant
     * @param title   notification title
     * @param message
     * @param level   {@link NotificationLevel}
     */
    void createNotificationForAdmin(String tenant, String title, String message, NotificationLevel level);

    /**
     * Delete index and recreate entities
     * @param tenant
     */
    void deleteIndexNRecreateEntities(String tenant) throws ModuleException;
}
