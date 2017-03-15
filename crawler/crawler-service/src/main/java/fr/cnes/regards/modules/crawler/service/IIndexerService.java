package fr.cnes.regards.modules.crawler.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.crawler.dao.FacetPage;
import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.SearchKey;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.crawler.domain.facet.FacetType;
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

    int saveBulkEntities(String pIndex, IIndexable... pEntities);

    int saveBulkEntities(String pIndex, Collection<? extends IIndexable> pEntities);

    boolean deleteEntity(String pIndex, IIndexable pEntity);

    /**
     * Search ordered documents into index following criterion. Some facets are asked for.
     * @param searchKey identity search key
     * @param pageRequest pagination information ({@link PageRequest}
     * @param criterion search criterion
     * @param facetsMap a map of { document property name, facet type }
     * @param ascSortMap a linked map (preserved insertion ordered) of { document property, true for ascending order }
     * @return a simple page of documents if facet are not asked for, a {@link FacetPage} else
     */
    <T> Page<T> search(SearchKey<T> searchKey, Pageable pageRequest, ICriterion criterion,
            Map<String, FacetType> facetsMap, LinkedHashMap<String, Boolean> ascSortMap);

    /**
     * Search documents part (ie a property of documents) into index following criterion (on documents, not on asked
     * property document).
     * @param searchKey the search key. <b>Be careful, the search type must be the type concerned by criterion, result
     * class is class for sourceAttribute</b>
     * @param pageRequest pagination information ({@link PageRequest}
     * @param criterion search criterion on document
     * @param sourceAttribute the property of the document to retrieve (can be an inner chained property :
     * toto.titi.tutu for example)
     * @return a page of asked property documents
     */
    <T> Page<T> search(SearchKey<T> searchKey, Pageable pageRequest, ICriterion criterion, String sourceAttribute);

    default <T> Page<T> search(SearchKey<T> searchKey, int pageSize, ICriterion criterion, String sourceAttribute) {
        return search(searchKey, new PageRequest(0, pageSize), criterion, sourceAttribute);
    }

    default <T> Page<T> search(SearchKey<T> searchKey, int pPageSize, ICriterion criterion) {
        return search(searchKey, new PageRequest(0, pPageSize), criterion);
    }

    default <T> Page<T> search(SearchKey<T> searchKey, Pageable pPageRequest, ICriterion criterion) {
        return search(searchKey, pPageRequest, criterion, null, null);
    }

    default <T> Page<T> search(SearchKey<T> searchKey, Pageable pPageRequest, ICriterion criterion,
            Map<String, FacetType> facetsMap) {
        return search(searchKey, pPageRequest, criterion, facetsMap, null);
    }

    default <T> Page<T> search(SearchKey<T> searchKey, Pageable pPageRequest, ICriterion criterion,
            LinkedHashMap<String, Boolean> ascSortMap) {
        return search(searchKey, pPageRequest, criterion, null, ascSortMap);
    }
}
