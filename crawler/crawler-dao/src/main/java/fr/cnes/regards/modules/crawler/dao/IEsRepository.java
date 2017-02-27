package fr.cnes.regards.modules.crawler.dao;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.elasticsearch.search.SearchHit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.crawler.domain.facet.FacetType;

/**
 * Elasticsearch DAO interface
 */
public interface IEsRepository {

    /**
     * Create specified index
     * @param pIndex index
     * @return true if acknowledged by Elasticsearch, false otherwise.
     * returns
     */
    boolean createIndex(String pIndex);

    /**
     * Delete specified index
     * @param pIndex index
     * @return true if acknowledged by Elasticsearch, false otherwise.
     */
    boolean deleteIndex(String pIndex);

    /**
     * Find all indices
     * @return all indices <b>lowercase</b>
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
     * @return true if created, false otherwise
     */
    boolean save(String pIndex, IIndexable pDocument);

    /**
     * Method only used for tests. Elasticsearch performs refreshes every second. So, il a search is called just after
     * a save, the document will not be available. A manual refresh is necessary (on saveBulkEntities, it is
     * automaticaly called)
     * @param pIndex index to refresh
     */
    void refresh(String pIndex);

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
        return (T) get(pIndex, pDocument.getType(), pDocument.getDocId(), pDocument.getClass());
    }

    /**
     * Delete specified document
     * @param pIndex index
     * @param pType document type
     * @param pId document id
     * @return true if document no more exists, false otherwise
     */
    boolean delete(String pIndex, String pType, String pId);

    /**
     * Same as {@link #delete(String, String, String)} using docId and type of provided document
     * @param pIndex index
     * @param pDocument IIndexable object specifying docId and type
     * @return true if document no more exists, false otherwise
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
     * @return true if document has been updated, false otherwise
     */
    boolean merge(String pIndex, String pType, String pId, Map<String, Object> pMergedPropertiesMap);

    /**
     * {@link #merge(String, String, String, Map)}
     * @param pIndex index
     * @param pDocument IIndexable object specifying docId and type
     * @param pMergedPropertiesMap map { name -> value } of properties to merge. Name can be one level sub-property dot
     * identifier (ie. "toto.tata")
     * @return true if document has been updated, false otherwise
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
     * Searching first page of elements from index giving page size with facets.
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param pFacetsMap map of (attribute name - facet type). Can be null if no facet asked for.
     * @param pAscSortMap map of (attributes name - true if ascending). Can be null if no sort asked for.
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize, ICriterion pCriterion,
            Map<String, FacetType> pFacetsMap, LinkedHashMap<String, Boolean> pAscSortMap);

    /**
     * Searching specified page of elements from index (for first call use
     * {@link #searchAllLimited(String, Class, int)} method) with facets.
     * <b>This method fails if asked for offset greater than 10000 (Elasticsearch limitation)</b>
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pCriterion search criterion
     * @param pFacetsMap map of (attribute name - facet type). Can be null if no facet asked for.
     * @param pAscSortMap map of (attributes name - true if ascending). Can be null if no sort asked for.
     * @param <T> class of document type
     * @return specified result page
     */
    <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest, ICriterion pCriterion,
            Map<String, FacetType> pFacetsMap, LinkedHashMap<String, Boolean> pAscSortMap);

    /**
     * Searching first page of elements from index giving page size without facets.
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize,
            LinkedHashMap<String, Boolean> pAscSortMap, ICriterion pCriterion) {
        return this.search(pIndex, pClass, pPageSize, pCriterion, null, pAscSortMap);
    }

    /**
     * Searching specified page of elements from index (for first call use
     * {@link #searchAllLimited(String, Class, int)} method) without facets.
     * <b>This method fails if asked for offset greater than 10000 (Elasticsearch limitation)</b>
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pCriterion search criterion
     * @param <T> class of document type
     * @return specified result page
     */
    default <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest,
            LinkedHashMap<String, Boolean> pAscSortMap, ICriterion pCriterion) {
        return this.search(pIndex, pClass, pPageRequest, pCriterion, null, pAscSortMap);
    }

    /**
     * Searching first page of elements from index giving page size without sort.
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize, ICriterion pCriterion,
            Map<String, FacetType> pFacetsMap) {
        return this.search(pIndex, pClass, pPageSize, pCriterion, pFacetsMap, null);
    }

    /**
     * Searching specified page of elements from index (for first call use
     * {@link #searchAllLimited(String, Class, int)} method) without sort.
     * <b>This method fails if asked for offset greater than 10000 (Elasticsearch limitation)</b>
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pCriterion search criterion
     * @param <T> class of document type
     * @return specified result page
     */
    default <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest, ICriterion pCriterion,
            Map<String, FacetType> pFacetsMap) {
        return this.search(pIndex, pClass, pPageRequest, pCriterion, pFacetsMap, null);
    }

    /**
     * Searching first page of elements from index giving page size without facets nor sort
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T> Page<T> search(String pIndex, Class<T> pClass, int pPageSize, ICriterion pCriterion) {
        return this.search(pIndex, pClass, pPageSize, pCriterion, null, null);
    }

    /**
     * Searching specified page of elements from index (for first call use
     * {@link #searchAllLimited(String, Class, int)} method) without facets nor sort.
     * <b>This method fails if asked for offset greater than 10000 (Elasticsearch limitation)</b>
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pCriterion search criterion
     * @param <T> class of document type
     * @return specified result page
     */
    default <T> Page<T> search(String pIndex, Class<T> pClass, Pageable pPageRequest, ICriterion pCriterion) {
        return this.search(pIndex, pClass, pPageRequest, pCriterion, null, null);
    }

    /**
     * Searching first page of elements from index giving page size
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageSize page size
     * @param pValue value to search
     * @param pFields fields to search on (use '.' for inner objects, ie "attributes.tags").
     * <b>Fields types must be consistent with given value type</b>
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    <T> Page<T> multiFieldsSearch(String pIndex, Class<T> pClass, int pPageSize, Object pValue, String... pFields);

    /**
     * Searching specified page of elements from index giving page size (for first call us
     * {@link #multiFieldsSearch(String, Class, int, Object, String...)} method
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pValue value to search
     * @param pFields fields to search on (use '.' for inner objects, ie "attributes.tags"). Wildcards '*' can be
     * used too (ie attributes.dataRange.*). <b>Fields types must be consistent with given value type</b>
     * @param <T> document type
     * @return specified result page
     */
    <T> Page<T> multiFieldsSearch(String pIndex, Class<T> pClass, Pageable pPageRequest, Object pValue,
            String... pFields);

    /**
     * Execute specified action for all search results<br/>
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
