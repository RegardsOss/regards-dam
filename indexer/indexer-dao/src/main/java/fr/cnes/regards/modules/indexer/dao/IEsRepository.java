package fr.cnes.regards.modules.indexer.dao;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.indexer.dao.converter.LinkedHashMapToSort;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.SearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;

/**
 * Elasticsearch DAO interface
 *
 * @author oroussel
 */
public interface IEsRepository {

    int BULK_SIZE = 10_000;

    /**
     * Create specified index
     *
     * @param pIndex index
     * @return true if acknowledged by Elasticsearch, false otherwise. returns
     */
    boolean createIndex(String pIndex);

    /**
     * Put dynamic mapping on specified types of specified index for floating point values (force "double" mapping
     * type instead of float)
     * @param index index
     * @param types all types needing automatic double mapping
     * @return true if acknowledged by Elasticsearch, false otherwise
     */
    boolean setAutomaticDoubleMapping(String index, String... types);

    /**
     * Put geometry mapping on specified types of specified index (ie a "geo_shape" type "geometry" property)
     *
     * @param pIndex index
     * @param types all types with geometry mapping
     * @return true if acknowledged by Elasticsearch, false otherwise.
     */
    boolean setGeometryMapping(String pIndex, String... types);

    /**
     * Delete specified index
     *
     * @param pIndex index
     * @return true if acknowledged by Elasticsearch, false otherwise.
     */
    boolean deleteIndex(String pIndex);

    /**
     * Find all indices
     *
     * @return all indices <b>lowercase</b>
     */
    String[] findIndices();

    /**
     * Does specified index exist ?
     *
     * @param pName index name
     * @return true or false
     */
    boolean indexExists(String pName);

    /**
     * Create or update a document index specifying index.
     *
     * @param pIndex index
     * @param pDocument object implementing IIndexable thus needs to provide id and type
     * @return true if created, false otherwise
     */
    boolean save(String pIndex, IIndexable pDocument);

    /**
     * Method only used for tests. Elasticsearch performs refreshes every second. So, il a search is called just after a save, the document will not be available. A manual refresh is necessary (on
     * saveBulkEntities, it is automaticaly called)
     *
     * @param pIndex index to refresh
     */
    void refresh(String pIndex);

    /**
     * Create or update several documents into same index. Errors are logged.
     *
     * @param pIndex index
     * @param pDocuments documents to save (docId and type are mandatory for all of them)
     * @param <T> parameterized type to avoid array inheritance restriction type definition
     * @return the number of effectively saved documents
     * @exception IllegalArgumentException If at least one document hasn't its two mandatory properties (docId and type).
     */
    @SuppressWarnings("unchecked")
    <T extends IIndexable> int saveBulk(String pIndex, T... pDocuments) throws IllegalArgumentException;

    /**
     * {@link #saveBulk(String, IIndexable...)}
     *
     * @param pIndex index
     * @param pDocuments documents to save (docId and type are mandatory for all of them)
     * @return the number of effectively saved documents
     * @exception IllegalArgumentException If at least one document hasn't its two mandatory properties (docId and type).
     */
    default int saveBulk(final String pIndex, final Collection<? extends IIndexable> pDocuments)
            throws IllegalArgumentException {
        return this.saveBulk(pIndex, pDocuments.toArray(new IIndexable[pDocuments.size()]));
    }

    /**
     * Retrieve a Document from its id
     *
     * @param pIndex index
     * @param pDocType document type
     * @param pDocId document id
     * @param pClass class of document type
     * @param <T> document type
     * @return found document or null
     */
    <T extends IIndexable> T get(String pIndex, String pDocType, String pDocId, Class<T> pClass);

