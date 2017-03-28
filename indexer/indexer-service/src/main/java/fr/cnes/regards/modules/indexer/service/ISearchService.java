package fr.cnes.regards.modules.indexer.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.dao.FacetPage;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.SearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;

/**
 * Elasticsearch search service.
 * This service contains all search and get methods.
 * For other methods, check crawler-service and IndexerService class.
 *
 * @author oroussel
 */
public interface ISearchService {

    <T extends IIndexable> T get(UniformResourceName urn);

    /**
     * Search ordered documents into index following criterion. Some facets are asked for.
     * @param searchKey identity search key
     * @param pageRequest pagination information ({@link PageRequest}
     * @param criterion search criterion
     * @param facetsMap a map of { document property name, facet type }
     * @param ascSortMap a linked map (preserved insertion ordered) of { document property, true for ascending order }
     * @return a simple page of documents if facet are not asked for, a {@link FacetPage} else
     */
    <T> Page<T> search(SearchKey<T, T> searchKey, Pageable pageRequest, ICriterion criterion,
            Map<String, FacetType> facetsMap, LinkedHashMap<String, Boolean> ascSortMap);

    /**
     * Search documents as usual ({@link #search(SearchKey, int, ICriterion)} BUT return joined entity whom type is
     * specified into searchKey
     * @param searchKey the search key. <b>Be careful, the search type must be the type concerned by criterion, result
     * class must be joined entity class </b>
     * @param pageRequest pagination information ({@link PageRequest}
     * @param criterion search criterion on document
     * @param <T> Joined entity class
     * @return a page of joined entities
     */
    <S, R extends IIndexable> Page<R> searchAndReturnJoinedEntities(SearchKey<S, R> searchKey, Pageable pageRequest,
            ICriterion pCriterion);

    /**
     * Searching specified page of elements from index giving page size (for first call us
     * {@link #multiFieldsSearch(String, Class, int, Object, String...)} method
     * @param searchKey the search key
     * @param pClass class of document type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pValue value to search
     * @param pFields fields to search on (use '.' for inner objects, ie "attributes.tags"). Wildcards '*' can be
     * used too (ie attributes.dataRange.*). <b>Fields types must be consistent with given value type</b>
     * @param <T> document type
     * @return specified result page
     */
    <T> Page<T> multiFieldsSearch(SearchKey<T, T> searchKey, Pageable pPageRequest, Object pValue, String... pFields);

    default <T> Page<T> multiFieldsSearch(SearchKey<T, T> searchKey, int pageSize, Object pValue, String... pFields) {
        return multiFieldsSearch(searchKey, new PageRequest(0, pageSize), pValue, pFields);
    }

    default <S, R extends IIndexable> Page<R> searchAndReturnJoinedEntities(SearchKey<S, R> searchKey, int pageSize,
            ICriterion pCriterion) {
        return this.searchAndReturnJoinedEntities(searchKey, new PageRequest(0, pageSize), pCriterion);
    }

    default <T> Page<T> search(SearchKey<T, T> searchKey, int pPageSize, ICriterion criterion) {
        return search(searchKey, new PageRequest(0, pPageSize), criterion);
    }

    default <T> Page<T> search(SearchKey<T, T> searchKey, Pageable pPageRequest, ICriterion criterion) {
        return search(searchKey, pPageRequest, criterion, null, null);
    }

    default <T> Page<T> search(SearchKey<T, T> searchKey, Pageable pPageRequest, ICriterion criterion,
            Map<String, FacetType> facetsMap) {
        return search(searchKey, pPageRequest, criterion, facetsMap, null);
    }

    default <T> Page<T> search(SearchKey<T, T> searchKey, Pageable pPageRequest, ICriterion criterion,
            LinkedHashMap<String, Boolean> ascSortMap) {
        return search(searchKey, pPageRequest, criterion, null, ascSortMap);
    }
}
