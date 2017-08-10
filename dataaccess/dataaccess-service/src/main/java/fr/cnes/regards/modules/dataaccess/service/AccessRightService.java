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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.event.AccessRightEvent;
import fr.cnes.regards.modules.dataaccess.domain.accessright.event.AccessRightEventType;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.framework.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@Service
@MultitenantTransactional
public class AccessRightService implements IAccessRightService {

    private final IAccessRightRepository repository;

    private final IAccessGroupService accessGroupService;

    private final IDatasetService datasetService;

    private final IPublisher eventPublisher;

    public AccessRightService(final IAccessRightRepository pAccessRightRepository,
            final IAccessGroupService pAccessGroupService, final IDatasetService pDatasetService,
            final IPublisher pEventPublisher) {
        repository = pAccessRightRepository;
        accessGroupService = pAccessGroupService;
        datasetService = pDatasetService;
        eventPublisher = pEventPublisher;
    }

    @Override
    public Page<AccessRight> retrieveAccessRights(final String pAccessGroupName, final UniformResourceName pDatasetIpId,
            final Pageable pPageable) throws EntityNotFoundException {
        if (pAccessGroupName != null) {
            return retrieveAccessRightsByAccessGroup(pDatasetIpId, pAccessGroupName, pPageable);
        }
        return retrieveAccessRightsByDataset(pDatasetIpId, pPageable);
    }

    private Page<AccessRight> retrieveAccessRightsByDataset(final UniformResourceName pDatasetIpId,
            final Pageable pPageable) throws EntityNotFoundException {
        if (pDatasetIpId != null) {
            final Dataset ds = datasetService.load(pDatasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
            }

            return repository.findAllByDataset(ds, pPageable);
        }
        return repository.findAll(pPageable);
    }

    /**
     * Retrieve groups access levels of a specified dataset
     * @param datasetIpId concerned datasetIpId, must not be null
     * @return a map { groupName, accessLevel }
     * @throws EntityNotFoundException if dataset doesn't exist
     */
    @Override
    public Map<String, AccessLevel> retrieveGroupAccessLevelMap(UniformResourceName datasetIpId) {
        if (datasetIpId == null) {
            throw new IllegalArgumentException("datasetIpId must not be null");
        }
        try {
            return retrieveAccessRightsByDataset(datasetIpId, new PageRequest(0, Integer.MAX_VALUE)).getContent()
                    .stream().collect(Collectors.toMap(r -> r.getAccessGroup().getName(), AccessRight::getAccessLevel));
        } catch (EntityNotFoundException e) {
            return Collections.emptyMap();
        }
    }

    private Page<AccessRight> retrieveAccessRightsByAccessGroup(final UniformResourceName pDatasetIpId,
            final String pAccessGroupName, final Pageable pPageable) throws EntityNotFoundException {
        final AccessGroup ag = accessGroupService.retrieveAccessGroup(pAccessGroupName);
        if (ag == null) {
            throw new EntityNotFoundException(pAccessGroupName, AccessGroup.class);
        }
        if (pDatasetIpId != null) {
            final Dataset ds = datasetService.load(pDatasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
            }

            return repository.findAllByAccessGroupAndDataset(ag, ds, pPageable);
        } else {
            return repository.findAllByAccessGroup(ag, pPageable);
        }
    }

    @Override
    public AccessRight createAccessRight(final AccessRight pAccessRight) throws ModuleException {
        final Dataset dataset = datasetService.load(pAccessRight.getDataset().getId());
        if (dataset == null) {
            throw new EntityNotFoundException(pAccessRight.getDataset().getId(), Dataset.class);
        }

        final AccessGroup accessGroup = pAccessRight.getAccessGroup();
        if (!accessGroupService.existGroup(accessGroup.getId())) {
            throw new EntityNotFoundException(accessGroup.getId(), AccessGroup.class);
        }
        // Adding group to Dataset
        dataset.getGroups().add(accessGroup.getName());
        // Save dataset through DatasetService (no cascade on AbstractAccessRight.dataset)
        datasetService.update(dataset);
        // re-set updated dataset on accessRight to make its state correct on accessRight object
        pAccessRight.setDataset(dataset);

        final AccessRight created = repository.save(pAccessRight);
        eventPublisher.publish(new AccessRightEvent(dataset.getIpId(), AccessRightEventType.CREATE));
        return created;
    }

    @Override
    public AccessRight retrieveAccessRight(final Long pId) throws EntityNotFoundException {
        final AccessRight result = repository.findById(pId);
        if (result == null) {
            throw new EntityNotFoundException(pId, AccessRight.class);
        }
        return result;
    }

    @Override
    public AccessRight updateAccessRight(final Long pId, final AccessRight pToBe) throws ModuleException {
        final AccessRight toBeUpdated = repository.findById(pId);
        if (toBeUpdated == null) {
            throw new EntityNotFoundException(pId, AccessRight.class);
        }
        if (!pToBe.getId().equals(pId)) {
            throw new EntityInconsistentIdentifierException(pId, pToBe.getId(), AccessRight.class);
        }
        // Remove current group from dataset if accessRight is a GroupAccessRight
        final Dataset dataset = datasetService.load(toBeUpdated.getDataset().getId());
        if (dataset != null) {

            // If group has changed
            final AccessGroup currentAccessGroup = toBeUpdated.getAccessGroup();
            final AccessGroup newAccessGroup = pToBe.getAccessGroup();
            if (!Objects.equals(currentAccessGroup, newAccessGroup)) {
                dataset.getGroups().remove(currentAccessGroup.getName());
                dataset.getGroups().add(newAccessGroup.getName());
                datasetService.update(dataset);
                pToBe.setDataset(dataset);
            }

        }
        final AccessRight updated = repository.save(pToBe);
        eventPublisher.publish(new AccessRightEvent(dataset.getIpId(), AccessRightEventType.UPDATE));
        return updated;
    }

    @Override
    public void deleteAccessRight(final Long pId) throws ModuleException {
        final AccessRight accessRight = repository.findById(pId);
        // Remove current group from dataset if accessRight is a GroupAccessRight
        final Dataset dataset = datasetService.load(accessRight.getDataset().getId());
        if (dataset != null) {
            dataset.getGroups().remove(accessRight.getAccessGroup().getName());
            datasetService.update(dataset);
        }
        repository.delete(pId);
        eventPublisher.publish(new AccessRightEvent(dataset.getIpId(), AccessRightEventType.DELETE));
    }

}
