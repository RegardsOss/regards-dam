/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.dataaccess;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;

/**
 * @author Olivier Roussel
 */
public interface IAccessGroupService {

    Page<AccessGroup> retrieveAccessGroups(Boolean isPublic, Pageable pPageable);

    AccessGroup createAccessGroup(AccessGroup pToBeCreated) throws EntityAlreadyExistsException;

    AccessGroup retrieveAccessGroup(String pAccessGroupName) throws EntityNotFoundException;

    void deleteAccessGroup(String pAccessGroupName) throws EntityOperationForbiddenException, EntityNotFoundException;

    AccessGroup associateUserToAccessGroup(String userEmail, String accessGroupName) throws EntityNotFoundException;

    AccessGroup dissociateUserFromAccessGroup(String userEmail, String accessGroupName) throws EntityNotFoundException;

    Set<AccessGroup> retrieveAllUserAccessGroupsOrPublicAccessGroups(String pUserEmail);

    Page<AccessGroup> retrieveUserAccessGroups(String pUserEmail, Pageable pPageable);

    void setAccessGroupsOfUser(String pUserEmail, List<AccessGroup> pNewAcessGroups) throws EntityNotFoundException;

    boolean existGroup(Long pId);

    AccessGroup update(String pAccessGroupName, AccessGroup pAccessGroup) throws ModuleException;

    void initDefaultAccessGroup();
}
