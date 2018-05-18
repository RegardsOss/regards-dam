package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

public class JoinEntitySearchKey<S, R> extends SearchKey<S, R> {

    public JoinEntitySearchKey(String searchIndex, Map<String, Class<? extends S>> searchTypeMap,
            Class<R> resultClass) {
        super(searchIndex, searchTypeMap, resultClass);

    }

    public JoinEntitySearchKey(String searchIndex, Map<String, Class<? extends S>> searchTypeMap) {
        super(searchIndex, searchTypeMap);

    }

    public JoinEntitySearchKey(String searchIndex, String searchType, Class<? extends S> searchClass,
            Class<R> resultClass) {
        super(searchIndex, searchType, searchClass, resultClass);

    }

    public JoinEntitySearchKey(String searchIndex, String searchType, Class<? extends S> searchClass) {
        super(searchIndex, searchType, searchClass);

    }

}
