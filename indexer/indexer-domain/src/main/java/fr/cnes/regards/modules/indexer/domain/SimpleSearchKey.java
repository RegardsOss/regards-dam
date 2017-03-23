package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

/**
 * Simple search key where search and result type are the same
 * @param <T> search and result type
 * @author oroussel
 */
public class SimpleSearchKey<T> extends SearchKey<T, T> {

    public SimpleSearchKey(String pSearchIndex, Map<String, Class<? extends T>> pSearchTypeMap, Class<T> pResultClass) {
        super(pSearchIndex, pSearchTypeMap, pResultClass);

    }

    public SimpleSearchKey(String pSearchIndex, Map<String, Class<? extends T>> pSearchTypeMap) {
        super(pSearchIndex, pSearchTypeMap);

    }

    public SimpleSearchKey(String pSearchIndex, String pSearchType, Class<? extends T> pSearchClass,
            Class<T> pResultClass) {
        super(pSearchIndex, pSearchType, pSearchClass, pResultClass);

    }

    public SimpleSearchKey(String pSearchIndex, String pSearchType, Class<? extends T> pSearchClass) {
        super(pSearchIndex, pSearchType, pSearchClass);

    }

}
