package fr.cnes.regards.modules.crawler.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;

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

    Map<String, Throwable> saveBulkEntities(String pIndex, Collection<? extends IIndexable> pEntities);

    boolean deleteEntity(String pIndex, IIndexable pEntity);

    <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize, ICriterion criterion);

    <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest, ICriterion criterion);

}
