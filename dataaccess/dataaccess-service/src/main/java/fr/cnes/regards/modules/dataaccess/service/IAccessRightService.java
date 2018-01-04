/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dataaccess.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;

/**
 * @author Olivier Roussel
 */
public interface IAccessRightService {

    Page<AccessRight> retrieveAccessRights(String pAccessGroupName, UniformResourceName pDatasetIpId,
            Pageable pPageable) throws EntityNotFoundException;

    Map<String, AccessLevel> retrieveGroupAccessLevelMap(UniformResourceName datasetIpId);

    AccessRight createAccessRight(AccessRight pAccessRight) throws ModuleException;

    AccessRight retrieveAccessRight(Long pId) throws EntityNotFoundException;

    AccessRight updateAccessRight(Long pId, AccessRight pToBe) throws ModuleException;

    void deleteAccessRight(Long pId) throws ModuleException;

    Boolean isUserAutorisedToAccessDataset(UniformResourceName datasetIpId, String userEMail)
            throws EntityNotFoundException;
}
