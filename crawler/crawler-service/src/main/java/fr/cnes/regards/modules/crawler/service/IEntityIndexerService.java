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

    void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate);

    default void updateEntityIntoEs(String tenant, UniformResourceName ipId, OffsetDateTime updateDate) {
        this.updateEntityIntoEs(tenant, ipId, null, updateDate);
    }

    default void updateEntitiesIntoEs(String tenant, UniformResourceName[] ipIds, OffsetDateTime updateDate) {
        this.updateEntitiesIntoEs(tenant, ipIds, null, updateDate);
    }

    void updateEntitiesIntoEs(String tenant, UniformResourceName[] ipIds, OffsetDateTime lastUpdateDate,
            OffsetDateTime updateDate);

    boolean createIndexIfNeeded(String tenant);

    /**
     * Transactional method updating a set of datasets
     * @param lastUpdateDate Take into account only more recent lastUpdateDate than provided
     */
    void updateDatasets(String tenant, Set<Dataset> datasets, OffsetDateTime lastUpdateDate);

    int createDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects);

    int mergeDataObjects(String tenant, String datasourceId, OffsetDateTime now, List<DataObject> objects);
}
