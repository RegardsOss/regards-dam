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

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;

/**
 * Specific requests on Dataset
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Repository
public interface IDatasetRepository extends IAbstractEntityRepository<Dataset> {

    List<Dataset> findByGroups(String group);

    /**
     * Find entity giving its id eagerly loading its common relations (ie relations defined into AbstractEntity)
     * @param pId id of entity
     * @return entity
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues" })
    Dataset findById(Long pId);

    /**
     * Find all datasets of which ipId belongs to given set (eagerly loading all relations)
     * @param pIpIds set of ipId
     * @return found entities
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues" })
    List<Dataset> findByIpIdIn(Set<UniformResourceName> pIpIds);

    /**
     * Find entity of given IpId eagerly loading all common relations (except pluginConfigurationIds)
     * @param pIpId ipId of which entity
     * @return found entity
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues" })
    Dataset findByIpId(UniformResourceName pIpId);

    /**
     * Find all entities complient with the given modelName
     * @param pModelName name of the model we want to be complient with
     * @return datasets complient with the given model
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues" })
    Set<Dataset> findAllByModelName(String pModelName);

    /**
     * Find all entities complient with given model ids
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues" })
    Set<Dataset> findAllByModelIdIn(Set<Long> pModelIds);
}