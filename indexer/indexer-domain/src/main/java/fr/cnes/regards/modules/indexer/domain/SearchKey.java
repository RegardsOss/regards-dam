package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * Search key to be used with all search methods of IndexerService.
 * A SearchKey object identifies the tenant (ie ES index), the type(s) of documents to be searched for and eventually
 * the class of result object if this one is specified (different from type(s) of searched documents)
 * @param <S> search type
 * @param <R> result type
 * @author oroussel
 */
public class SearchKey<S, R> {

    private String searchIndex;

    private Class<R> resultClass = null;

    private Map<String, Class<? extends S>> searchTypeMap = null;

    private String[] searchTypes;

    /**
     * Constructor with result class to be searched from search types
     * @param searchIndex index to search on (or tenant)
     * @param searchTypeMap map of { type, associated class with empty constructor }
     */
    public SearchKey(String searchIndex, Map<String, Class<? extends S>> searchTypeMap) {
        this(searchIndex, ImmutableMap.copyOf(searchTypeMap));
    }

    /**
     * Constructor with only one search type
     */
    public SearchKey(String searchIndex, String searchType, Class<? extends S> searchClass) {
        this(searchIndex, ImmutableMap.of(searchType, searchClass));
    }

    private SearchKey(String searchIndex, ImmutableMap<String, Class<? extends S>> searchTypeMap) {
        super();
        Preconditions.checkNotNull(searchIndex);
        Preconditions.checkNotNull(searchTypeMap);
        Preconditions.checkArgument(searchTypeMap.size() != 0);

        this.searchIndex = searchIndex.toLowerCase();
        this.searchTypeMap = searchTypeMap;
        this.searchTypes = searchTypeMap.keySet().toArray(new String[searchTypeMap.size()]);
    }

    public SearchKey(String searchIndex, String searchType, Class<? extends S> searchClass, Class<R> resultClass) {
        this(searchIndex, ImmutableMap.of(searchType, searchClass));
        this.resultClass = resultClass;
    }

    /**
     * Constructor with a result class different from search types
     * @param searchIndex index to search on (or tenant)
     * @param searchTypeMap map of { type, associated class with empty constructor }
     * @param resultClass result type
     */
    public SearchKey(String searchIndex, Map<String, Class<? extends S>> searchTypeMap, Class<R> resultClass) {
        this(searchIndex, searchTypeMap);
        this.resultClass = resultClass;
    }

    public String getSearchIndex() {
        return searchIndex;
    }

    public String[] getSearchTypes() {
        return searchTypes;
    }

    public Class<R> getResultClass() {
        return resultClass;
    }

    /**
     * Return associated search type class
     * @param type type for which result class is asked for (ie the search type)
     * @return associated search type class
     */
    public Class<? extends S> fromType(String type) {
        return searchTypeMap.get(type);
    }

    /**
     * PLEASE, DON'T USE THIS METHOD
     * @return nothing useful for you, let people who know what they do play with this
     */
    public Map<String, Class<? extends S>> getSearchTypeMap() {
        return searchTypeMap;
    }

}
