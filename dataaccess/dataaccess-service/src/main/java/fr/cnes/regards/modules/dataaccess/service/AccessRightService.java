/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.event.AccessRightCreated;
import fr.cnes.regards.modules.dataaccess.domain.accessright.event.AccessRightDeleted;
import fr.cnes.regards.modules.dataaccess.domain.accessright.event.AccessRightUpdated;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Service
@MultitenantTransactional
public class AccessRightService {

    private final IAccessRightRepository repository;

    private final AccessGroupService accessGroupService;

    private final IDatasetService datasetService;

    private final IPublisher eventPublisher;

    public AccessRightService(final IAccessRightRepository pAccessRightRepository,
            final AccessGroupService pAccessGroupService, final IDatasetService pDatasetService,
            final IPublisher pEventPublisher) {
        repository = pAccessRightRepository;
        accessGroupService = pAccessGroupService;
        datasetService = pDatasetService;
        eventPublisher = pEventPublisher;
    }

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
        eventPublisher.publish(new AccessRightCreated(created.getId()));
        return created;
    }

    public AccessRight retrieveAccessRight(final Long pId) throws EntityNotFoundException {
        final AccessRight result = repository.findById(pId);
        if (result == null) {
            throw new EntityNotFoundException(pId, AccessRight.class);
        }
        return result;
    }

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
        eventPublisher.publish(new AccessRightUpdated(pId));
        return updated;
    }

    public void deleteAccessRight(final Long pId) throws ModuleException {
        final AccessRight accessRight = repository.findById(pId);
        // Remove current group from dataset if accessRight is a GroupAccessRight
        final Dataset dataset = datasetService.load(accessRight.getDataset().getId());
        if (dataset != null) {
            dataset.getGroups().remove(accessRight.getAccessGroup().getName());
            datasetService.update(dataset);
        }
        repository.delete(pId);
        eventPublisher.publish(new AccessRightDeleted(pId));
    }

}
