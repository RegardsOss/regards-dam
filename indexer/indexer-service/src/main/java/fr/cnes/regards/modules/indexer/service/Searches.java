package fr.cnes.regards.modules.indexer.service;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.indexer.domain.JoinEntitySearchKey;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 * Factory class for search (types, keys, etc...)
 * @author oroussel
 */
public class Searches {

    protected static final BiMap<EntityType, Class<? extends AbstractEntity>> TYPE_MAP = EnumHashBiMap
            .create(EntityType.class);
    static {
        TYPE_MAP.put(EntityType.COLLECTION, fr.cnes.regards.modules.entities.domain.Collection.class);
        TYPE_MAP.put(EntityType.DATASET, Dataset.class);
        TYPE_MAP.put(EntityType.DATA, DataObject.class);
        TYPE_MAP.put(EntityType.DOCUMENT, Document.class);
    }

    private static final Map<String, Class<? extends AbstractEntity>> SEARCH_TYPE_MAP = TYPE_MAP.keySet().stream()
            .collect(Collectors.toMap(EntityType::toString, type -> TYPE_MAP.get(type)));

    public static EntityType fromClass(Class<?> clazz) {
        return TYPE_MAP.inverse().get(clazz);
    }

    /**
     * Define a search key on a single entity type returning this single entity type
     * @param index Elasticsearch index
     * @param entityType search and result type
     * @return a SimpleSearchKey of AbstractEntity inherited type
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity> SimpleSearchKey<T> onSingleEntity(String index, EntityType entityType) {
        return new SimpleSearchKey<>(index, entityType.toString(), (Class<T>) TYPE_MAP.get(entityType));
    }

    /**
     * Define a search key on all entities ie each search result will be of AbstractEntity inherited type
     * @param index Elasticsearch index
     * @return a SimpleSearchKey of AbstractEntity
     */
    public static SimpleSearchKey<AbstractEntity> onAllEntities(String index) {
        return new SimpleSearchKey<AbstractEntity>(index, SEARCH_TYPE_MAP);
    }

    /**
     * Define a search key on a single entity returning a joined entity (via tags property) of given type
     * @param index Elasticsearch index
     * @param searchType search type ie the one concerned by criterions search
     * @param resultJoinType result type ie to be extracted from tags property and loaded on Elasticsearch
     * @return a SearchKey of search type and result type, both inherited from AbstractEntity
     */
    @SuppressWarnings("unchecked")
    public static <S extends AbstractEntity, R extends AbstractEntity> JoinEntitySearchKey<S, R> onSingleEntityReturningJoinEntity(
            String index, EntityType searchType, EntityType resultJoinType) {
        return new JoinEntitySearchKey<S, R>(index, searchType.toString(), (Class<S>) TYPE_MAP.get(searchType),
                (Class<R>) TYPE_MAP.get(resultJoinType));
    }

    /**
     * Define a search key on all entities returning a joined entity (via tags property) of given type
     * @param index Elasticsearch index
     * @param resultJoinType result type ie to be extracted from tags property and loaded on Elasticsearch
     * @return a SearchKey of search type and result type, search type is AbstractEntity, result type is inherited from
     *  AbstractEntity
     */
    @SuppressWarnings("unchecked")
    public static <R extends AbstractEntity> JoinEntitySearchKey<AbstractEntity, R> onAllEntitiesReturningJoinEntity(
            String index, EntityType resultJoinType) {
        return new JoinEntitySearchKey<AbstractEntity, R>(index, SEARCH_TYPE_MAP,
                (Class<R>) TYPE_MAP.get(resultJoinType));
    }
}
