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
package fr.cnes.regards.modules.dam.dao.dataaccess;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;

/**
 * @author Sylvain Vissiere-Guerinet
 */
public interface IAccessRightRepository extends JpaRepository<AccessRight, Long> {

    /**
     * Retrieve an AccessRight with the associated Dataset and AccessGroup.
     * @param pId the {@link AccessRight} to retrieve
     * @return {@link AccessRight} with {@link Dataset} associated.
     * @since 1.0-SNAPSHOT
     */
    @Override
    @EntityGraph(value = "graph.accessright.dataset.and.accessgroup")
    Optional<AccessRight> findById(Long pId);

    @EntityGraph(value = "graph.accessright.dataset.and.accessgroup")
    Page<AccessRight> findAllByDataset(Dataset dataset, Pageable pageable);

    @Override
    @EntityGraph(value = "graph.accessright.dataset.and.accessgroup")
    Page<AccessRight> findAll(Pageable pageable);

    @EntityGraph(value = "graph.accessright.dataset.and.accessgroup")
    List<AccessRight> findAllByDataset(Dataset dataset);

    @EntityGraph(value = "graph.accessright.dataset.and.accessgroup")
    Page<AccessRight> findAllByAccessGroup(AccessGroup accessGroup, Pageable pageable);

    /**
     * This method returns zero or one AccessRight
     * @param accessGroup
     * @param dataset
     * @param pageable
     * @return {@link AccessRight}s by page
     */
    @EntityGraph(value = "graph.accessright.plugins")
    Page<AccessRight> findAllByAccessGroupAndDataset(AccessGroup accessGroup, Dataset dataset, Pageable pageable);

    /**
     * This methods return only zero or one AccessRight
     * @param accessGroup
     * @param dataset
     * @return {@link AccessRight}
     */
    @EntityGraph(value = "graph.accessright.plugins")
    Optional<AccessRight> findAccessRightByAccessGroupAndDataset(AccessGroup accessGroup, Dataset dataset);

    /**
     * Find all {@link AccessRight}s associated a dataAccess plugin.
     * @return {@link AccessRight}s
     */
    @EntityGraph(value = "graph.accessright.dataset.and.accessgroup")
    List<AccessRight> findByDataAccessPluginNotNull();

}