    /**
     * Utility method to avoid using Class<T> and passing directly id and type
     *
     * @param pIndex index
     * @param pDocument IIndexable object specifying docId and type
     * @param <T> document type
     * @return found document of same type as pDocument or null
     */
    @SuppressWarnings("unchecked")
    default <T extends IIndexable> T get(final String pIndex, final T pDocument) {
        return (T) get(pIndex, pDocument.getType(), pDocument.getDocId(), pDocument.getClass());
    }

    /**
     * Delete specified document
     *
     * @param pIndex index
     * @param pType document type
     * @param pId document id
     * @return true if document no more exists, false otherwise
     */
    boolean delete(String pIndex, String pType, String pId);

    /**
     * Delete all documents from index
     * @param index index
     * @return number of documents deleted
     */
    long deleteAll(String index);

    /**
     * Delete all documents from index following criterion
     * @param index index
     * @param criterion criterion
     * @return number of deleted elements
     */
    long deleteByQuery(String index, ICriterion criterion);

    /**
     * Same as {@link #delete(String, String, String)} using docId and type of provided document
     *
     * @param pIndex index
     * @param pDocument IIndexable object specifying docId and type
     * @return true if document no more exists, false otherwise
     */
    default boolean delete(final String pIndex, final IIndexable pDocument) {
        return delete(pIndex, pDocument.getType(), pDocument.getDocId());
    }

    /**
     * Merge partial document with existing one.
     *
     * @param pIndex index
     * @param pType document type
     * @param pId document id
     * @param pMergedPropertiesMap map { name -> value } of properties to merge. Name can be one level sub-property dot identifier (ie. "toto.tata")
     * @return true if document has been updated, false otherwise
     */
    boolean merge(String pIndex, String pType, String pId, Map<String, Object> pMergedPropertiesMap);

    /**
     * {@link #merge(String, String, String, Map)}
     *
     * @param pIndex index
     * @param pDocument IIndexable object specifying docId and type
     * @param pMergedPropertiesMap map { name -> value } of properties to merge. Name can be one level sub-property dot identifier (ie. "toto.tata")
     * @return true if document has been updated, false otherwise
     */
    default boolean merge(final String pIndex, final IIndexable pDocument,
            final Map<String, Object> pMergedPropertiesMap) {
        return merge(pIndex, pDocument.getType(), pDocument.getDocId(), pMergedPropertiesMap);
    }

    /**
     * Searching first page of all elements from index giving page size.
     *
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageSize page size
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T> Page<T> searchAllLimited(final String pIndex, final Class<T> pClass, final int pPageSize) {
        return this.searchAllLimited(pIndex, pClass, new PageRequest(0, pPageSize));
    }

    /**
     * Searching specified page of all elements from index (for first call use {@link #searchAllLimited(String, Class, int)} method) <b>This method fails if asked for offset greater than 10000
     * (Elasticsearch limitation)</b>
     *
     * @param pIndex index
     * @param pClass class of document type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param <T> class of document type
     * @return specified result page
     */
    <T> Page<T> searchAllLimited(String pIndex, Class<T> pClass, Pageable pPageRequest);

    /**
     * Searching first page of elements from index giving page size with facets.
     *
     * @param searchKey the search key specifying on which index and type the search must be applied and the class of return objects type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param pFacetsMap map of (attribute name - facet type). Can be null if no facet asked for.
     * @param pAscSortMap map of (attributes name - true if ascending). Can be null if no sort asked for.
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T extends IIndexable> Page<T> search(final SearchKey<T, T> searchKey, final int pPageSize,
            final ICriterion pCriterion, final Map<String, FacetType> pFacetsMap,
            final LinkedHashMap<String, Boolean> pAscSortMap) {
        return this.search(searchKey, new PageRequest(0, pPageSize, new LinkedHashMapToSort().convert(pAscSortMap)),
                           pCriterion, pFacetsMap);
    }

    /**
     * Searching specified page of elements from index (for first call use {@link #searchAllLimited(String, Class, int)} method) with facets. <b>This method fails if asked for offset greater than
     * 10000 (Elasticsearch limitation)</b>
     *
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pCriterion search criterion
     * @param pFacetsMap map of (attribute name - facet type). Can be null if no facet asked for.
     * @param searchKey the search key
     * @param <T> class of document type
     * @return specified result page
     */
    <T extends IIndexable> FacetPage<T> search(SearchKey<T, T> searchKey, Pageable pPageRequest, ICriterion pCriterion,
            Map<String, FacetType> pFacetsMap);

