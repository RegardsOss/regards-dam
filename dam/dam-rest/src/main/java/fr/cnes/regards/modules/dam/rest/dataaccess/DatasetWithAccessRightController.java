/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.dto.DatasetWithAccessRight;
import fr.cnes.regards.modules.dam.service.dataaccess.IDatasetWithAccessRightService;

/**
 * Handle datasets access rights
 * @author Sébastien Binda
 */
@RestController
@RequestMapping(value = DatasetWithAccessRightController.ROOT_PATH)
public class DatasetWithAccessRightController implements IResourceController<DatasetWithAccessRight> {

    /**
     * Endpoint for datasets
     */
    public static final String ROOT_PATH = "/datasets/access-rights";

    public static final String GROUP_PATH = "/group/{accessGroupName}";

    @Autowired
    private IDatasetWithAccessRightService service;

    /**
     * {@link IResourceService} instance
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Retrieve datasets with access rights
     * @param accessGroupName
     * @param label String for label filter
     * @param pageRequest the page request
     * @param assembler the dataset resources assembler
     * @return the page of dataset wrapped in an HTTP response
     * @throws ModuleException
     */
    @RequestMapping(value = GROUP_PATH, method = RequestMethod.GET)
    @ResourceAccess(description = "endpoint to retrieve the list of all datasets")
    public ResponseEntity<PagedResources<Resource<DatasetWithAccessRight>>> retrieveDatasets(
            @PathVariable(name = "accessGroupName") String accessGroupName,
            @RequestParam(name = "datasetLabel", required = false) String label,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageRequest,
            PagedResourcesAssembler<DatasetWithAccessRight> assembler) throws ModuleException {
        final Page<DatasetWithAccessRight> datasetsWithAR = service.search(label, accessGroupName, pageRequest);
        final PagedResources<Resource<DatasetWithAccessRight>> resources = toPagedResources(datasetsWithAR, assembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @Override
    public Resource<DatasetWithAccessRight> toResource(DatasetWithAccessRight element, Object... extras) {
        Resource<DatasetWithAccessRight> resource = resourceService.toResource(element);
        if (element.getAccessRight() != null) {
            resourceService.addLink(resource, AccessRightController.class, "deleteAccessRight", LinkRels.DELETE,
                                    MethodParamFactory.build(Long.class, element.getAccessRight().getId()));
        }
        return resourceService.toResource(element);
    }

}
