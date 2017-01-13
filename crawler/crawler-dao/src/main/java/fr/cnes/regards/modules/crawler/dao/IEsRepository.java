package fr.cnes.regards.modules.crawler.dao;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import org.elasticsearch.search.SearchHit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.crawler.domain.IIndexable;

/**
 * Elasticsearch DAO interface
 */
public interface IEsRepository {

    /**
     * Create specified index
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
     * Create or update a document index specifying index.
     * @param pIndex index
     * @param pDocument object implementing IIndexable thus needs to provide id and type
     * @return true if created, false overwise
     */
    boolean save(String pIndex, IIndexable pDocument);

    /**
     * Create or update several documents into same index.
     * @param pIndex index
     * @param pDocuments documents to save (docId and type are mandatory for all of them)
     * @param <T> parameterized type to avoid array inheritance restriction type definition
     * @return null if no error, a map { document id -> Throwable } for all documents for which save has failed
     * @exception IllegalArgumentException If at least one document hasn't its two mandatory properties (docId and
     * type).
     */
    <T extends IIndexable> Map<String, Throwable> saveBulk(String pIndex,
            @SuppressWarnings("unchecked") T... pDocuments) throws IllegalArgumentException;

    /**
     * {@link #saveBulk(String, IIndexable...)}
     * @param pIndex index
     * @param pDocuments documents to save (docId and type are mandatory for all of them)
     * @return null if no error, a map { document id -> Throwable } for all documents for which save has failed
     * @exception IllegalArgumentException If at least one document hasn't its two mandatory properties (docId and
     * type).
     */
    default Map<String, Throwable> saveBulk(String pIndex, Collection<? extends IIndexable> pDocuments)
            throws IllegalArgumentException {
        return this.saveBulk(pIndex, pDocuments.toArray(new IIndexable[pDocuments.size()]));
    }

    /**
     * Not necessary but....
     * @param pIndex index
     * @param pDocType document type
     * @param pDocId document id
     * @param pClass class of document type
     * @param <T> document type
     * @return found document or null
     */
    <T> T get(String pIndex, String pDocType, String pDocId, Class<T> pClass);

    /**
     * Utility method to avoid using Class<T> and passing directly id and type
     * @param pIndex index
     * @param pDocument IIndexable object specifying docId and type
     * @param <T> document type
     * @return found document of same type as pDocument or null
     */
    @SuppressWarnings("unchecked")
    default <T extends IIndexable> T get(String pIndex, T pDocument) {
        return (T) get(pIndex, pDocument.getDocId(), pDocument.getType(), pDocument.getClass());
    }

    /**
     * Delete specified document
     * @param pIndex index
     * @param pType document type
     * @param pId document id
     * @return true if document has been deleted, false overwise
     */
    boolean delete(String pIndex, String pType, String pId);

    /**
     * Same as {@link #delete(String, String, String)} using docId and type of provided document
     * @param pIndex index
     * @param pDocument IIndexable object specifying docId and type
     * @return true if document has been deleted, false overwise
     */
    default boolean delete(String pIndex, IIndexable pDocument) {
        return delete(pIndex, pDocument.getType(), pDocument.getDocId());
    }

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
     * {@link #merge(String, String, String, Map)}
     * @param pIndex index
     * @param pDocument IIndexable object specifying docId and type
     * @param pMergedPropertiesMap map { name -> value } of properties to merge. Name can be one level sub-property dot
     * identifier (ie. "toto.tata")
     * @return true if document has been updated, false overwise
     */
    default boolean merge(String pIndex, IIndexable pDocument, Map<String, Object> pMergedPropertiesMap) {
        return merge(pIndex, pDocument.getType(), pDocument.getDocId(), pMergedPropertiesMap);
    }

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