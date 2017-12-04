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
package fr.cnes.regards.modules.dataaccess.rest;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.service.IAccessGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Controller REST handling requests about {@link AccessGroup}s to the data
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestController
@ModuleInfo(name = "dataaccess", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CNES",
        documentation = "http://test")
@RequestMapping(AccessGroupController.PATH_ACCESS_GROUPS)
public class AccessGroupController implements IResourceController<AccessGroup> {

    /**
     * Controller base path
     */
    public static final String PATH_ACCESS_GROUPS = "/accessgroups";

    /**
     * Controller path using an access group name as path variable
     */
    public static final String PATH_ACCESS_GROUPS_NAME = "/{name}";

    /**
     * Controller path using an access group name and a user email as path variable
     */
    public static final String PATH_ACCESS_GROUPS_NAME_EMAIL = PATH_ACCESS_GROUPS_NAME + "/{email}";

    /**
     * {@link IResourceService} instance
     */
    @Autowired
    private IResourceService resourceService;

    @Autowired
    private IAccessGroupService accessGroupService;

    @RequestMapping(method = RequestMethod.GET)
    @ResourceAccess(description = "send the whole list of accessGroups")
    public ResponseEntity<PagedResources<Resource<AccessGroup>>> retrieveAccessGroupsList(
            @RequestParam(name = "public", required = false) Boolean isPublic, final Pageable pPageable,
            final PagedResourcesAssembler<AccessGroup> pAssembler) {
        final Page<AccessGroup> accessGroups = accessGroupService.retrieveAccessGroups(isPublic, pPageable);
        return new ResponseEntity<>(toPagedResources(accessGroups, pAssembler), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create an access group according to the parameter")
    public ResponseEntity<Resource<AccessGroup>> createAccessGroup(@Valid @RequestBody final AccessGroup pToBeCreated)
            throws EntityAlreadyExistsException {
        final AccessGroup created = accessGroupService.createAccessGroup(pToBeCreated);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    /**
     * Retrieve an access group by its name
     * @param pAccessGroupName
     * @return retrieved access group
     * @throws EntityNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = PATH_ACCESS_GROUPS_NAME)
    @ResourceAccess(description = "send the access group of name requested")
    public ResponseEntity<Resource<AccessGroup>> retrieveAccessGroup(
            @Valid @PathVariable("name") final String pAccessGroupName) throws EntityNotFoundException {
        final AccessGroup ag = accessGroupService.retrieveAccessGroup(pAccessGroupName);
        return new ResponseEntity<>(toResource(ag), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_GROUPS_NAME)
    @ResourceAccess(description = "delete the access group of name requested")
    public ResponseEntity<Void> deleteAccessGroup(@Valid @PathVariable("name") final String pAccessGroupName) throws EntityOperationForbiddenException, EntityNotFoundException {
        accessGroupService.deleteAccessGroup(pAccessGroupName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_GROUPS_NAME)
    @ResourceAccess(description = "only used to modify the privacy of the group")
    public ResponseEntity<Resource<AccessGroup>> updateAccessGroup(
            @Valid @PathVariable("name") final String pAccessGroupName,
            @Valid @RequestBody final AccessGroup pAccessGroup) throws ModuleException {
        final AccessGroup ag = accessGroupService.update(pAccessGroupName, pAccessGroup);
        return new ResponseEntity<>(toResource(ag), HttpStatus.OK);
    }

    /**
     * Associate a user, represented by its email, to an access group, represented by its name
     * @param pAccessGroupName
     * @param pUserEmail
     * @return the updated access group
     * @throws EntityNotFoundException
     */
    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_GROUPS_NAME_EMAIL)
    @ResourceAccess(description = "associated the user of email specified to the access group of name requested")
    public ResponseEntity<Resource<AccessGroup>> associateUserToAccessGroup(
            @Valid @PathVariable("name") final String pAccessGroupName,
            @Valid @PathVariable("email") final String pUserEmail) throws EntityNotFoundException {
        final AccessGroup ag = accessGroupService.associateUserToAccessGroup(pUserEmail, pAccessGroupName);
        return new ResponseEntity<>(toResource(ag), HttpStatus.OK);
    }

    /**
     * dissociate a user, represented by its email, from an access group, represented by its name.
     * @param pAccessGroupName
     * @param pUserEmail
     * @return the updated access group
     * @throws EntityNotFoundException
     */
    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_GROUPS_NAME_EMAIL)
    @ResourceAccess(description = "dissociated the user of email specified from the access group of name requested")
    public ResponseEntity<Resource<AccessGroup>> dissociateUserFromAccessGroup(
            @Valid @PathVariable("name") final String pAccessGroupName,
            @Valid @PathVariable("email") final String pUserEmail) throws EntityNotFoundException {
        final AccessGroup ag = accessGroupService.dissociateUserFromAccessGroup(pUserEmail, pAccessGroupName);
        return new ResponseEntity<>(toResource(ag), HttpStatus.OK);
    }

    @Override
    public Resource<AccessGroup> toResource(final AccessGroup pElement, final Object... pExtras) {
        final Resource<AccessGroup> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "retrieveAccessGroup", LinkRels.SELF,
                                MethodParamFactory.build(String.class, pElement.getName()));
        resourceService.addLink(resource, this.getClass(), "updateAccessGroup", LinkRels.UPDATE,
                                MethodParamFactory.build(String.class, pElement.getName()),
                                MethodParamFactory.build(AccessGroup.class));
        if (pElement.getUsers().isEmpty()) {
            resourceService.addLink(resource, this.getClass(), "deleteAccessGroup", LinkRels.DELETE,
                                    MethodParamFactory.build(String.class, pElement.getName()));
        }
        resourceService.addLink(resource, this.getClass(), "createAccessGroup", LinkRels.CREATE,
                                MethodParamFactory.build(AccessGroup.class, pElement));
        return resource;
    }
}
