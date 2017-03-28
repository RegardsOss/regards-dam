package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

public class JoinEntitySearchKey<S, R> extends SearchKey<S, R> {

    public JoinEntitySearchKey(String pSearchIndex, Map<String, Class<? extends S>> pSearchTypeMap,
            Class<R> pResultClass) {
        super(pSearchIndex, pSearchTypeMap, pResultClass);

    }

    public JoinEntitySearchKey(String pSearchIndex, Map<String, Class<? extends S>> pSearchTypeMap) {
        super(pSearchIndex, pSearchTypeMap);

    }

    public JoinEntitySearchKey(String pSearchIndex, String pSearchType, Class<? extends S> pSearchClass,
            Class<R> pResultClass) {
        super(pSearchIndex, pSearchType, pSearchClass, pResultClass);

    }

    public JoinEntitySearchKey(String pSearchIndex, String pSearchType, Class<? extends S> pSearchClass) {
        super(pSearchIndex, pSearchType, pSearchClass);

    }

}