    /**
     * Searching first page of elements from index giving page size without facets.
     *
     * @param searchKey the search key specifying on which index and type the search must be applied and the class of return objects type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T extends IIndexable> Page<T> search(final SearchKey<T, T> searchKey, final int pPageSize,
            final LinkedHashMap<String, Boolean> pAscSortMap, final ICriterion pCriterion) {
        return this.search(searchKey, pPageSize, pCriterion, (Map<String, FacetType>) null, pAscSortMap);
    }

    /**
     * Searching first page of elements from index giving page size without sort.
     *
     * @param searchKey the search key specifying on which index and type the search must be applied and the class of return objects type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T extends IIndexable> Page<T> search(final SearchKey<T, T> searchKey, final int pPageSize,
            final ICriterion pCriterion, final Map<String, FacetType> pFacetsMap) {
        return this.search(searchKey, pPageSize, pCriterion, pFacetsMap, (LinkedHashMap<String, Boolean>) null);
    }

    /**
     * Searching first page of elements from index giving page size without facets nor sort
     *
     * @param searchKey the search key specifying on which index and type the search must be applied and the class of return objects type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T extends IIndexable> Page<T> search(final SearchKey<T, T> searchKey, final int pPageSize,
            final ICriterion pCriterion) {
        return this.search(searchKey, pPageSize, pCriterion, (Map<String, FacetType>) null);
    }

    /**
     * Searching specified page of elements from index (for first call use {@link #searchAllLimited(String, Class, int)} method) without facets nor sort. <b>This method fails if asked for offset
     * greater than 10000 (Elasticsearch limitation)</b>
     *
     * @param searchKey the search key specifying on which index and type the search must be applied and the class of return objects type
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pCriterion search criterion
     * @param <T> class of document type
     * @return specified result page
     */
    default <T extends IIndexable> Page<T> search(final SearchKey<T, T> searchKey, final Pageable pPageRequest,
            final ICriterion pCriterion) {
        return this.search(searchKey, pPageRequest, pCriterion, (Map<String, FacetType>) null);
    }

    /**
     * Searching first page of elements from index giving page size. The results are reduced to given inner property that's why no sorting can be done.
     *
     * @param searchKey the search key specifying on which index and type the search must be applied and the class of return objects type
     * @param pPageSize page size
     * @param pCriterion search criterion
     * @param pSourceAttribute if the search is on a document but the result shoult be an inner property of the results documents
     * @param <T> inner result property type
     * @return first result page containing max page size documents
     */
    default <T extends IIndexable> Page<T> search(final SearchKey<?, T> searchKey, final int pPageSize,
            final ICriterion pCriterion, final String pSourceAttribute) {
        return this.search(searchKey, pPageSize, pCriterion, pSourceAttribute);
    }

    /**
     * Searching first page of elements from index giving page size and facet map. The results are reduced to given inner property that's why no sorting can be done.
     *
     * @param searchKey the search key specifying on which index and type the search must be applied and the class of return objects type
     * @param pCriterion search criterion
     * @param pSourceAttribute if the search is on a document but the result shoult be an inner property of the results documents
     * @param <T> class of inner sourceAttribute
     * @return all results (ordered is garanteed to be always the same)
     */
    <T> List<T> search(SearchKey<?, T> searchKey, ICriterion pCriterion, String pSourceAttribute);

