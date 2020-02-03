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
package fr.cnes.regards.modules.model.rest;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.service.FragmentService;
import fr.cnes.regards.modules.model.service.IAttributeModelService;
import fr.cnes.regards.modules.model.service.IFragmentService;

/**
 * REST controller for managing {@link Fragment}
 * @author Marc Sordi
 */
@RestController
@RequestMapping(FragmentController.TYPE_MAPPING)
public class FragmentController implements IResourceController<Fragment> {

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/models/fragments";

    /**
     * Prefix for imported/exported filename
     */
    private static final String FRAGMENT_FILE_PREFIX = "fragment-";

    /**
     * Suffix for imported/exported filename
     */
    private static final String FRAGMENT_EXTENSION = ".xml";

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentService.class);

    /**
     * Fragment service
     */
    private final IFragmentService fragmentService;

    /**
     * Resource service
     */
    private final IResourceService resourceService;

    /**
     * {@link IAttributeModelService} instance
     */
    private final IAttributeModelService attributeModelService;

    /**
     * Constructor setting the parameters as attributes
     * @param fragmentService  {@link IFragmentService}
     * @param resourceService  {@link IResourceService}
     * @param attributeModelService  {@link IAttributeModelService}
     */
    public FragmentController(IFragmentService fragmentService, IResourceService resourceService,
            IAttributeModelService attributeModelService) {
        this.fragmentService = fragmentService;
        this.resourceService = resourceService;
        this.attributeModelService = attributeModelService;
    }

    /**
     * Retrieve all fragments except default one
     * @return list of fragments
     */
    @ResourceAccess(description = "List all fragments")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<EntityModel<Fragment>>> getFragments() {
        return ResponseEntity.ok(toResources(fragmentService.getFragments()));
    }

    /**
     * Create a new fragment
     * @param fragment the fragment to create
     * @return the created {@link Fragment}
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Add a fragment", role = DefaultRole.ADMIN)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<EntityModel<Fragment>> addFragment(@Valid @RequestBody Fragment fragment)
            throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.addFragment(fragment)));
    }

    /**
     * Retrieve a fragment
     * @param id fragment identifier
     * @return the retrieved {@link Fragment}
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Get a fragment", role = DefaultRole.ADMIN)
    @RequestMapping(method = RequestMethod.GET, value = "/{fragmentId}")
    public ResponseEntity<EntityModel<Fragment>> getFragment(@PathVariable(name = "fragmentId") Long id)
            throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.getFragment(id)));
    }

    /**
     * Update fragment. At the moment, only its description is updatable.
     * @param id fragment identifier
     * @param fragment the fragment
     * @return the updated {@link Fragment}
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Update a fragment", role = DefaultRole.ADMIN)
    @RequestMapping(method = RequestMethod.PUT, value = "/{fragmentId}")
    public ResponseEntity<EntityModel<Fragment>> updateFragment(@PathVariable(name = "fragmentId") Long id,
            @Valid @RequestBody Fragment fragment) throws ModuleException {
        return ResponseEntity.ok(toResource(fragmentService.updateFragment(id, fragment)));
    }

    /**
     * Delete a fragment
     * @param id fragment identifier
     * @return nothing
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Delete a fragment", role = DefaultRole.ADMIN)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{fragmentId}")
    public ResponseEntity<Void> deleteFragment(@PathVariable(name = "fragmentId") Long id) throws ModuleException {
        fragmentService.deleteFragment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Export a model fragment
     * @param request HTTP request
     * @param response HTTP response
     * @param fragmentId fragment to export
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Export a fragment", role = DefaultRole.ADMIN)
    @RequestMapping(method = RequestMethod.GET, value = "/{fragmentId}/export")
    public void exportFragment(HttpServletRequest request, HttpServletResponse response,
            @PathVariable(name = "fragmentId") Long fragmentId) throws ModuleException {

        Fragment fragment = fragmentService.getFragment(fragmentId);
        String exportedFilename = FRAGMENT_FILE_PREFIX + fragment.getName() + FRAGMENT_EXTENSION;

        // Produce octet stream to force navigator opening "save as" dialog
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportedFilename + "\"");

        try {
            fragmentService.exportFragment(fragmentId, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            final String message = String.format("Error with servlet output stream while exporting fragment %s.",
                                                 fragment.getName());
            LOGGER.error(message, e);
            throw new ModuleException(e);
        }
    }

    /**
     * Import a model fragment
     * @param file file representing the fragment
     * @return nothing
     * @throws ModuleException if error occurs!
     */
    @ResourceAccess(description = "Import a fragment", role = DefaultRole.ADMIN)
    @RequestMapping(method = RequestMethod.POST, value = "/import")
    public ResponseEntity<EntityModel<Fragment>> importFragment(@RequestParam("file") MultipartFile file)
            throws ModuleException {
        try {
            Fragment frag = fragmentService.importFragment(file.getInputStream());
            return new ResponseEntity<>(toResource(frag), HttpStatus.CREATED);
        } catch (IOException e) {
            final String message = "Error with file stream while importing fragment.";
            LOGGER.error(message, e);
            throw new ModuleException(e);
        }
    }

    @Override
    public EntityModel<Fragment> toResource(Fragment fragment, Object... extras) {
        final EntityModel<Fragment> resource = resourceService.toResource(fragment);
        resourceService.addLink(resource, this.getClass(), "getFragment", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, fragment.getId()));
        resourceService.addLink(resource, this.getClass(), "updateFragment", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, fragment.getId()),
                                MethodParamFactory.build(Fragment.class));
        if (isDeletable(fragment)) {
            resourceService.addLink(resource, this.getClass(), "deleteFragment", LinkRels.DELETE,
                                    MethodParamFactory.build(Long.class, fragment.getId()));
        }
        resourceService.addLink(resource, this.getClass(), "getFragments", LinkRels.LIST);
        // Export
        resourceService.addLink(resource, this.getClass(), "exportFragment", LinkRelation.of("export"),
                                MethodParamFactory.build(HttpServletRequest.class),
                                MethodParamFactory.build(HttpServletResponse.class),
                                MethodParamFactory.build(Long.class, fragment.getId()));
        return resource;
    }

    private boolean isDeletable(Fragment fragment) {
        List<AttributeModel> fragmentAttributes = attributeModelService.findByFragmentId(fragment.getId());
        return fragmentAttributes.isEmpty();
    }
}
