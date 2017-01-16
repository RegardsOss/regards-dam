package fr.cnes.regards.modules.crawler.service;

import java.util.Map;

import fr.cnes.regards.modules.crawler.domain.IIndexable;

public interface IIndexerService {

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

    boolean saveEntity(String pIndex, IIndexable pEntity);

    Map<String, Throwable> saveBulkEntities(String pIndex, IIndexable... pEntities);

    boolean deleteEntity(String index, IIndexable pEntity);
}
