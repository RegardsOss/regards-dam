package fr.cnes.regards.modules.crawler.dao;

import java.util.Map;
import java.util.function.Consumer;

import org.elasticsearch.search.SearchHit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IEsRepository {

    /**
     * Create specified index
     *
     * @return true if acknowledged by Elasticsearch
     */
    boolean createIndex(String pIndex);

    /**
     * Delete specified index
     *
     * @return true if acknowledged by Elasticsearch
     */
    boolean deleteIndex(String pIndex);

    /**
     * Find all indices
     */
    String[] findIndices();

    /**
     * Does specified index exist ?
     */
    boolean indexExists(String pName);

    /**
     * Create or update a document index specifying index and type. index, type and id must be provided.
     *
     * @return true if created, false overwise
     */
    boolean save(String index, String type, String id, Object document);

    /**
     * Create or update several documents into same index. Index, type and id (throw map) must be provided. TODO :
     * ATTENTION : pour l'instant mono-type mais il faudra prendre en compte le type de chaque document
     */
    Map<String, Throwable> saveBulk(String index, String type, Map<String, ?> documentMap);

    /**
     * Not necessary but....
     */
    <T> T get(String index, String type, String id, Class<T> clazz);

    boolean delete(String index, String type, String id);

    /**
     * Merge partial document with existing one.
     *
     */
    boolean merge(String index, String type, String id, Map<String, Object> mergedPropertiesMap);

    /**
     * Searching first page of all elements from index giving page size
     */
    <T> Page<T> searchAllLimited(String index, Class<T> clazz, int pageSize);

    /**
     * Searching specified page of all elements from index <b>Beware:</b> limited to 10000 elements
     */
    <T> Page<T> searchAllLimited(String index, Class<T> clazz, Pageable pageRequest);

    /**
     * Execute specified action or all search results
     */
    void searchAll(String index, Consumer<SearchHit> action);

    /**
     * Close Client
     */
    void close();
}
