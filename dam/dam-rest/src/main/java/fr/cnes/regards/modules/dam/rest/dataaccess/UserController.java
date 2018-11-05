/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.dataaccess;

import javax.validation.Valid;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.service.dataaccess.IAccessGroupService;

/**
 * DAM User REST controller
 * @author Sylvain Vissiere-Guerinet
 */
@RestController
@RequestMapping(UserController.BASE_PATH)
public class UserController implements IResourceController<AccessGroup> {

    /**
     * Controller base path
     */
    public static final String BASE_PATH = "/users/{email}/accessgroups";

    /**
     * Controller path using a group name as path variable
     */
    public static final String GROUP_NAME_PATH = "/{name}";

    /**
     * {@link IAccessGroupService} instance
     */
    @Autowired
    private IAccessGroupService accessGroupService;

    /**
     * {@link IResourceService} instance
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Retrieve page of a user, represented by its email, access groups
     * @return page of a user access groups
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "send the list of accessGroups of the specified user")
    public ResponseEntity<PagedResources<Resource<AccessGroup>>> retrieveAccessGroupsOfUser(
            @Valid @PathVariable("email") String userEmail,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            final PagedResourcesAssembler<AccessGroup> pAssembler) {
        Page<AccessGroup> accessGroups = accessGroupService.retrieveUserAccessGroups(userEmail, pageable);
        return new ResponseEntity<>(toPagedResources(accessGroups, pAssembler), HttpStatus.OK);
    }

    /**
     * Set a user, represented by its email, access groups
     */
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    @ResourceAccess(description = "replace actual access groups of the user by the ones in parameter")
    public ResponseEntity<Void> setAccessGroupsOfUser(@Valid @PathVariable("email") String userEmail,
            List<AccessGroup> newAccessGroups) throws EntityNotFoundException {
        accessGroupService.setAccessGroupsOfUser(userEmail, newAccessGroups);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Dissociate an access group, represented by its name, to a user, represented by its email
     */

    @RequestMapping(method = RequestMethod.PUT, value = GROUP_NAME_PATH)
    @ResponseBody
    @ResourceAccess(description = "add the access group in parameter to the specified user")
    public ResponseEntity<Void> associateAccessGroupToUser(@Valid @PathVariable("email") String userEmail,
            @Valid @PathVariable("name") String accessGroupNameToBeAdded) throws EntityNotFoundException {
        accessGroupService.associateUserToAccessGroup(userEmail, accessGroupNameToBeAdded);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Dissociate an access group, represented by its name, from a user, represented by its email
     */
    @RequestMapping(method = RequestMethod.DELETE, value = GROUP_NAME_PATH)
    @ResponseBody
    @ResourceAccess(description = "remove the access group in parameter to the specified user")
    public ResponseEntity<Void> dissociateAccessGroupFromUser(@Valid @PathVariable("email") String userEmail,
            @Valid @PathVariable("name") String accessGroupNameToBeAdded) throws EntityNotFoundException {
        accessGroupService.dissociateUserFromAccessGroup(userEmail, accessGroupNameToBeAdded);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public Resource<AccessGroup> toResource(AccessGroup pElement, Object... pExtras) {
        Resource<AccessGroup> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, AccessGroupController.class, "retrieveAccessGroup", LinkRels.SELF,
                                MethodParamFactory.build(String.class, pElement.getName()));
        resourceService.addLink(resource, AccessGroupController.class, "deleteAccessGroup", LinkRels.DELETE,
                                MethodParamFactory.build(String.class, pElement.getName()));
        resourceService.addLink(resource, AccessGroupController.class, "createAccessGroup", LinkRels.CREATE,
                                MethodParamFactory.build(AccessGroup.class, pElement));
        return resourceService.toResource(pElement);
    }

}
