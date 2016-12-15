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

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.dao.IGroupAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.dao.IUserAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.GroupAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.UserAccessRight;
import fr.cnes.regards.modules.dataset.domain.DataSet;
import fr.cnes.regards.modules.dataset.service.DataSetService;
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

    private final DataSetService dataSetService;

    public AccessRightService(IAccessRightRepository<AbstractAccessRight> pAccessRightRepository,
            IGroupAccessRightRepository pGroupRepository, IUserAccessRightRepository pUserRepository,
            AccessGroupService pAccessGroupService, DataSetService pDataSetService) {
        repository = pAccessRightRepository;
        groupRepository = pGroupRepository;
        userRepository = pUserRepository;
        accessGroupService = pAccessGroupService;
        dataSetService = pDataSetService;
    }

    /**
     * @param pAccessGroupName
     * @param pDataSetIpId
     * @param pPageable
     * @return
     * @throws EntityNotFoundException
     */
    public Page<AbstractAccessRight> retrieveAccessRights(String pAccessGroupName, UniformResourceName pDataSetIpId,
            String pUserEmail, Pageable pPageable) throws EntityNotFoundException {
        if (pUserEmail != null) {
            Page<UserAccessRight> page = retrieveAccessRightsByUser(pDataSetIpId, pUserEmail, pPageable);
            List<AbstractAccessRight> content = page.getContent().stream().map(gar -> (AbstractAccessRight) gar)
                    .collect(Collectors.toList());
            return new PageImpl<>(content, pPageable, page.getTotalElements());
        }
        if (pAccessGroupName != null) {
            Page<GroupAccessRight> page = retrieveAccessRightsByAccessGroup(pDataSetIpId, pAccessGroupName, pPageable);
            List<AbstractAccessRight> content = page.getContent().stream().map(uar -> (AbstractAccessRight) uar)
                    .collect(Collectors.toList());
            return new PageImpl<>(content, pPageable, page.getTotalElements());
        }
        return retrieveAccessRightsByDataSet(pDataSetIpId, pPageable);
    }

    /**
     * @param pDataSetIpId
     * @param pPageable
     * @return
     * @throws EntityNotFoundException
     */
    private Page<AbstractAccessRight> retrieveAccessRightsByDataSet(UniformResourceName pDataSetIpId,
            Pageable pPageable) throws EntityNotFoundException {
        if (pDataSetIpId != null) {
            DataSet ds = dataSetService.retrieveDataSet(pDataSetIpId);
            return repository.findAllByDataSet(ds, pPageable);
        }
        return repository.findAll(pPageable);
    }

    /**
     * @param pDataSetIpId
     * @param pUserEmail
     * @param pPageable
     * @return
     * @throws EntityNotFoundException
     */
    private Page<UserAccessRight> retrieveAccessRightsByUser(UniformResourceName pDataSetIpId, String pUserEmail,
            Pageable pPageable) throws EntityNotFoundException {
        if (!accessGroupService.existUser(new User(pUserEmail))) {
            throw new EntityNotFoundException(pUserEmail, User.class);
        }
        User user = new User(pUserEmail);
        if (pDataSetIpId != null) {
            DataSet ds = dataSetService.retrieveDataSet(pDataSetIpId);
            return userRepository.findAllByUserAndDataSet(user, ds, pPageable);
        } else {
            return userRepository.findAllByUser(user, pPageable);
        }
    }

    /**
     * @param pDataSetIpId
     * @param pAccessGroupName
     * @param pPageable
     * @return
     * @throws EntityNotFoundException
     */
    private Page<GroupAccessRight> retrieveAccessRightsByAccessGroup(UniformResourceName pDataSetIpId,
            String pAccessGroupName, Pageable pPageable) throws EntityNotFoundException {
        AccessGroup ag = accessGroupService.retrieveAccessGroup(pAccessGroupName);
        if (ag == null) {
            throw new EntityNotFoundException(pAccessGroupName, AccessGroup.class);
        }
        if (pDataSetIpId != null) {
            DataSet ds = dataSetService.retrieveDataSet(pDataSetIpId);
            return groupRepository.findAllByAccessGroupAndDataSet(ag, ds, pPageable);
        } else {
            return groupRepository.findAllByAccessGroup(ag, pPageable);
        }
    }

    /**
     * @param pAccessRight
     * @return
     * @throws EntityNotFoundException
     */
    public AbstractAccessRight createAccessRight(AbstractAccessRight pAccessRight) throws EntityNotFoundException {
        dataSetService.retrieveDataSet(pAccessRight.getDataset().getId());
        if (pAccessRight instanceof GroupAccessRight) {
            Long accessGroupId = ((GroupAccessRight) pAccessRight).getAccessGroup().getId();
            if (accessGroupService.existGroup(accessGroupId)) {
                throw new EntityNotFoundException(accessGroupId, AccessGroup.class);
            }
        } else {
            if (pAccessRight instanceof UserAccessRight) {
                User user = ((UserAccessRight) pAccessRight).getUser();
                if (accessGroupService.existUser(user)) {
                    throw new EntityNotFoundException(user.getEmail(), User.class);
                }
            }
        }
        return repository.save(pAccessRight);
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
        return repository.save(toBeUpdated);
    }

    /**
     * @param pId
     */
    public void deleteAccessRight(Long pId) {
        repository.delete(pId);
    }

}
