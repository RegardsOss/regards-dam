/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.service.AccessGroupService;

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

    public static final String PATH_ACCESS_GROUPS = "/accessgroups";

    public static final String PATH_ACCESS_GROUPS_NAME = "/{name}";

    public static final String PATH_ACCESS_GROUPS_NAME_EMAIL = PATH_ACCESS_GROUPS_NAME + "/{email}";

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private AccessGroupService accessGroupService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "send the whole list of accessGroups")
    public ResponseEntity<PagedResources<Resource<AccessGroup>>> retrieveAccessGroupsList(final Pageable pPageable,
            final PagedResourcesAssembler<AccessGroup> pAssembler) {
        Page<AccessGroup> accessGroups = accessGroupService.retrieveAccessGroups(pPageable);
        return new ResponseEntity<>(toPagedResources(accessGroups, pAssembler), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ResourceAccess(description = "create an access group according to the parameter")
    public ResponseEntity<Resource<AccessGroup>> createAccessGroup(@Valid @RequestBody AccessGroup pToBeCreated)
            throws EntityAlreadyExistsException {
        AccessGroup created = accessGroupService.createAccessGroup(pToBeCreated);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = PATH_ACCESS_GROUPS_NAME)
    @ResponseBody
    @ResourceAccess(description = "send the access group of name requested")
    public ResponseEntity<Resource<AccessGroup>> retrieveAccessGroup(
            @Valid @PathVariable("name") String pAccessGroupName) {
        final AccessGroup ag = accessGroupService.retrieveAccessGroup(pAccessGroupName);
        return new ResponseEntity<>(toResource(ag), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_GROUPS_NAME)
    @ResponseBody
    @ResourceAccess(description = "delete the access group of name requested")
    public ResponseEntity<Void> deleteAccessGroup(@Valid @PathVariable("name") String pAccessGroupName) {
        accessGroupService.deleteAccessGroup(pAccessGroupName);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_GROUPS_NAME)
    @ResponseBody
    @ResourceAccess(description = "only used to modify the privacy of the group")
    public ResponseEntity<Resource<AccessGroup>> updateAccessGroup(@Valid @PathVariable("name") String pAccessGroupName,
            @Valid AccessGroup pAccessGroup) throws ModuleException {
        final AccessGroup ag = accessGroupService.update(pAccessGroupName, pAccessGroup);
        return new ResponseEntity<>(toResource(ag), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_GROUPS_NAME_EMAIL)
    @ResponseBody
    @ResourceAccess(description = "associated the user of email specified to the access group of name requested")
    public ResponseEntity<Resource<AccessGroup>> associateUserToAccessGroup(
            @Valid @PathVariable("name") String pAccessGroupName, @Valid @PathVariable("email") String pUserEmail)
            throws EntityNotFoundException {
        final AccessGroup ag = accessGroupService.associateUserToAccessGroup(pUserEmail, pAccessGroupName);
        return new ResponseEntity<>(toResource(ag), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_GROUPS_NAME_EMAIL)
    @ResponseBody
    @ResourceAccess(description = "dissociated the user of email specified from the access group of name requested")
    public ResponseEntity<Resource<AccessGroup>> dissociateUserFromAccessGroup(
            @Valid @PathVariable("name") String pAccessGroupName, @Valid @PathVariable("email") String pUserEmail)
            throws EntityNotFoundException {
        final AccessGroup ag = accessGroupService.dissociateUserFromAccessGroup(pUserEmail, pAccessGroupName);
        return new ResponseEntity<>(toResource(ag), HttpStatus.OK);
    }

    @Override
    public Resource<AccessGroup> toResource(AccessGroup pElement, Object... pExtras) {
        Resource<AccessGroup> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "retrieveAccessGroup", LinkRels.SELF,
                                MethodParamFactory.build(String.class, pElement.getName()));
        resourceService.addLink(resource, this.getClass(), "deleteAccessGroup", LinkRels.DELETE,
                                MethodParamFactory.build(String.class, pElement.getName()));
        resourceService.addLink(resource, this.getClass(), "createAccessGroup", LinkRels.CREATE,
                                MethodParamFactory.build(AccessGroup.class, pElement));
        return resourceService.toResource(pElement);
    }
}
