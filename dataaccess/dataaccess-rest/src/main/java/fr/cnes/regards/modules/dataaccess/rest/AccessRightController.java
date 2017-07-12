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

import java.beans.PropertyEditorSupport;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.service.IAccessRightService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestController
@RequestMapping(path = AccessRightController.PATH_ACCESS_RIGHTS, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AccessRightController implements IResourceController<AccessRight> {

    public static final String PATH_ACCESS_RIGHTS = "/accessrights";

    public static final String PATH_ACCESS_RIGHTS_ID = "/{accessright_id}";

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private IAccessRightService accessRightService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "send the list, or subset asked, of accessRight")
    public ResponseEntity<PagedResources<Resource<AccessRight>>> retrieveAccessRightsList(
            @RequestParam(name = "accessgroup", required = false) String pAccessGroupName,
            @RequestParam(name = "dataset", required = false) UniformResourceName pDatasetIpId,
            final Pageable pPageable, final PagedResourcesAssembler<AccessRight> pAssembler)
            throws EntityNotFoundException {
        Page<AccessRight> accessRights = accessRightService.retrieveAccessRights(pAccessGroupName, pDatasetIpId,
                                                                                 pPageable);
        return new ResponseEntity<>(toPagedResources(accessRights, pAssembler), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ResourceAccess(description = "create an accessRight according to the argument")
    public ResponseEntity<Resource<AccessRight>> createAccessRight(@Valid @RequestBody AccessRight pAccessRight)
            throws ModuleException {
        AccessRight created = accessRightService.createAccessRight(pAccessRight);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    @ResourceAccess(description = "send the access right of id requested")
    public ResponseEntity<Resource<AccessRight>> retrieveAccessRight(@Valid @PathVariable("accessright_id") Long pId)
            throws EntityNotFoundException {
        AccessRight requested = accessRightService.retrieveAccessRight(pId);
        return new ResponseEntity<>(toResource(requested), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    @ResourceAccess(description = "modify the access right of id requested according to the argument")
    public ResponseEntity<Resource<AccessRight>> updateAccessRight(@Valid @PathVariable("accessright_id") Long pId,
            @Valid @RequestBody AccessRight pToBe) throws ModuleException {
        AccessRight updated = accessRightService.updateAccessRight(pId, pToBe);
        return new ResponseEntity<>(toResource(updated), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_RIGHTS_ID)
    @ResponseBody
    @ResourceAccess(description = "delete the access right of id requested")
    public ResponseEntity<Void> deleteAccessRight(@Valid @PathVariable("accessright_id") Long pId)
            throws ModuleException {
        accessRightService.deleteAccessRight(pId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public Resource<AccessRight> toResource(AccessRight pElement, Object... pExtras) {
        Resource<AccessRight> resource = new Resource<>(pElement);
        resourceService.addLink(resource, this.getClass(), "createAccessRight", LinkRels.CREATE,
                                MethodParamFactory.build(AccessRight.class, pElement));
        resourceService.addLink(resource, this.getClass(), "deleteAccessRight", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateAccessRight", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(AccessRight.class, pElement));
        resourceService.addLink(resource, this.getClass(), "retrieveAccessRight", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        return resource;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(UniformResourceName.class, new PropertyEditorSupport() {

            Object value;

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public void setAsText(String text) {
                value = UniformResourceName.fromString(text);
            }
        });
    }

}
