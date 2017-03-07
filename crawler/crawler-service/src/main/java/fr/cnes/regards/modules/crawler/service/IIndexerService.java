package fr.cnes.regards.modules.crawler.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

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

    <T extends IIndexable> T get(UniformResourceName urn);

    boolean saveEntity(String pIndex, IIndexable pEntity);

    /**
     * Method only used for tests. Elasticsearch performs refreshes every second. So, il a search is called just after
     * a save, the document will not be available. A manual refresh is necessary (on saveBulkEntities, it is
     * automaticaly called)
     * @param pIndex index to refresh
     */
    void refresh(String pIndex);

    Map<String, Throwable> saveBulkEntities(String pIndex, IIndexable... pEntities);

    Map<String, Throwable> saveBulkEntities(String pIndex, Collection<? extends IIndexable> pEntities);

    boolean deleteEntity(String pIndex, IIndexable pEntity);

    <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize, ICriterion criterion);

    <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest, ICriterion criterion);
}
