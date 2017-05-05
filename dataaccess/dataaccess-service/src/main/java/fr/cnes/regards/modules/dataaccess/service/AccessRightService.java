/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    public AccessRightService(IAccessRightRepository pAccessRightRepository, AccessGroupService pAccessGroupService,
            IDatasetService pDatasetService, IPublisher pEventPublisher) {
        repository = pAccessRightRepository;
        accessGroupService = pAccessGroupService;
        datasetService = pDatasetService;
        eventPublisher = pEventPublisher;
    }

    public Page<AccessRight> retrieveAccessRights(String pAccessGroupName, UniformResourceName pDatasetIpId,
            Pageable pPageable) throws EntityNotFoundException {
        if (pAccessGroupName != null) {
            return retrieveAccessRightsByAccessGroup(pDatasetIpId, pAccessGroupName, pPageable);
        }
        return retrieveAccessRightsByDataset(pDatasetIpId, pPageable);
    }

    private Page<AccessRight> retrieveAccessRightsByDataset(UniformResourceName pDatasetIpId, Pageable pPageable)
            throws EntityNotFoundException {
        if (pDatasetIpId != null) {
            Dataset ds = datasetService.load(pDatasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
            }

            return repository.findAllByDataset(ds, pPageable);
        }
        return repository.findAll(pPageable);
    }

    private Page<AccessRight> retrieveAccessRightsByAccessGroup(UniformResourceName pDatasetIpId,
            String pAccessGroupName, Pageable pPageable) throws EntityNotFoundException {
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

    public AccessRight createAccessRight(AccessRight pAccessRight) throws ModuleException {
        Dataset dataset = datasetService.load(pAccessRight.getDataset().getId());
        if (dataset == null) {
            throw new EntityNotFoundException(pAccessRight.getDataset().getId(), Dataset.class);
        }

        AccessGroup accessGroup = pAccessRight.getAccessGroup();
        if (!accessGroupService.existGroup(accessGroup.getId())) {
            throw new EntityNotFoundException(accessGroup.getId(), AccessGroup.class);
        }
        // Adding group to Dataset
        dataset.getGroups().add(accessGroup.getName());
        // Save dataset through DatasetService (no cascade on AbstractAccessRight.dataset)
        datasetService.update(dataset);
        // re-set updated dataset on accessRight to make its state correct on accessRight object
        pAccessRight.setDataset(dataset);

        AccessRight created = repository.save(pAccessRight);
        eventPublisher.publish(new AccessRightCreated(created.getId()));
        return created;
    }

    public AccessRight retrieveAccessRight(Long pId) throws EntityNotFoundException {
        AccessRight result = repository.findOne(pId);
        if (result == null) {
            throw new EntityNotFoundException(pId, AccessRight.class);
        }
        return result;
    }

    public AccessRight updateAccessRight(Long pId, AccessRight pToBe) throws ModuleException {
        AccessRight toBeUpdated = repository.findOne(pId);
        if (toBeUpdated == null) {
            throw new EntityNotFoundException(pId, AccessRight.class);
        }
        if (!pToBe.getId().equals(pId)) {
            throw new EntityInconsistentIdentifierException(pId, pToBe.getId(), AccessRight.class);
        }
        // Remove current group from dataset if accessRight is a GroupAccessRight
        Dataset dataset = datasetService.load(toBeUpdated.getDataset().getId());
        if (dataset != null) {

            // If group has changed
            AccessGroup currentAccessGroup = toBeUpdated.getAccessGroup();
            AccessGroup newAccessGroup = pToBe.getAccessGroup();
            if (!Objects.equals(currentAccessGroup, newAccessGroup)) {
                dataset.getGroups().remove(currentAccessGroup.getName());
                dataset.getGroups().add(newAccessGroup.getName());
                datasetService.update(dataset);
                pToBe.setDataset(dataset);
            }

        }
        AccessRight updated = repository.save(pToBe);
        eventPublisher.publish(new AccessRightUpdated(pId));
        return updated;
    }

    public void deleteAccessRight(Long pId) throws ModuleException {
        AccessRight accessRight = repository.findOne(pId);
        // Remove current group from dataset if accessRight is a GroupAccessRight
        Dataset dataset = datasetService.load(accessRight.getDataset().getId());
        if (dataset != null) {
            dataset.getGroups().remove(accessRight.getAccessGroup().getName());
            datasetService.update(dataset);
        }
        repository.delete(pId);
        eventPublisher.publish(new AccessRightDeleted(pId));
    }

}
