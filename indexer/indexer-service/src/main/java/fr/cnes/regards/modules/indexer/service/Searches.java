/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.indexer.service;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;

import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.indexer.domain.JoinEntitySearchKey;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;

/**
 * Factory class for search (types, keys, etc...)
 * @author oroussel
 */
public final class Searches {

    protected static final BiMap<EntityType, Class<? extends AbstractEntity>> TYPE_MAP = EnumHashBiMap
            .create(EntityType.class);

    static {
        TYPE_MAP.put(EntityType.COLLECTION, fr.cnes.regards.modules.dam.domain.entities.Collection.class);
        TYPE_MAP.put(EntityType.DATASET, Dataset.class);
        TYPE_MAP.put(EntityType.DATA, DataObject.class);
    }

    private static final Map<String, Class<? extends AbstractEntity>> SEARCH_TYPE_MAP = TYPE_MAP.keySet().stream()
            .collect(Collectors.toMap(EntityType::toString, type -> TYPE_MAP.get(type)));

    private Searches() {
    }

    public static EntityType fromClass(Class<?> clazz) {
        return TYPE_MAP.inverse().get(clazz);
    }

    /**
     * Define a search key on a single entity type returning this single entity type
     * @param entityType search and result type
     * @return a SimpleSearchKey of AbstractEntity inherited type
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEntity> SimpleSearchKey<T> onSingleEntity(EntityType entityType) {
        return new SimpleSearchKey<>(entityType.toString(), (Class<T>) TYPE_MAP.get(entityType));
    }

    /**
     * Define a search key on all entities ie each search result will be of AbstractEntity inherited type
     * @return a SimpleSearchKey of AbstractEntity
     */
    public static SimpleSearchKey<AbstractEntity> onAllEntities() {
        return new SimpleSearchKey<AbstractEntity>(SEARCH_TYPE_MAP);
    }

    /**
     * Define a search key on a single entity returning a joined entity (via tags property) of given type
     * @param searchType search type ie the one concerned by criterions search
     * @param resultJoinType result type ie to be extracted from tags property and loaded on Elasticsearch
     * @return a SearchKey of search type and result type, both inherited from AbstractEntity
     */
    @SuppressWarnings("unchecked")
    public static <S extends AbstractEntity, R extends AbstractEntity> JoinEntitySearchKey<S, R> onSingleEntityReturningJoinEntity(
            EntityType searchType, EntityType resultJoinType) {
        return new JoinEntitySearchKey<S, R>(searchType.toString(), (Class<S>) TYPE_MAP.get(searchType),
                (Class<R>) TYPE_MAP.get(resultJoinType));
    }

    /**
     * Define a search key on all entities returning a joined entity (via tags property) of given type
     * @param resultJoinType result type ie to be extracted from tags property and loaded on Elasticsearch
     * @return a SearchKey of search type and result type, search type is AbstractEntity, result type is inherited from
     * AbstractEntity
     */
    @SuppressWarnings("unchecked")
    public static <R extends AbstractEntity> JoinEntitySearchKey<AbstractEntity, R> onAllEntitiesReturningJoinEntity(
            EntityType resultJoinType) {
        return new JoinEntitySearchKey<AbstractEntity, R>(SEARCH_TYPE_MAP, (Class<R>) TYPE_MAP.get(resultJoinType));
    }
}
