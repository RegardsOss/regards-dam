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
package fr.cnes.regards.modules.dam.dao.entities;

import java.util.List;
import java.util.Set;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.EntityAipState;

/**
 * Common requests on entities
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 * @param <T> {@link AbstractEntity}
 */
public interface IAbstractEntityRepository<T extends AbstractEntity<?>>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    /**
     * Find entity giving its id eagerly loading its common relations (ie relations defined into AbstractEntity
     * @param pId id of entity
     * @return entity
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    T findById(Long pId);

    /**
     * Find all entities of which ipId belongs to given set (eagerly loading all relations)
     * @param pIpIds set of ipId
     * @return found entities
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    List<T> findByIpIdIn(Set<UniformResourceName> pIpIds);

    /**
     * Find entity of given ipId
     * @param pIpId ipId of which entity
     * @return found entity
     */
    T findOneByIpId(UniformResourceName pIpId);

    /**
     * Find entity of given IpId eagerly loading all common relations
     * @param pIpId ipId of which entity
     * @return found entity
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    T findByIpId(UniformResourceName pIpId);

    /**
     * Find all entities complient with the given modelName
     * @param pModelName name of the model we want to be complient with
     * @return entities complient with the given model
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Set<T> findAllByModelName(String pModelName);

    /**
     * Find all entities complient with the given modelName
     * @param modelIds model list
     * @return entities complient with the given model
     */
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Set<T> findAllByModelIdIn(Set<Long> modelIds);

    /**
     * Check if at least one model is already linked to at least one entity
     * @param modelIds model list
     * @return true if no entity exists linked with at least one model
     */
    default boolean isLinkedToEntities(Set<Long> modelIds) {
        return !findAllByModelIdIn(modelIds).isEmpty();
    }

    /**
     * Find all entities containing given tag
     * @param pTagToSearch tag to search entities for
     * @return entities which contain given tag
     */
    List<T> findByTags(String pTagToSearch);

    /**
     * Find the all the entity with this specified provider id
     * @param providerId a provider id
     * @return entities corresponding to the provider id
     */
    @Query(value = "select * from {h-schema}t_entity where feature @> jsonb_build_object('providerId', ?1)",
            nativeQuery = true)
    Set<T> findAllByProviderId(String providerId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    Set<T> findAllByStateAip(EntityAipState state);
}
