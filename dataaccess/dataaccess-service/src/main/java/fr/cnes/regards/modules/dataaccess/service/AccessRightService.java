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
package fr.cnes.regards.modules.dataaccess.service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.DataAccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.event.AccessRightEvent;
import fr.cnes.regards.modules.dataaccess.domain.accessright.event.AccessRightEventType;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.IDatasetService;

/**
 * Access right service implementation
 * @author Sylvain Vissiere-Guerinet
 */
@Service
@MultitenantTransactional
public class AccessRightService implements IAccessRightService {

    @Autowired
    private IAccessRightRepository repository;

    @Autowired
    private IAccessGroupService accessGroupService;

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private IPublisher eventPublisher;

    @Override
    public Page<AccessRight> retrieveAccessRights(String accessGroupName, UniformResourceName datasetIpId,
            Pageable pageable) throws ModuleException {
        if (accessGroupName != null) {
            return retrieveAccessRightsByAccessGroup(datasetIpId, accessGroupName, pageable);
        }
        return retrieveAccessRightsByDataset(datasetIpId, pageable);
    }

    private Page<AccessRight> retrieveAccessRightsByDataset(UniformResourceName datasetIpId, Pageable pageable)
            throws ModuleException {
        if (datasetIpId != null) {
            Dataset ds = datasetService.load(datasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(datasetIpId.toString(), Dataset.class);
            }

            return repository.findAllByDataset(ds, pageable);
        }
        return repository.findAll(pageable);
    }

    @Override
    public Optional<AccessRight> retrieveAccessRight(String accessGroupName, UniformResourceName datasetIpId)
            throws ModuleException {
        Preconditions.checkNotNull(accessGroupName);
        Preconditions.checkNotNull(datasetIpId);
        AccessGroup ag = accessGroupService.retrieveAccessGroup(accessGroupName);
        Dataset dataset = datasetService.load(datasetIpId);

        return repository.findAccessRightByAccessGroupAndDataset(ag, dataset);
    }

    @Override
    public Map<String, Pair<AccessLevel, DataAccessLevel>> retrieveGroupAccessLevelMap(UniformResourceName datasetIpId)
            throws ModuleException {
        if (datasetIpId == null) {
            throw new IllegalArgumentException("datasetIpId must not be null");
        }
        return retrieveAccessRightsByDataset(datasetIpId, new PageRequest(0, Integer.MAX_VALUE)).getContent().stream()
                .collect(Collectors.toMap(r -> r.getAccessGroup().getName(), accessRight -> Pair
                        .of(accessRight.getAccessLevel(), accessRight.getDataAccessRight().getDataAccessLevel())));
    }

    private Page<AccessRight> retrieveAccessRightsByAccessGroup(UniformResourceName pDatasetIpId,
            String pAccessGroupName, Pageable pPageable) throws ModuleException {
        AccessGroup ag = accessGroupService.retrieveAccessGroup(pAccessGroupName);
        if (ag == null) {
            throw new EntityNotFoundException(pAccessGroupName, AccessGroup.class);
        }
        if (pDatasetIpId != null) {
            Dataset ds = datasetService.load(pDatasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
            }

            return repository.findAllByAccessGroupAndDataset(ag, ds, pPageable);
        } else {
            return repository.findAllByAccessGroup(ag, pPageable);
        }
    }

    @Override
    public AccessRight createAccessRight(AccessRight accessRight) throws ModuleException {
        Dataset dataset = datasetService.load(accessRight.getDataset().getId());
        if (dataset == null) {
            throw new EntityNotFoundException(accessRight.getDataset().getId(), Dataset.class);
        }

        AccessGroup accessGroup = accessRight.getAccessGroup();
        if (!accessGroupService.existGroup(accessGroup.getId())) {
            throw new EntityNotFoundException(accessGroup.getId(), AccessGroup.class);
        }
        // Adding group to Dataset
        dataset.getGroups().add(accessGroup.getName());
        // Save dataset through DatasetService (no cascade on AbstractAccessRight.dataset)
        datasetService.update(dataset);
        // re-set updated dataset on accessRight to make its state correct on accessRight object
        accessRight.setDataset(dataset);

        AccessRight created = repository.save(accessRight);
        eventPublisher.publish(new AccessRightEvent(dataset.getIpId(), AccessRightEventType.CREATE));
        return created;
    }

    @Override
    public AccessRight retrieveAccessRight(Long id) throws EntityNotFoundException {
        AccessRight result = repository.findById(id);
        if (result == null) {
            throw new EntityNotFoundException(id, AccessRight.class);
        }
        return result;
    }

    @Override
    public AccessRight updateAccessRight(Long id, AccessRight accessRight) throws ModuleException {
        AccessRight accessRightFromDb = repository.findById(id);
        if (accessRightFromDb == null) {
            throw new EntityNotFoundException(id, AccessRight.class);
        }
        if (!accessRight.getId().equals(id)) {
            throw new EntityInconsistentIdentifierException(id, accessRight.getId(), AccessRight.class);
        }
        // Remove current group from dataset if accessRight is a GroupAccessRight
        Dataset dataset = datasetService.load(accessRightFromDb.getDataset().getId());
        if (dataset != null) {
            // If group has changed
            AccessGroup currentAccessGroup = accessRightFromDb.getAccessGroup();
            AccessGroup newAccessGroup = accessRight.getAccessGroup();
            if (!Objects.equals(currentAccessGroup, newAccessGroup)) {
                dataset.getGroups().remove(currentAccessGroup.getName());
                dataset.getGroups().add(newAccessGroup.getName());
                datasetService.update(dataset);
                accessRight.setDataset(dataset);
            }

        }
        AccessRight updated = repository.save(accessRight);
        if (dataset != null) {
            eventPublisher.publish(new AccessRightEvent(dataset.getIpId(), AccessRightEventType.UPDATE));
        }
        return updated;
    }

    @Override
    public void deleteAccessRight(Long id) throws ModuleException {
        AccessRight accessRight = repository.findById(id);
        // Remove current group from dataset if accessRight is a GroupAccessRight
        Dataset dataset = datasetService.load(accessRight.getDataset().getId());
        if (dataset != null) {
            dataset.getGroups().remove(accessRight.getAccessGroup().getName());
            datasetService.update(dataset);
        }
        repository.delete(id);
        if (dataset != null) {
            eventPublisher.publish(new AccessRightEvent(dataset.getIpId(), AccessRightEventType.DELETE));
        }
    }

    @Override
    public boolean isUserAutorisedToAccessDataset(UniformResourceName datasetIpId, String userEMail)
            throws ModuleException {
        User user = new User(userEMail);

        Dataset ds = datasetService.load(datasetIpId);
        if (ds == null) {
            throw new EntityNotFoundException(datasetIpId.toString(), Dataset.class);
        }
        Set<AccessGroup> accessGroups = accessGroupService.retrieveAllUserAccessGroupsOrPublicAccessGroups(userEMail);
        boolean isAutorised = false;
        for (AccessGroup accessGroup : accessGroups) {
            // Check if the user is concerned by the accessGroup
            if (accessGroup.isPublic() || accessGroup.getUsers().contains(user)) {
                Optional<AccessRight> accessRightOptional = repository
                        .findAccessRightByAccessGroupAndDataset(accessGroup, ds);
                // Check if the accessRight allows to access to that dataset
                if (accessRightOptional.isPresent()
                        && !AccessLevel.NO_ACCESS.equals(accessRightOptional.get().getAccessLevel())) {
                    isAutorised = true;
                    // Stop loop iteration
                    break;
                }
            }
        }
        return isAutorised;
    }

}
