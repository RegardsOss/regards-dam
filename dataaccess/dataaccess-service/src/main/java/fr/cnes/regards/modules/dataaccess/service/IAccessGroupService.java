package fr.cnes.regards.modules.dataaccess.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;

/**
 * Created by oroussel on 03/07/17.
 */
public interface IAccessGroupService {

    void setMicroserviceName(String pMicroserviceName);

    Page<AccessGroup> retrieveAccessGroups(Pageable pPageable);

    AccessGroup createAccessGroup(AccessGroup pToBeCreated) throws EntityAlreadyExistsException;

    AccessGroup retrieveAccessGroup(String pAccessGroupName) throws EntityNotFoundException;

    void deleteAccessGroup(String pAccessGroupName);

    AccessGroup associateUserToAccessGroup(String userEmail, String accessGroupName)
            throws EntityNotFoundException;

    AccessGroup dissociateUserFromAccessGroup(String userEmail, String accessGroupName)
                    throws EntityNotFoundException;

    Page<AccessGroup> retrieveAccessGroupsOfUser(String pUserEmail, Pageable pPageable);

    void setAccessGroupsOfUser(String pUserEmail, List<AccessGroup> pNewAcessGroups)
            throws EntityNotFoundException;

    boolean existGroup(Long pId);

    boolean existUser(User pUser);

    AccessGroup update(String pAccessGroupName, AccessGroup pAccessGroup) throws ModuleException;
}
