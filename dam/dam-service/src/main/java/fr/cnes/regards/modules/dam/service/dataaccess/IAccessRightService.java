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
package fr.cnes.regards.modules.dam.service.dataaccess;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.DataAccessLevel;

/**
 * Access right service
 * @author Sylvain Vissiere-Guerinet
 */
public interface IAccessRightService {

    /**
     * Retrieve access rights for given group and dataset
     * @param accessGroupName optional access group name
     * @param datasetIpId optional dataset ipId
     * @throws EntityNotFoundException
     */
    Page<AccessRight> retrieveAccessRights(String accessGroupName, UniformResourceName datasetIpId, Pageable pageable)
            throws ModuleException;

    /**
     * Retrieve access right for both given access group and dataset
     * @param accessGroupName mandatory access group name
     * @param datasetIpId mandatory dataset IPID
     */
    Optional<AccessRight> retrieveAccessRight(String accessGroupName, UniformResourceName datasetIpId)
            throws ModuleException;

    /**
     * Retrieve groups access levels of a specified dataset
     * @param datasetIpId concerned datasetIpId, must not be null
     * @return a map { groupName, Pair(accessLevel, dataAccessLevel) }
     * @throws EntityNotFoundException if dataset doesn't exist
     */
    Map<String, Pair<AccessLevel, DataAccessLevel>> retrieveGroupAccessLevelMap(UniformResourceName datasetIpId)
            throws ModuleException;

    AccessRight createAccessRight(AccessRight accessRight) throws ModuleException;

    AccessRight retrieveAccessRight(Long id) throws ModuleException;

    AccessRight updateAccessRight(Long id, AccessRight accessRight) throws ModuleException;

    void deleteAccessRight(Long id) throws ModuleException;

    boolean isUserAutorisedToAccessDataset(UniformResourceName datasetIpId, String userEMail) throws ModuleException;
}