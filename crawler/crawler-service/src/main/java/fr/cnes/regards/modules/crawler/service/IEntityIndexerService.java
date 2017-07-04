package fr.cnes.regards.modules.crawler.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Entity domain indexer service interface. This is on top of indexerService to manage domain specific objects.
 * @author oroussel
 */
public interface IEntityIndexerService {

    /**
     * Update entity into Elasticsearch
     * @param tenant concerned tenant
     * @param ipId concerned entity id
     * @param updateDate current update date (usually now)
     */
    default void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime updateDate) {
        this.updateEntityIntoEs(tenant, ipId, null, updateDate);
    }

    /**
     * Update entity into Elasticsearch
     * @param tenant concerned tenant
     * @param ipId concerned entity id
     * @param lastUpdateDate last ingestion update date
     * @param updateDate current update date (usually now)
     */
    void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate);

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
     */
    void updateDatasets(String tenant, Set<Dataset> datasets, OffsetDateTime lastUpdateDate,
            boolean forceDataObjectsUpdate);

    /**
     * Create given data objects into Elasticsearch
     * @param tenant concerned tenant
     * @param datasourceId id of data source from where data objects come
     * @param now update date (usually now)
     * @param objects objects to save
     * @return number of objects effectively created
     */
    int createDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects);

    /**
     * Merge given data objects into Elasticsearch
     * @param tenant concerned tenant
     * @param datasourceId id of data source from where data objects come
     * @param now update date (usually now)
     * @param objects objects to save
     * @return number of objects effectively saved
     */
    int mergeDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects);
}