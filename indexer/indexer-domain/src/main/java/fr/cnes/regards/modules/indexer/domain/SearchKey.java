/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * Search key to be used with all search methods of IndexerService.
 * A SearchKey object identifies the ES index, the type(s) of documents to be searched for and eventually
 * the class of result object if this one is specified (different from type(s) of searched documents)
 * No search index is provided into constructor, it must be injectd afterward (in multi-tenant environment, the one
 * which create SearchIndex doesn't have think of current tenant ie current ES index)
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
     * @param searchTypeMap map of { type, associated class with empty constructor }
     */
    public SearchKey(Map<String, Class<? extends S>> searchTypeMap) {
        this(ImmutableMap.copyOf(searchTypeMap));
    }

    /**
     * Constructor with only one search type
     */
    public SearchKey(String searchType, Class<? extends S> searchClass) {
        this(ImmutableMap.of(searchType, searchClass));
    }

    private SearchKey(ImmutableMap<String, Class<? extends S>> searchTypeMap) {
        super();
        Preconditions.checkNotNull(searchTypeMap);
        Preconditions.checkArgument(searchTypeMap.size() != 0);

        this.searchTypeMap = searchTypeMap;
        this.searchTypes = searchTypeMap.keySet().toArray(new String[searchTypeMap.size()]);
    }

    public SearchKey(String searchType, Class<? extends S> searchClass, Class<R> resultClass) {
        this(ImmutableMap.of(searchType, searchClass));
        this.resultClass = resultClass;
    }

    /**
     * Constructor with a result class different from search types
     * @param searchTypeMap map of { type, associated class with empty constructor }
     * @param resultClass result type
     */
    public SearchKey(Map<String, Class<? extends S>> searchTypeMap, Class<R> resultClass) {
        this(searchTypeMap);
        this.resultClass = resultClass;
    }

    public String getSearchIndex() {
        return searchIndex;
    }

    public void setSearchIndex(String searchIndex) {
        Preconditions.checkNotNull(searchIndex);
        this.searchIndex = searchIndex.toLowerCase();
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
