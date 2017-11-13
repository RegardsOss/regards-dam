package fr.cnes.regards.modules.dataaccess.service;

import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Created by oroussel on 03/07/17.
 */
public interface IAccessGroupService {

    void setMicroserviceName(String pMicroserviceName);

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
}
