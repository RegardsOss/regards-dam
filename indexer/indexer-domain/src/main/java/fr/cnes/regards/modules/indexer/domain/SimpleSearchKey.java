package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

/**
 * Simple search key where search and result type are the same
 * @param <T> search and result type
 * @author oroussel
 */
public class SimpleSearchKey<T> extends SearchKey<T, T> {

    public SimpleSearchKey(String searchIndex, Map<String, Class<? extends T>> searchTypeMap, Class<T> resultClass) {
        super(searchIndex, searchTypeMap, resultClass);

    }

    public SimpleSearchKey(String searchIndex, Map<String, Class<? extends T>> searchTypeMap) {
        super(searchIndex, searchTypeMap);

    }

    public SimpleSearchKey(String searchIndex, String searchType, Class<? extends T> searchClass,
            Class<T> resultClass) {
        super(searchIndex, searchType, searchClass, resultClass);

    }

    public SimpleSearchKey(String searchIndex, String searchType, Class<? extends T> searchClass) {
        super(searchIndex, searchType, searchClass);

    }

}
