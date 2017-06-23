package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

import org.springframework.util.Assert;

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
     * @param pSearchIndex index to search on (or tenant)
     * @param pSearchTypeMap map of { type, associated class with empty constructor }
     */
    public SearchKey(String pSearchIndex, Map<String, Class<? extends S>> pSearchTypeMap) {
        this(pSearchIndex, ImmutableMap.copyOf(pSearchTypeMap));
    }

    /**
     * Constructor with only one search type
     */
    public SearchKey(String pSearchIndex, String searchType, Class<? extends S> searchClass) {
        this(pSearchIndex, ImmutableMap.of(searchType, searchClass));
    }

    private SearchKey(String pSearchIndex, ImmutableMap<String, Class<? extends S>> pSearchTypeMap) {
        super();
        Assert.notNull(pSearchIndex);
        Assert.notNull(pSearchTypeMap);
        Assert.notEmpty(pSearchTypeMap);
        searchIndex = pSearchIndex;
        searchTypeMap = pSearchTypeMap;
        searchTypes = searchTypeMap.keySet().toArray(new String[searchTypeMap.size()]);
    }

    public SearchKey(String pSearchIndex, String searchType, Class<? extends S> searchClass, Class<R> pResultClass) {
        this(pSearchIndex, ImmutableMap.of(searchType, searchClass));
        resultClass = pResultClass;
    }

    /**
     * Constructor with a result class different from search types
     * @param pSearchIndex index to search on (or tenant)
     * @param pSearchTypeMap map of { type, associated class with empty constructor }
     * @param pResultClass result type
     */
    public SearchKey(String pSearchIndex, Map<String, Class<? extends S>> pSearchTypeMap, Class<R> pResultClass) {
        this(pSearchIndex, pSearchTypeMap);
        resultClass = pResultClass;
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
