package fr.cnes.regards.modules.indexer.domain;

import java.util.Map;

public class JoinEntitySearchKey<S, R> extends SearchKey<S, R> {

    public JoinEntitySearchKey(Map<String, Class<? extends S>> searchTypeMap, Class<R> resultClass) {
        super(searchTypeMap, resultClass);

    }

    public JoinEntitySearchKey(Map<String, Class<? extends S>> searchTypeMap) {
        super(searchTypeMap);

    }

    public JoinEntitySearchKey(String searchType, Class<? extends S> searchClass, Class<R> resultClass) {
        super(searchType, searchClass, resultClass);

    }

    public JoinEntitySearchKey(String searchType, Class<? extends S> searchClass) {
        super(searchType, searchClass);

    }

}
