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
package fr.cnes.regards.modules.dam.dao.entities;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.Collection;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Repository
public interface ICollectionRepository extends IAbstractEntityRepository<Collection> {

    List<Collection> findByGroups(String group);

    /**
     * Find all collection of which ipId belongs to given set (eagerly loading all relations)
     *
     * @param pIpIds set of ipId
     * @return found collections
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    List<Collection> findByIpIdIn(Set<UniformResourceName> pIpIds);

    /**
     * Find collection of given IpId eagerly loading all common relations
     *
     * @param pIpId ipId of which entity
     * @return found entity
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Collection findByIpId(UniformResourceName pIpId);

    /**
     * Find a collection by its id
     * @param pId
     *            id of entity
     * @return the collection or null if none were found
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Optional<Collection> findById(Long pId);
}
