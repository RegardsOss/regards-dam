package fr.cnes.regards.modules.crawler.dao;

import java.util.Map;
import java.util.function.Consumer;

import org.elasticsearch.search.SearchHit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Elasticsearch DAO interface
 */
public interface IEsRepository {

    /**
     * Create specified index
     *
     * @param pIndex index
     * @return true if acknowledged by Elasticsearch
     */
    boolean createIndex(String pIndex);

    /**
     * Delete specified index
     * @param pIndex index
     * @return true if acknowledged by Elasticsearch
     */
    boolean deleteIndex(String pIndex);

    /**
     * Find all indices
     *
     * @return all indices
     */
    String[] findIndices();

    /**
     * Does specified index exist ?
     * @param pName index name
     * @return true or false
     */
    boolean indexExists(String pName);

    /**
     * Create or update a document index specifying index and type. index, type and id must be provided.
     * @param pIndex index
     * @param pType document type
     * @param pId document id
     * @param pDocument document
     * @return true if created, false overwise
     */
    boolean save(String pIndex, String pType, String pId, Object pDocument);

    /**
     * Create or update several documents into same index. Index, type and id (throw map) must be provided.
     * TODO : ATTENTION : pour l'instant mono-type mais il faudra prendre en compte le type de chaque document
     * @param pIndex index
     * @param pType documents type
     * @param pDocumentMap document map { document id -> document }
     * @return null if no error, a map { id -> Throwable } for all documents when save has failed
     */
    Map<String, Throwable> saveBulk(String pIndex, String pType, Map<String, ?> pDocumentMap);

    /**
     * Not necessary but....
     * @param pIndex index
     * @param pType document type
     * @param pId document id
     * @param pClass class of document type
     * @param <T> document type
     * @return found document or null
     */
    <T> T get(String pIndex, String pType, String pId, Class<T> pClass);

    /**
     * Delete specified document
     * @param pIndex index
     * @param pType document type
     * @param pId document id
     * @return true if document has been deleted, false overwise
     */
    boolean delete(String pIndex, String pType, String pId);

    /**
     * Merge partial document with existing one.
     * @param pIndex index
     * @param pType document type
     * @param pId document id
     * @param pMergedPropertiesMap map { name -> value } of properties to merge. Name can be one level sub-property dot
     * identifier (ie. "toto.tata")
     * @return true if document has been updated, false overwise
     */
    boolean merge(String pIndex, String pType, String pId, Map<String, Object> pMergedPropertiesMap);

    /**
     * Searching first page of all elements from index giving page size.
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageSize page size
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    <T> Page<T> searchAllLimited(String pIndex, Class<T> pClass, int pPageSize);

    /**
     * Searching specified page of all elements from index (for first call use
     * {@link #searchAllLimited(String, Class, int)} method)
     * <b>This method fails if asked for offset greater than 10000 (Elasticsearch limitation)</b>
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param <T> class of document type
     * @return specified result page
     */
    <T> Page<T> searchAllLimited(String pIndex, Class<T> pClass, Pageable pPageRequest);

    /**
     * Execute specified action or all search results
     * <b>No 10000 offset Elasticsearch limitation</b>
     * @param pIndex index
     * @param pAction action to be executed for each search result element
     */
    void searchAll(String pIndex, Consumer<SearchHit> pAction);

    /**
     * Close Client
     */
    void close();
}
