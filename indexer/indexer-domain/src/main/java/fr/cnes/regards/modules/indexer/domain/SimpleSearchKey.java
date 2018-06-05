package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

/**
 * Simple search key where search and result type are the same
 * @param <T> search and result type
 * @author oroussel
 */
public class SimpleSearchKey<T> extends SearchKey<T, T> {

    public SimpleSearchKey(Map<String, Class<? extends T>> searchTypeMap, Class<T> resultClass) {
        super(searchTypeMap, resultClass);

    }

    public SimpleSearchKey(Map<String, Class<? extends T>> searchTypeMap) {
        super(searchTypeMap);

    }

    public SimpleSearchKey(String searchType, Class<? extends T> searchClass, Class<T> resultClass) {
        super(searchType, searchClass, resultClass);

    }

    public SimpleSearchKey(String searchType, Class<? extends T> searchClass) {
        super(searchType, searchClass);

    }

}
