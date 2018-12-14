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
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.Document;

/**
 * Specific requests on Dataset
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 * @author lmieulet
 */
@Repository
public interface IDocumentRepository extends IAbstractEntityRepository<Document> {

    List<Document> findByGroups(String group);

    /**
     * Find document giving its id eagerly loading its common relations (ie relations defined into AbstractEntity)
     * @param pId document id
     * @return document
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Optional<Document> findById(Long pId);

    /**
     * Find all documents of which ipId belongs to given set (eagerly loading all relations)
     * @param pIpIds set of ipId
     * @return found entities
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    List<Document> findByIpIdIn(Set<UniformResourceName> pIpIds);

    /**
     * Find document of given IpId eagerly loading all common relations (except pluginConfigurationIds)
     * @param pIpId document ipId
     * @return found document
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Document findByIpId(UniformResourceName pIpId);

    /**
     * Find all entities complient with the given modelName
     * @param pModelName name of the model we want to be complient with
     * @return documents complient with the given model
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Set<Document> findAllByModelName(String pModelName);

    /**
     * Find all entities complient with the given modelName
     * @param pModelIds model ids we want to be complient with
     * @return documents complient with the given model
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "model" })
    Set<Document> findAllByModelIdIn(Set<Long> pModelIds);

}
