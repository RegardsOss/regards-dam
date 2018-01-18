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

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.cnes.regards.framework.amqp.IInstanceSubscriber;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.utils.HttpUtils;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUserEvent;
import fr.cnes.regards.modules.dataaccess.dao.IAccessGroupRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.event.AccessGroupAssociationEvent;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.event.AccessGroupDissociationEvent;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.event.AccessGroupEvent;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.event.AccessGroupPublicEvent;

/**
 *
 * Service handling {@link AccessGroup}
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Léo Mieulet
 *
 */
@Service
@MultitenantTransactional
public class AccessGroupService implements ApplicationListener<ApplicationReadyEvent>, IAccessGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupService.class);

    /**
     * Access group already exist message format
     */
    public static final String ACCESS_GROUP_ALREADY_EXIST_ERROR_MESSAGE = "Access Group of name %s already exists! Name of an access group has to be unique.";

    /**
     * Name of the public access group used to allow everyone to access Documents
     */
    public static final String ACCESS_GROUP_PUBLIC_DOCUMENTS = "PublicDocumentGroup";

    /**
     * {@link IAccessGroupRepository} instance
     */
    private final IAccessGroupRepository accessGroupDao;

    /**
     * {@link IProjectUsersClient} instance
     */
    private final IProjectUsersClient projectUserClient;

    /**
     * Publish for model changes
     */
    private final IPublisher publisher;

    /**
     * {@link ISubscriber} instance
     */
    private final ISubscriber subscriber;

    /**
     * Tenant resolver to access all configured tenant
     */
    private final ITenantResolver tenantResolver;

    /**
     * {@link IRuntimeTenantResolver} instance
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    public AccessGroupService(final IAccessGroupRepository pAccessGroupDao,
            final IProjectUsersClient pProjectUserClient, final IPublisher pPublisher, final ISubscriber subscriber,
            final ITenantResolver pTenantResolver, final IInstanceSubscriber pInstanceSubscriber,
            IRuntimeTenantResolver runtimeTenantResolver) {
        super();
        accessGroupDao = pAccessGroupDao;
        projectUserClient = pProjectUserClient;
        publisher = pPublisher;
        this.subscriber = subscriber;
        this.runtimeTenantResolver = runtimeTenantResolver;

        tenantResolver = pTenantResolver;
    }

    /**
     * Post contruct
     */
    @PostConstruct
    public void init() {
        // Ensure the existence of the default Group Access for Documents.
        for (final String tenant : tenantResolver.getAllActiveTenants()) {
            try {
                // Set working tenant
                runtimeTenantResolver.forceTenant(tenant);
                initDefaultAccessGroup();
            } finally {
                runtimeTenantResolver.clearTenant();
            }

        }
    }

    @Override
    public Page<AccessGroup> retrieveAccessGroups(Boolean isPublic, final Pageable pPageable) {
        if ((isPublic != null) && isPublic) {
            return accessGroupDao.findAllByIsPublic(isPublic, pPageable);
        }
        return accessGroupDao.findAll(pPageable);
    }

    @Override
    public AccessGroup createAccessGroup(final AccessGroup pToBeCreated) throws EntityAlreadyExistsException {
        final AccessGroup alreadyExisting = accessGroupDao.findOneByName(pToBeCreated.getName());
        if (alreadyExisting != null) {
            throw new EntityAlreadyExistsException(
                    String.format(ACCESS_GROUP_ALREADY_EXIST_ERROR_MESSAGE, pToBeCreated.getName()));
        }
        final AccessGroup created = accessGroupDao.save(pToBeCreated);
        // Publish group creation
        publisher.publish(new AccessGroupEvent(created));
        // Publish public group event
        if (created.isPublic()) {
            publisher.publish(new AccessGroupPublicEvent(created));
        }
        return created;
    }

    @Override
    public AccessGroup retrieveAccessGroup(final String pAccessGroupName) throws EntityNotFoundException {
        final AccessGroup ag = accessGroupDao.findOneByName(pAccessGroupName);
        if (ag == null) {
            throw new EntityNotFoundException(pAccessGroupName, AccessGroup.class);
        }
        return ag;
    }

    @Override
    public void deleteAccessGroup(final String pAccessGroupName)
            throws EntityOperationForbiddenException, EntityNotFoundException {
        final AccessGroup toDelete = accessGroupDao.findOneByName(pAccessGroupName);
        if (toDelete == null) {
            throw new EntityNotFoundException(pAccessGroupName, AccessGroup.class);
        } else {
            // Prevent users to delete the public AccessGroup used by Documents
            if (toDelete.isInternal()) {
                throw new EntityOperationForbiddenException(toDelete.getName(), AccessGroup.class,
                        "Cannot remove the public access group used by Documents");
            }
            accessGroupDao.delete(toDelete.getId());
            // Publish attribute deletion
            publisher.publish(new AccessGroupEvent(toDelete));
            // Publish public group event
            if (toDelete.isPublic()) {
                publisher.publish(new AccessGroupPublicEvent(toDelete));
            }
        }
    }

    @Override
    public AccessGroup associateUserToAccessGroup(final String userEmail, final String accessGroupName)
            throws EntityNotFoundException {
        final User user = getUser(userEmail);
        if (user == null) {
            throw new EntityNotFoundException(userEmail, ProjectUser.class);
        }
        final AccessGroup ag = accessGroupDao.findOneByName(accessGroupName);
        ag.addUser(user);
        final AccessGroup updated = accessGroupDao.save(ag);
        // Publish
        publisher.publish(new AccessGroupAssociationEvent(updated, userEmail));
        return updated;
    }

    private User getUser(final String pUserEmail) { // NOSONAR: method is used but by lambda so it is not recognized
        try {
            FeignSecurityManager.asSystem();
            final ResponseEntity<Resource<ProjectUser>> response = projectUserClient
                    .retrieveProjectUserByEmail(pUserEmail);
            final HttpStatus responseStatus = response.getStatusCode();
            if (!HttpUtils.isSuccess(responseStatus)) {
                // if it gets here it's mainly because of 404 so it means entity not found
                return null;
            }
            return new User(pUserEmail);
        } finally {
            FeignSecurityManager.reset();
        }
    }

    @Override
    public AccessGroup dissociateUserFromAccessGroup(final String userEmail, final String accessGroupName)
            throws EntityNotFoundException {
        final User user = getUser(userEmail);
        if (user == null) {
            throw new EntityNotFoundException(userEmail, ProjectUser.class);
        }
        final AccessGroup ag = accessGroupDao.findOneByName(accessGroupName);
        ag.removeUser(user);
        final AccessGroup updated = accessGroupDao.save(ag);
        // Publish
        publisher.publish(new AccessGroupDissociationEvent(updated, userEmail));
        return updated;
    }

    @Override
    public Set<AccessGroup> retrieveAllUserAccessGroupsOrPublicAccessGroups(final String pUserEmail) {
        return accessGroupDao.findAllByUsersOrIsPublic(new User(pUserEmail), Boolean.TRUE);
    }

    @Override
    public Page<AccessGroup> retrieveUserAccessGroups(String pUserEmail, Pageable pPageable) {
        return accessGroupDao.findAllByUsers(new User(pUserEmail), pPageable);
    }

    @Override
    public void setAccessGroupsOfUser(final String pUserEmail, final List<AccessGroup> pNewAcessGroups)
            throws EntityNotFoundException {
        final Set<AccessGroup> actualAccessGroups = accessGroupDao.findAllByUsers(new User(pUserEmail));
        for (final AccessGroup actualGroup : actualAccessGroups) {
            dissociateUserFromAccessGroup(pUserEmail, actualGroup.getName());
        }
        for (final AccessGroup newGroup : pNewAcessGroups) {
            associateUserToAccessGroup(pUserEmail, newGroup.getName());
        }
    }

    @Override
    public boolean existGroup(final Long pId) {
        return accessGroupDao.exists(pId);
    }

    @Override
    public AccessGroup update(final String pAccessGroupName, final AccessGroup pAccessGroup) throws ModuleException {
        final AccessGroup group = accessGroupDao.findOneByName(pAccessGroupName);
        if (group == null) {
            throw new EntityNotFoundException(pAccessGroupName);
        }
        if (!group.getId().equals(pAccessGroup.getId())) {
            throw new EntityInconsistentIdentifierException(group.getId(), pAccessGroup.getId(), AccessGroup.class);
        }
        // Update public visibility
        group.setPublic(pAccessGroup.isPublic());
        accessGroupDao.save(group);

        // Publish public group event
        publisher.publish(new AccessGroupPublicEvent(group));

        return group;
    }

    private void removeUser(String email) {
        User toRemove = new User(email);
        Set<AccessGroup> groupsToProcess = accessGroupDao.findAllByUsers(toRemove);
        for (AccessGroup toProcess : groupsToProcess) {
            toProcess.removeUser(toRemove);
            accessGroupDao.save(toProcess);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    // this method is called out of context on microservice initialization
    public void onApplicationEvent(ApplicationReadyEvent event) {
        subscriber.subscribeTo(ProjectUserEvent.class, new ProjectUserEventHandler());
    }

    /**
     * Project user event handler
     */
    private class ProjectUserEventHandler implements IHandler<ProjectUserEvent> {

        @Override
        public void handle(TenantWrapper<ProjectUserEvent> pWrapper) {
            String tenant = pWrapper.getTenant();
            runtimeTenantResolver.forceTenant(tenant);
            ProjectUserEvent event = pWrapper.getContent();
            switch (event.getAction()) {
                case DELETION:
                    removeUser(event.getEmail());
                    break;
                default:
                    // nothing to do
                    break;
            }

        }

    }

    @Override
    public void initDefaultAccessGroup() {
        // Build the AccessGroup we need in the current project
        final AccessGroup publicDocumentAccessGroup = new AccessGroup();
        publicDocumentAccessGroup.setName(AccessGroupService.ACCESS_GROUP_PUBLIC_DOCUMENTS);
        publicDocumentAccessGroup.setPublic(true);
        publicDocumentAccessGroup.setInternal(true);
        try {
            createAccessGroup(publicDocumentAccessGroup);
        } catch (EntityAlreadyExistsException e) {
            // the entity already exists, no problem
        } catch (Exception e) {
            LOGGER.error("Failed to register the public AccessGroup used by documents");
            // Do not prevent microservice to boot
        }
    }
}