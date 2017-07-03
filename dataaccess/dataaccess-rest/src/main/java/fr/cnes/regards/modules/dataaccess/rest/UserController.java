/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.service.IAccessGroupService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestController
@RequestMapping(UserController.BASE_PATH)
public class UserController implements IResourceController<AccessGroup> {

    public static final String BASE_PATH = "/users/{email}/accessgroups";

    public static final String GROUP_NAME_PATH = "/{name}";

    @Autowired
    private IAccessGroupService accessGroupService;

    @Autowired
    private IResourceService resourceService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "send the list of accessGroups of the specified user")
    public ResponseEntity<PagedResources<Resource<AccessGroup>>> retrieveAccessGroupsOfUser(
            @Valid @PathVariable("email") final String pUserEmail, final Pageable pPageable,
            final PagedResourcesAssembler<AccessGroup> pAssembler) {
        Page<AccessGroup> accessGroups = accessGroupService.retrieveAccessGroupsOfUser(pUserEmail, pPageable);
        return new ResponseEntity<>(toPagedResources(accessGroups, pAssembler), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    @ResourceAccess(description = "replace actual access groups of the user by the ones in parameter")
    public ResponseEntity<Void> setAccessGroupsOfUser(@Valid @PathVariable("email") final String pUserEmail,
            final List<AccessGroup> pNewAcessGroups) throws EntityNotFoundException {
        accessGroupService.setAccessGroupsOfUser(pUserEmail, pNewAcessGroups);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = GROUP_NAME_PATH)
    @ResponseBody
    @ResourceAccess(description = "add the access group in parameter to the specified user")
    public ResponseEntity<Void> associateAccessGroupToUser(@Valid @PathVariable("email") final String pUserEmail,
            @Valid @PathVariable("name") final String pAcessGroupNameToBeAdded) throws EntityNotFoundException {
        accessGroupService.associateUserToAccessGroup(pUserEmail, pAcessGroupNameToBeAdded);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = GROUP_NAME_PATH)
    @ResponseBody
    @ResourceAccess(description = "remove the access group in parameter to the specified user")
    public ResponseEntity<Void> dissociateAccessGroupFromUser(@Valid @PathVariable("email") final String pUserEmail,
            @Valid @PathVariable("name") final String pAcessGroupNameToBeAdded) throws EntityNotFoundException {
        accessGroupService.dissociateUserFromAccessGroup(pUserEmail, pAcessGroupNameToBeAdded);
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
