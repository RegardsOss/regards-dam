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

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.User;

/**
 *
 * Repository handling AccessGroup
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IAccessGroupRepository extends JpaRepository<AccessGroup, Long> {

    /**
     * find an access group by its name
     * @param pName
     * @return the access group or null if none found
     */
    @EntityGraph(value = "graph.accessgroup.users")
    AccessGroup findOneByName(String pName);

    /**
     * Retrieve all groups explicitly associated to the user
     * @param pUser user
     * @param pPageable pageable
     * @return list of groups
     */
    @EntityGraph(value = "graph.accessgroup.users")
    Page<AccessGroup> findAllByUsers(User pUser, Pageable pPageable);

    /**
     * find all groups which the user belongs to
     * @param pUser
     * @return all the groups which the user belongs to
     */
    @EntityGraph(value = "graph.accessgroup.users")
    Set<AccessGroup> findAllByUsers(User pUser);

    /**
     * find a page of groups which the user belongs to or which are public
     * @param pUser
     * @param pTrue
     * @param pPageable page informations
     * @return page of groups which the user belongs to or which are public
     */
    @EntityGraph(value = "graph.accessgroup.users")
    Page<AccessGroup> findAllByUsersOrIsPublic(User pUser, Boolean pTrue, Pageable pPageable);

    /**
     * Find all public or non public group
     * @param isPublic whether we have to select public or non public groups
     * @param pPageable {@link Pageable}
     * @return list of public or non public groups
     */
    @EntityGraph(value = "graph.accessgroup.users")
    Page<AccessGroup> findAllByIsPublic(Boolean isPublic, Pageable pPageable);

    @Override
    @EntityGraph(value = "graph.accessgroup.users")
    Page<AccessGroup> findAll(Pageable pPageable);

    @EntityGraph(value = "graph.accessgroup.users")
    Set<AccessGroup> findAllByUsersOrIsPublic(User user, Boolean aTrue);
}