    /**
     * Same as {@link #search(SearchKey, ICriterion, String)} providing a transform function to apply on all results
     *
     * @param <T> class of inner sourceAttribute
     * @param <U> result class of transform function
     */
    <T, U> List<U> search(SearchKey<?, T> searchKey, ICriterion pCriterion, String pSourceAttribute,
            Function<T, U> transformFct);

    /**
     * Same as {@link #search(SearchKey, ICriterion, String, Function)} with an intermediate filter predicate to be applied before the transform function
     */
    <T, U> List<U> search(SearchKey<?, T[]> searchKey, ICriterion criterion, String sourceAttribute,
            Predicate<T> filterPredicate, Function<T, U> transformFct);

    /**
     * Count result
     * @param searchKey the search key
     * @param criterion search criterion
     * @return
     */
    <T extends IIndexable> Long count(SearchKey<?, T> searchKey, ICriterion criterion);

    /**
     * Retrieve sum of given attribute
     * @param searchKey the search key
     * @param pCrit search criterion
     * @param attName complete attribute path
     * @return the sum
     */
    <T extends IIndexable> double sum(SearchKey<?, T> searchKey, ICriterion pCrit, String attName);

    /**
     * Retrieve minimum date of given date attribute
     * @param searchKey the search key
     * @param pCrit search criterion
     * @param attName complete attribute path
     * @return the min
     */
    <T extends IIndexable> OffsetDateTime minDate(SearchKey<?, T> searchKey, ICriterion pCrit, String attName);

    /**
     * Retrieve maximum date of given date attribute
     * @param searchKey the search key
     * @param pCrit search criterion
     * @param attName complete attribute path
     * @return the max
     */
    <T extends IIndexable> OffsetDateTime maxDate(SearchKey<?, T> searchKey, ICriterion pCrit, String attName);

    /**
     * Searching first page of elements from index giving page size
     *
     * @param searchKey the search key
     * @param pPageSize page size
     * @param pValue value to search
     * @param pFields fields to search on (use '.' for inner objects, ie "attributes.tags"). <b>Fields types must be consistent with given value type</b>
     * @param <T> document type
     * @return first result page containing max page size documents
     */
    default <T> Page<T> multiFieldsSearch(final SearchKey<T, T> searchKey, final int pPageSize, final Object pValue,
            final String... pFields) {
        return this.multiFieldsSearch(searchKey, new PageRequest(0, pPageSize), pValue, pFields);
    }

    /**
     * Searching specified page of elements from index giving page size (for first call us {@link #multiFieldsSearch(SearchKey, int, Object, String...)} method
     *
     * @param searchKey the search key
     * @param pPageRequest page request (use {@link Page#nextPageable()} method for example)
     * @param pValue value to search
     * @param pFields fields to search on (use '.' for inner objects, ie "attributes.tags"). Wildcards '*' can be used too
     *                (ie attributes.dataRange.*). <b>Fields types must be consistent with given value type</b>
     * @param <T> document type
     * @return specified result page
     */
    <T> Page<T> multiFieldsSearch(SearchKey<T, T> searchKey, Pageable pPageRequest, Object pValue, String... pFields);

    /**
     * Execute specified action for all search results<br/>
     * <b>No 10000 offset Elasticsearch limitation</b>
     *
     * @param searchKey the search key specifying the index and type to search and the result class used
     * @param pAction action to be executed for each search result element
     * @param pCriterion search criterion
     */
    <T> void searchAll(SearchKey<T, T> searchKey, Consumer<T> pAction, ICriterion pCriterion);

    /**
     * Execute specified action for all search results<br/>
     * <b>No 10000 offset Elasticsearch limitation</b>
     *
     * @param searchKey the search key     * @param pAction action to be executed for each search result element
     * @param pAttributeSource inner attribute to be used as ES "_source" results
     */
    <T> void searchAll(SearchKey<?, T> searchKey, Consumer<T> pAction, ICriterion pCriterion, String pAttributeSource);

    /**
     * Close Client
     */
    void close();
}
