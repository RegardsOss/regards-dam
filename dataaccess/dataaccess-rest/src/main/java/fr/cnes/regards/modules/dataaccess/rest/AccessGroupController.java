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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dataaccess.domain.AccessGroup;
import fr.cnes.regards.modules.dataaccess.service.AccessGroupService;

/**
 * REST module controller
 *
 * TODO Description
 *
 * @author TODO
 *
 */
@RestController
@ModuleInfo(name = "dataaccess", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
@RequestMapping(AccessGroupController.PATH_ACCESS_GROUPS)
public class AccessGroupController implements IResourceController<AccessGroup> {

    public static final String PATH_ACCESS_GROUPS = "/accessgroups";

    public static final String PATH_ACCESS_GROUPS_NAME = PATH_ACCESS_GROUPS + "/{name}";

    public static final String PATH_ACCESS_GROUPS_NAME_LOGIN = PATH_ACCESS_GROUPS_NAME + "/{login}";

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private AccessGroupService accessGroupService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "send the whole list of accessGroups")
    public HttpEntity<PagedResources<Resource<AccessGroup>>> retrieveAccessGroupsList(final Pageable pPageable,
            final PagedResourcesAssembler<AccessGroup> pAssembler) {
        Page<AccessGroup> accessGroups = accessGroupService.retrieveAccessGroups(pPageable);
        return new ResponseEntity<>(toPagedResources(accessGroups, pAssembler), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ResourceAccess(description = "create an access group according to the parameter")
    public HttpEntity<Resource<AccessGroup>> createAccessGroup(@Valid @RequestBody AccessGroup pToBeCreated) {
        AccessGroup created = accessGroupService.createAccessGroup(pToBeCreated);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    @Override
    public Resource<AccessGroup> toResource(AccessGroup pElement, Object... pExtras) {
        // TODO add hateoas links
        return resourceService.toResource(pElement);
    }
}
