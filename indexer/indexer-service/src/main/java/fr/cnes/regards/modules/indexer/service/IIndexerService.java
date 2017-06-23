/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.indexer.service;

import java.util.Collection;

import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.IIndexable;

/**
 * Indexer interface
 * @author oroussel
 */
public interface IIndexerService {

    int BULK_SIZE = IEsRepository.BULK_SIZE;

    /**
     * Create index if not already exists
     * @param pIndex index name
     * @return true if index exists after method returns, false overwise
     */
    boolean createIndex(String pIndex);

    /**
     * Delete index if index exists
     * @param pIndex index name
     * @return true if index doesn't exist after method returns
     */
    boolean deleteIndex(String pIndex);

    boolean indexExists(String pIndex);

    boolean saveEntity(String pIndex, IIndexable pEntity);

    /**
     * Method only used for tests. Elasticsearch performs refreshes every second. So, il a search is called just after
     * a save, the document will not be available. A manual refresh is necessary (on saveBulkEntities, it is
     * automaticaly called)
     * @param pIndex index to refresh
     */
    void refresh(String pIndex);

    int saveBulkEntities(String pIndex, IIndexable... pEntities);

    int saveBulkEntities(String pIndex, Collection<? extends IIndexable> pEntities);

    boolean deleteEntity(String pIndex, IIndexable pEntity);
}
