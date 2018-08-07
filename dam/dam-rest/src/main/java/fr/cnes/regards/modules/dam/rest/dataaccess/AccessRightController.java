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

import java.beans.PropertyEditorSupport;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.service.dataaccess.IAccessRightService;

/**
 * Access right REST controller
 * @author Sylvain Vissiere-Guerinet
 * @author Léo Mieulet
 */
@RestController
@RequestMapping(path = AccessRightController.PATH_ACCESS_RIGHTS, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AccessRightController implements IResourceController<AccessRight> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessRightController.class);

    /**
     * Controller base path
     */
    public static final String PATH_ACCESS_RIGHTS = "/accessrights";

    /**
     * Controller path to retrieve ONE access right of group / dataset pair
     */
    public static final String ACCESS_RIGHT = "/accessright";

    /**
     * Controller path using an access right id as path variable
     */
    public static final String PATH_ACCESS_RIGHTS_ID = "/{accessright_id}";

    /**
     * Controller path used to know whether a dataset is accessible
     */
    public static final String PATH_IS_DATASET_ACCESSIBLE = "/isAccessible";

    /**
     * {@link IResourceService} instance
     */
    @Autowired
    private IResourceService resourceService;

    @Autowired
    private IAccessRightService accessRightService;

    /**
     * Retrieve a page of access rights according to the parameters
     * @param accessGroupName name of the access group which the access rights belongs to
     * @param datasetIpId ip id of the dataset which is constrained by the access rights
     * @param pageable page information
     * @param assembler page assembler
     * @return page of access rights
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResourceAccess(description = "send the list, or subset asked, of accessRight")
    public ResponseEntity<PagedResources<Resource<AccessRight>>> retrieveAccessRightsList(
            @RequestParam(name = "accessgroup", required = false) String accessGroupName,
            @RequestParam(name = "dataset", required = false) UniformResourceName datasetIpId, final Pageable pageable,
            final PagedResourcesAssembler<AccessRight> assembler) throws ModuleException {
        Page<AccessRight> accessRights = accessRightService.retrieveAccessRights(accessGroupName, datasetIpId,
                                                                                 pageable);
        return new ResponseEntity<>(toPagedResources(accessRights, assembler), HttpStatus.OK);
    }

    /**
     * Retrieve access group and dataset pair access right or nothing
     */
    @RequestMapping(method = RequestMethod.GET, path = ACCESS_RIGHT)
    @ResourceAccess(description = "Retrieve access right of given access group / dataset if there is one",
            role = DefaultRole.PUBLIC)
    public ResponseEntity<AccessRight> retrieveAccessRight(@RequestParam(name = "accessgroup") String accessGroupName,
            @RequestParam(name = "dataset") UniformResourceName datasetIpId) throws ModuleException {
        try {
            Optional<AccessRight> accessRightOpt = accessRightService.retrieveAccessRight(accessGroupName, datasetIpId);
            if (accessRightOpt.isPresent()) {
                return new ResponseEntity<>(accessRightOpt.get(), HttpStatus.OK);
            }
        } catch (EntityNotFoundException e) {
            LOGGER.info("Either group or dataset does not exist", e);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Create an access right
     * @return created access right
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create an accessRight according to the argument")
    public ResponseEntity<Resource<AccessRight>> createAccessRight(@Valid @RequestBody AccessRight accessRight)
            throws ModuleException {
        AccessRight created = accessRightService.createAccessRight(accessRight);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    /**
     * Retrieve an access right by its id
     * @return retrieved access right
     */
    @RequestMapping(method = RequestMethod.GET, path = PATH_ACCESS_RIGHTS_ID)
    @ResourceAccess(description = "send the access right of id requested")
    public ResponseEntity<Resource<AccessRight>> retrieveAccessRight(@Valid @PathVariable("accessright_id") Long id)
            throws ModuleException {
        AccessRight requested = accessRightService.retrieveAccessRight(id);
        return new ResponseEntity<>(toResource(requested), HttpStatus.OK);
    }

    /**
     * Update an access right.
     * @return updated access right
     */
    @RequestMapping(method = RequestMethod.PUT, path = PATH_ACCESS_RIGHTS_ID)
    @ResourceAccess(description = "modify the access right of id requested according to the argument")
    public ResponseEntity<Resource<AccessRight>> updateAccessRight(@Valid @PathVariable("accessright_id") Long id,
            @Valid @RequestBody AccessRight toBe) throws ModuleException {
        AccessRight updated = accessRightService.updateAccessRight(id, toBe);
        return new ResponseEntity<>(toResource(updated), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = PATH_ACCESS_RIGHTS_ID)
    @ResourceAccess(description = "delete the access right of id requested")
    public ResponseEntity<Void> deleteAccessRight(@Valid @PathVariable("accessright_id") Long id)
            throws ModuleException {
        accessRightService.deleteAccessRight(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.GET, path = PATH_IS_DATASET_ACCESSIBLE)
    @ResourceAccess(description = "check if an user has access to a dataset")
    public ResponseEntity<Boolean> isUserAutorisedToAccessDataset(
            @RequestParam(name = "dataset") UniformResourceName datasetIpId,
            @RequestParam(name = "user") String userEMail) throws ModuleException {
        boolean hasAccessToDataset = accessRightService.isUserAutorisedToAccessDataset(datasetIpId, userEMail);
        return new ResponseEntity<>(hasAccessToDataset, HttpStatus.OK);
    }

    @Override
    public Resource<AccessRight> toResource(AccessRight accessRight, Object... extras) {
        Resource<AccessRight> resource = new Resource<>(accessRight);
        resourceService.addLink(resource, this.getClass(), "createAccessRight", LinkRels.CREATE,
                                MethodParamFactory.build(AccessRight.class, accessRight));
        resourceService.addLink(resource, this.getClass(), "deleteAccessRight", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, accessRight.getId()));
        resourceService.addLink(resource, this.getClass(), "updateAccessRight", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, accessRight.getId()),
                                MethodParamFactory.build(AccessRight.class, accessRight));
        resourceService.addLink(resource, this.getClass(), "retrieveAccessRight", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, accessRight.getId()));
        return resource;
    }

    /**
     * Data binder to recognize {@link UniformResourceName}
     */
    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        dataBinder.registerCustomEditor(UniformResourceName.class, new PropertyEditorSupport() {

            /**
             * The value
             */
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
