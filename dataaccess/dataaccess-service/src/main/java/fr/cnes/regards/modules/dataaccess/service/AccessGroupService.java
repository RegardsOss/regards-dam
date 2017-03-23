/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.utils.HttpUtils;
import fr.cnes.regards.framework.security.utils.endpoint.RoleAuthority;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.security.utils.jwt.JwtTokenUtils;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.dataaccess.dao.IAccessGroupRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;

/**
 *
 * Service handling {@link AccessGroup}
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Service
@EnableFeignClients(clients = IProjectUsersClient.class)
public class AccessGroupService {

    public static final String ACCESS_GROUP_ALREADY_EXIST_ERROR_MESSAGE = "Access Group of name %s already exists! Name of an access group has to be unique.";

    private final IAccessGroupRepository accessGroupDao;

    private final IProjectUsersClient projectUserClient;

    private final JWTService jwtService;

    @Value("${spring.application.name}")
    private String microserviceName;

    public AccessGroupService(IAccessGroupRepository pAccessGroupDao, IProjectUsersClient pProjectUserClient,
            JWTService pJwtService) {
        accessGroupDao = pAccessGroupDao;
        projectUserClient = pProjectUserClient;
        jwtService = pJwtService;
    }

    /**
     * setter only used for unit testing purpose without spring context
     *
     * @param pMicroserviceName
     */
    public void setMicroserviceName(String pMicroserviceName) {
        microserviceName = pMicroserviceName;
    }

    /**
     * @param pPageable
     * @return
     */
    public Page<AccessGroup> retrieveAccessGroups(Pageable pPageable) {
        return accessGroupDao.findAll(pPageable);
    }

    /**
     * @param pToBeCreated
     * @return
     * @throws EntityAlreadyExistsException
     */
    public AccessGroup createAccessGroup(AccessGroup pToBeCreated) throws EntityAlreadyExistsException {
        final AccessGroup alreadyExisting = accessGroupDao.findOneByName(pToBeCreated.getName());
        if (alreadyExisting != null) {
            throw new EntityAlreadyExistsException(
                    String.format(ACCESS_GROUP_ALREADY_EXIST_ERROR_MESSAGE, pToBeCreated.getName()));
        }
        return accessGroupDao.save(pToBeCreated);
    }

    /**
     * @param pAccessGroupName
     * @return
     * @throws EntityNotFoundException
     */
    public AccessGroup retrieveAccessGroup(String pAccessGroupName) throws EntityNotFoundException {
        AccessGroup ag = accessGroupDao.findOneByName(pAccessGroupName);
        if (ag == null) {
            throw new EntityNotFoundException(pAccessGroupName, AccessGroup.class);
        }
        return ag;
    }

    /**
     * @param pAccessGroupName
     */
    public void deleteAccessGroup(String pAccessGroupName) {
        final AccessGroup toDelete = accessGroupDao.findOneByName(pAccessGroupName);
        if (toDelete != null) {
            accessGroupDao.delete(toDelete.getId());
        }
    }

    /**
     * @param pUserEmail
     * @param pAccessGroupName
     * @return
     * @throws EntityNotFoundException
     */
    public AccessGroup associateUserToAccessGroup(String pUserEmail, String pAccessGroupName)
            throws EntityNotFoundException {
        final User user = JwtTokenUtils.asSafeCallableOnRole(this::getUser, pUserEmail, jwtService, null)
                .apply(RoleAuthority.getSysRole(microserviceName));
        if (user == null) {
            throw new EntityNotFoundException(pUserEmail, ProjectUser.class);
        }
        final AccessGroup ag = accessGroupDao.findOneByName(pAccessGroupName);
        ag.addUser(user);

        return accessGroupDao.save(ag);
    }

    /**
     * @param pUserEmail
     * @return
     * @throws EntityNotFoundException
     */
    private User getUser(String pUserEmail) { // NOSONAR: method is used but by lambda so it is not recognized
        final ResponseEntity<Resource<ProjectUser>> response = projectUserClient.retrieveProjectUser(pUserEmail);
        final HttpStatus responseStatus = response.getStatusCode();
        if (!HttpUtils.isSuccess(responseStatus)) {
            // if it gets here it's mainly because of 404 so it means entity not found
            return null;
        }
        return new User(pUserEmail);
    }

    /**
     * @param pUserEmail
     * @param pAccessGroupName
     * @throws EntityNotFoundException
     */
    public AccessGroup dissociateUserFromAccessGroup(String pUserEmail, String pAccessGroupName)
            throws EntityNotFoundException {
        final User user = JwtTokenUtils.asSafeCallableOnRole(this::getUser, pUserEmail, jwtService, null)
                .apply(RoleAuthority.getSysRole(microserviceName));
        if (user == null) {
            throw new EntityNotFoundException(pUserEmail, ProjectUser.class);
        }
        final AccessGroup ag = accessGroupDao.findOneByName(pAccessGroupName);
        ag.removeUser(user);

        return accessGroupDao.save(ag);
    }

    /**
     * @param pUserEmail
     * @param pPageable
     * @return
     */
    public Page<AccessGroup> retrieveAccessGroupsOfUser(String pUserEmail, Pageable pPageable) {
        return accessGroupDao.findAllByUsersOrIsPublic(new User(pUserEmail), Boolean.TRUE, pPageable);
    }

    /**
     * @param pUserEmail
     * @param pNewAcessGroups
     * @return
     * @throws EntityNotFoundException
     */
    public void setAccessGroupsOfUser(String pUserEmail, List<AccessGroup> pNewAcessGroups)
            throws EntityNotFoundException {
        Set<AccessGroup> actualAccessGroups = accessGroupDao.findAllByUsers(new User(pUserEmail));
        for (AccessGroup actualGroup : actualAccessGroups) {
            dissociateUserFromAccessGroup(pUserEmail, actualGroup.getName());
        }
        for (AccessGroup newGroup : pNewAcessGroups) {
            associateUserToAccessGroup(pUserEmail, newGroup.getName());
        }
    }

    /**
     * @param pId
     * @return
     */
    public boolean existGroup(Long pId) {
        return accessGroupDao.exists(pId);
    }

    /**
     * @param pUser
     * @return
     */
    public boolean existUser(User pUser) {
        final User user = JwtTokenUtils.asSafeCallableOnRole(this::getUser, pUser.getEmail(), jwtService, null)
                .apply(RoleAuthority.getSysRole(microserviceName));
        return user != null;
    }

    /**
     * only update the privacy of the group
     *
     * @param pAccessGroupName
     * @param pAccessGroup
     * @return updated accessGroup
     * @throws ModuleException
     */
    public AccessGroup update(String pAccessGroupName, AccessGroup pAccessGroup) throws ModuleException {
        AccessGroup group = accessGroupDao.findOneByName(pAccessGroupName);
        if (group == null) {
            throw new EntityNotFoundException(pAccessGroupName);
        }
        if (!group.getId().equals(pAccessGroup.getId())) {
            throw new EntityInconsistentIdentifierException(group.getId(), pAccessGroup.getId(), AccessGroup.class);
        }
        group.setPublic(pAccessGroup.isPublic());
        return accessGroupDao.save(group);
    }

}