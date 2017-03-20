/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.exception.RabbitMQVhostException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.dao.IGroupAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.dao.IUserAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.GroupAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.UserAccessRight;
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
public class AccessRightService {

    private final IAccessRightRepository<AbstractAccessRight> repository;

    private final IGroupAccessRightRepository groupRepository;

    private final IUserAccessRightRepository userRepository;

    private final AccessGroupService accessGroupService;

    private final IDatasetService datasetService;

    private final IPublisher eventPublisher;

    public AccessRightService(IAccessRightRepository<AbstractAccessRight> pAccessRightRepository,
            IGroupAccessRightRepository pGroupRepository, IUserAccessRightRepository pUserRepository,
            AccessGroupService pAccessGroupService, IDatasetService pDatasetService, IPublisher pEventPublisher) {
        repository = pAccessRightRepository;
        groupRepository = pGroupRepository;
        userRepository = pUserRepository;
        accessGroupService = pAccessGroupService;
        datasetService = pDatasetService;
        eventPublisher = pEventPublisher;
    }

    /**
     * @param pAccessGroupName
     * @param pDatasetIpId
     * @param pPageable
     * @return
     * @throws EntityNotFoundException
     */
    public Page<AbstractAccessRight> retrieveAccessRights(String pAccessGroupName, UniformResourceName pDatasetIpId,
            String pUserEmail, Pageable pPageable) throws EntityNotFoundException {
        if (pUserEmail != null) {
            Page<UserAccessRight> page = retrieveAccessRightsByUser(pDatasetIpId, pUserEmail, pPageable);
            List<AbstractAccessRight> content = page.getContent().stream().map(gar -> (AbstractAccessRight) gar)
                    .collect(Collectors.toList());
            return new PageImpl<>(content, pPageable, page.getTotalElements());
        }
        if (pAccessGroupName != null) {
            Page<GroupAccessRight> page = retrieveAccessRightsByAccessGroup(pDatasetIpId, pAccessGroupName, pPageable);
            List<AbstractAccessRight> content = page.getContent().stream().map(uar -> (AbstractAccessRight) uar)
                    .collect(Collectors.toList());
            return new PageImpl<>(content, pPageable, page.getTotalElements());
        }
        return retrieveAccessRightsByDataset(pDatasetIpId, pPageable);
    }

    /**
     * @param pDatasetIpId
     * @param pPageable
     * @return
     * @throws EntityNotFoundException
     */
    private Page<AbstractAccessRight> retrieveAccessRightsByDataset(UniformResourceName pDatasetIpId,
            Pageable pPageable) throws EntityNotFoundException {
        if (pDatasetIpId != null) {
            Dataset ds = datasetService.load(pDatasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
            }

            return repository.findAllByDataset(ds, pPageable);
        }
        return repository.findAll(pPageable);
    }

    /**
     * @param pDatasetIpId
     * @param pUserEmail
     * @param pPageable
     * @return
     * @throws EntityNotFoundException
     */
    private Page<UserAccessRight> retrieveAccessRightsByUser(UniformResourceName pDatasetIpId, String pUserEmail,
            Pageable pPageable) throws EntityNotFoundException {
        if (!accessGroupService.existUser(new User(pUserEmail))) {
            throw new EntityNotFoundException(pUserEmail, User.class);
        }
        User user = new User(pUserEmail);
        if (pDatasetIpId != null) {
            Dataset ds = datasetService.load(pDatasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
            }

            return userRepository.findAllByUserAndDataset(user, ds, pPageable);
        } else {
            return userRepository.findAllByUser(user, pPageable);
        }
    }

    /**
     * @param pDatasetIpId
     * @param pAccessGroupName
     * @param pPageable
     * @return
     * @throws EntityNotFoundException
     */
    private Page<GroupAccessRight> retrieveAccessRightsByAccessGroup(UniformResourceName pDatasetIpId,
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

            return groupRepository.findAllByAccessGroupAndDataset(ag, ds, pPageable);
        } else {
            return groupRepository.findAllByAccessGroup(ag, pPageable);
        }
    }

    /**
     * @param pAccessRight
     * @return
     * @throws ModuleException
     * @throws RabbitMQVhostException
     */
    public AbstractAccessRight createAccessRight(AbstractAccessRight pAccessRight) throws ModuleException {
        Dataset dataset = datasetService.load(pAccessRight.getDataset().getId());
        if (dataset == null) {
            throw new EntityNotFoundException(pAccessRight.getDataset().getId(), Dataset.class);
        }

        if (pAccessRight instanceof GroupAccessRight) {
            AccessGroup accessGroup = ((GroupAccessRight) pAccessRight).getAccessGroup();
            if (!accessGroupService.existGroup(accessGroup.getId())) {
                throw new EntityNotFoundException(accessGroup.getId(), AccessGroup.class);
            }
            // Adding group to Dataset
            dataset.getGroups().add(accessGroup.getName());
            datasetService.update(dataset);
        } else {
            if (pAccessRight instanceof UserAccessRight) {
                User user = ((UserAccessRight) pAccessRight).getUser();
                if (!accessGroupService.existUser(user)) {
                    throw new EntityNotFoundException(user.getEmail(), User.class);
                }
            }
        }
        AbstractAccessRight created = repository.save(pAccessRight);
        eventPublisher.publish(new AccessRightCreated(created.getId()));
        return created;
    }

    /**
     * @param pId
     * @return
     * @throws EntityNotFoundException
     */
    public AbstractAccessRight retrieveAccessRight(Long pId) throws EntityNotFoundException {
        AbstractAccessRight result = repository.findOne(pId);
        if (result == null) {
            throw new EntityNotFoundException(pId, AbstractAccessRight.class);
        }
        return result;
    }

    /**
     * @param pId
     * @param pToBe
     * @return
     * @throws EntityNotFoundException
     * @throws EntityInconsistentIdentifierException
     * @throws RabbitMQVhostException
     */
    public AbstractAccessRight updateAccessRight(Long pId, AbstractAccessRight pToBe)
            throws EntityNotFoundException, EntityInconsistentIdentifierException {
        AbstractAccessRight toBeUpdated = repository.findOne(pId);
        if (toBeUpdated == null) {
            throw new EntityNotFoundException(pId, AbstractAccessRight.class);
        }
        if (!pToBe.getId().equals(pId)) {
            throw new EntityInconsistentIdentifierException(pId, pToBe.getId(), AbstractAccessRight.class);
        }
        AbstractAccessRight updated = repository.save(toBeUpdated);
        eventPublisher.publish(new AccessRightUpdated(pId));
        return updated;
    }

    /**
     * @param pId
     * @throws RabbitMQVhostException
     */
    public void deleteAccessRight(Long pId) {
        repository.delete(pId);
        eventPublisher.publish(new AccessRightDeleted(pId));
    }

}
